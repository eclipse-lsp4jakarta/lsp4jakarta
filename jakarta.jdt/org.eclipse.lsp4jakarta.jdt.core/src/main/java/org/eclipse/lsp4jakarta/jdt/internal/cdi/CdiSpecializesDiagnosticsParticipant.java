/*******************************************************************************
* Copyright (c) 2026 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation - initial implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.cdi;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.TypeHierarchyUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.ManagedBean;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * CDI diagnostics participant that validates specialization.
 *
 * <p>Validates two separate specialization scenarios per the CDI 3.0 specification:</p>
 *
 * <ol>
 *   <li><strong>Bean-level specialization</strong> (§3.1.4): A class annotated with
 *       {@code @Specializes} must directly extend a CDI bean (a class with a scope
 *       annotation). A specialized bean must also not declare an explicit bean name
 *       using {@code @Named}.</li>
 *   <li><strong>Producer method specialization</strong>
 *       (§specialize_producer_method): A producer method annotated with
 *       {@code @Specializes} must be non-static and must directly override another
 *       producer method in the superclass.</li>
 * </ol>
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#direct_and_indirect_specialization">CDI 3.0 §direct_and_indirect_specialization</a>
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#specialize_producer_method">CDI 3.0 §specialize_producer_method</a>
 */
public class CdiSpecializesDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    private static final Logger LOGGER = Logger.getLogger(CdiSpecializesDiagnosticsParticipant.class.getName());

    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        String uri = context.getUri();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        try {
            IType[] types = unit.getAllTypes();
            for (IType type : types) {
                boolean isSpecializesAnnotated = DiagnosticUtils.isMatchedAnnotation(unit, type.getAnnotations(), Constants.SPECIALIZES_FQ_NAME);
                if (isSpecializesAnnotated) {
                    validateSpecializes(type, uri, context, diagnostics);
                    // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#direct_and_indirect_specialization
                    // A specialized bean must not declare an explicit bean name using @Named.
                    // The name is inherited from the bean it specializes.
                    for (IAnnotation annotation : type.getAnnotations()) {
                        if (DiagnosticUtils.isMatchedAnnotation(unit, annotation, Constants.NAMED_FQ_NAME)) {
                            Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                            diagnostics.add(context.createDiagnostic(uri,
                                                                     Messages.getMessage("SpecializedBeanWithNamedAnnotation", type.getElementName()), range,
                                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                                     ErrorCode.InvalidSpecializedBeanWithNamedAnnotation, DiagnosticSeverity.Error));
                            break;
                        }
                    }
                }

                // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#specialize_producer_method
                // A producer method annotated with @Specializes must:
                //   1. Be non-static
                //   2. Directly override another producer method in a superclass
                for (IMethod method : type.getMethods()) {
                    boolean hasSpecializes = DiagnosticUtils.isMatchedAnnotation(unit, method.getAnnotations(), Constants.SPECIALIZES_FQ_NAME);
                    boolean hasProduces = DiagnosticUtils.isMatchedAnnotation(unit, method.getAnnotations(), Constants.PRODUCES_FQ_NAME);
                    if (hasSpecializes && hasProduces) {
                        validateSpecializesProducerMethod(method, type, unit, uri, context, diagnostics);
                    }
                }
            }
        } catch (JavaModelException e) {
            LOGGER.log(Level.SEVERE, "Error occurred while validating @Specializes usage", e);
        }

        return diagnostics;
    }

    /**
     * Validates that a class annotated with @Specializes directly extends a valid bean.
     *
     * Per CDI spec section 3.1.4: "the bean class of X must directly extend the bean class
     * of another managed bean Y". Only the immediate superclass is checked — a scoped
     * grandparent does NOT satisfy this requirement.
     *
     * @param type the type to validate
     * @param uri the file URI
     * @param context the diagnostics context
     * @param diagnostics the list to add diagnostics to
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private void validateSpecializes(IType type, String uri, JavaDiagnosticsContext context,
                                     List<Diagnostic> diagnostics) throws JavaModelException {
        // Per CDI spec 3.1.4, only the direct (immediate) superclass must be a bean.
        // Check built-in scope annotations first.
        boolean directSuperclassIsBean = Stream.concat(Constants.SCOPE_FQ_NAMES.stream(),
                                                       Stream.of(Constants.NORMAL_SCOPE_FQ_NAME)).anyMatch(scopeFQName -> {
                                                           try {
                                                               return TypeHierarchyUtils.directSuperClassHasAnnotation(type, scopeFQName);
                                                           } catch (JavaModelException e) {
                                                               LOGGER.log(Level.WARNING, "Could not inspect direct superclass annotations", e);
                                                               return false;
                                                           }
                                                       });
        if (!directSuperclassIsBean) {
            // Also accept a custom scope: any annotation on the direct superclass whose
            // annotation type is itself meta-annotated with @NormalScope.
            directSuperclassIsBean = TypeHierarchyUtils.directSuperclassHasAnnotationWithMetaAnnotation(
                                                                                                        type, Constants.NORMAL_SCOPE_FQ_NAME);
        }
        if (directSuperclassIsBean) {
            return;
        }
        // Direct superclass is not a bean — specialization is invalid
        Range range = PositionUtils.toNameRange(type, context.getUtils());
        diagnostics.add(context.createDiagnostic(uri,
                                                 Messages.getMessage("InvalidSpecializesAnnotationOnNonBeanSuperclass"),
                                                 range,
                                                 Constants.DIAGNOSTIC_SOURCE, null,
                                                 ErrorCode.InvalidSpecializesAnnotationOnNonBeanSuperclass,
                                                 DiagnosticSeverity.Error));
    }

    /**
     * Validates a producer method annotated with {@code @Specializes}.
     *
     * <p>Per CDI spec §specialize_producer_method, the method must:</p>
     * <ol>
     *   <li>Be non-static</li>
     *   <li>Directly override another producer method (annotated with {@code @Produces})
     *       in the direct superclass</li>
     * </ol>
     *
     * <p>Both violations are reported independently — a static method that also fails
     * the override check will produce two diagnostics.</p>
     *
     * @param method the producer method to validate
     * @param type the declaring type
     * @param unit the compilation unit
     * @param uri the file URI
     * @param context the diagnostics context
     * @param diagnostics the list to add diagnostics to
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private void validateSpecializesProducerMethod(IMethod method, IType type, ICompilationUnit unit,
                                                   String uri, JavaDiagnosticsContext context,
                                                   List<Diagnostic> diagnostics) throws JavaModelException {
        // Rule 1: the method must not be static
        if (Flags.isStatic(method.getFlags())) {
            Range range = PositionUtils.toNameRange(method, context.getUtils());
            diagnostics.add(context.createDiagnostic(uri,
                                                     Messages.getMessage("InvalidSpecializesStaticProducerMethod"),
                                                     range,
                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                     ErrorCode.InvalidSpecializesStaticProducerMethod,
                                                     DiagnosticSeverity.Error));
        }

        // Rule 2: the method must directly override a @Produces method in the superclass
        String superclassName = type.getSuperclassName();
        boolean overridesSuperProducer = false;
        if (superclassName != null) {
            IType superclassType = ManagedBean.getChildITypeByName(type, superclassName);
            if (superclassType != null) {
                for (IMethod superMethod : superclassType.getMethods()) {
                    if (superMethod.getElementName().equals(method.getElementName())
                            && superMethod.getParameterTypes().length == method.getParameterTypes().length) {
                        boolean superHasProduces = DiagnosticUtils.isMatchedAnnotation(
                                superclassType.getCompilationUnit(),
                                superMethod.getAnnotations(),
                                Constants.PRODUCES_FQ_NAME);
                        if (superHasProduces) {
                            overridesSuperProducer = true;
                            break;
                        }
                    }
                }
            }
        }
        if (!overridesSuperProducer) {
            Range range = PositionUtils.toNameRange(method, context.getUtils());
            diagnostics.add(context.createDiagnostic(uri,
                                                     Messages.getMessage("InvalidSpecializesProducerMethodNotOverriding"),
                                                     range,
                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                     ErrorCode.InvalidSpecializesProducerMethodNotOverriding,
                                                     DiagnosticSeverity.Error));
        }
    }
}
