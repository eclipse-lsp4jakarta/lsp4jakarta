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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.ManagedBean;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * CDI diagnostics participant that validates the use of the {@code @Typed} annotation.
 *
 * <p>Per CDI 3.0 specification section 2.2.2:</p>
 * <blockquote>
 * If a bean class or producer method or field specifies a {@code @Typed} annotation, and the
 * {@code value} member specifies a class which does not correspond to a type in the unrestricted
 * set of bean types of a bean, the container automatically detects the problem and treats it as
 * a definition error.
 * </blockquote>
 *
 * <p>The unrestricted set of bean types for a managed bean is: the bean class itself, every
 * superclass (excluding {@code Object}), and every interface implemented directly or indirectly
 * by the class. For a producer method the set is derived from the return type; for a producer
 * field it is derived from the field type.</p>
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#restricting_bean_types">CDI 3.0 §2.2.2</a>
 */
public class CdiTypedAnnotationDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(CdiTypedAnnotationDiagnosticsParticipant.class.getName());

    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        String uri = context.getUri();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        IType[] types = unit.getAllTypes();
        for (IType type : types) {
            // Check @Typed on the bean class itself
            for (IAnnotation annotation : type.getAnnotations()) {
                if (DiagnosticUtils.isMatchedAnnotation(unit, annotation, Constants.TYPED_FQ_NAME)) {
                    checkTypedAnnotation(context, uri, diagnostics, type, type, annotation);
                }
            }

            // Check @Typed on producer fields
            for (IField field : type.getFields()) {
                for (IAnnotation annotation : field.getAnnotations()) {
                    if (DiagnosticUtils.isMatchedAnnotation(unit, annotation, Constants.TYPED_FQ_NAME)) {
                        IType fieldType = resolveTypeFromSignature(type, field.getTypeSignature());
                        checkTypedAnnotation(context, uri, diagnostics, type, fieldType, annotation);
                    }
                }
            }

            // Check @Typed on producer methods
            for (IMethod method : type.getMethods()) {
                for (IAnnotation annotation : method.getAnnotations()) {
                    if (DiagnosticUtils.isMatchedAnnotation(unit, annotation, Constants.TYPED_FQ_NAME)) {
                        IType returnType = resolveTypeFromSignature(type, method.getReturnType());
                        checkTypedAnnotation(context, uri, diagnostics, type, returnType, annotation);
                    }
                }
            }
        }

        return diagnostics;
    }

    /**
     * Validates a {@code @Typed} annotation against the unrestricted bean types of
     * {@code beanType}. Each class listed in the annotation's {@code value} member
     * that is not found in the supertype hierarchy of {@code beanType} produces a
     * diagnostic.
     *
     * @param context the diagnostics context
     * @param uri URI of the compilation unit
     * @param diagnostics list to add diagnostics to
     * @param declaringType the type that owns the annotated element (used for name resolution)
     * @param beanType the type whose unrestricted bean types are checked (the bean class,
     *            producer field type, or producer method return type); may be
     *            {@code null} if the type could not be resolved
     * @param typedAnnotation the {@code @Typed} annotation
     */
    private void checkTypedAnnotation(JavaDiagnosticsContext context, String uri,
                                      List<Diagnostic> diagnostics,
                                      IType declaringType, IType beanType,
                                      IAnnotation typedAnnotation) throws JavaModelException {
        if (beanType == null) {
            return;
        }

        List<String> typedValues = DiagnosticUtils.getAnnotationClassValues(typedAnnotation, "value");
        if (typedValues.isEmpty()) {
            return;
        }

        List<String> unrestrictedTypes = getUnrestrictedBeanTypes(beanType);
        Range range = PositionUtils.toNameRange(typedAnnotation, context.getUtils());

        for (String typedValue : typedValues) {
            // Use ManagedBean.getFullyQualifiedClassName to resolve the simple name
            String resolvedFQName = ManagedBean.getFullyQualifiedClassName(declaringType, typedValue);
            if (resolvedFQName != null && !isInUnrestrictedSet(resolvedFQName, unrestrictedTypes)) {
                diagnostics.add(context.createDiagnostic(uri,
                                                         Messages.getMessage("InvalidTypedAnnotationNonMatchingBeanType", typedValue),
                                                         range,
                                                         Constants.DIAGNOSTIC_SOURCE,
                                                         null,
                                                         ErrorCode.InvalidTypedAnnotationNonMatchingBeanType,
                                                         DiagnosticSeverity.Error));
            }
        }
    }

    /**
     * Returns the unrestricted set of bean types for {@code type}: the type itself,
     * all superclasses (excluding {@code java.lang.Object}), and all directly or
     * indirectly implemented interfaces.
     *
     * <p>Uses {@link ITypeHierarchy#newSupertypeHierarchy} — the same JDT API used
     * by {@link DiagnosticUtils#doesImplementInterfaces} — instead of a manual
     * recursive walk.</p>
     */
    private List<String> getUnrestrictedBeanTypes(IType type) {
        try {
            ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());

            return Stream.concat(
                                 Stream.of(type.getFullyQualifiedName()),
                                 Stream.concat(
                                               Arrays.stream(hierarchy.getAllSuperclasses(type)).map(IType::getFullyQualifiedName).filter(fqn -> !Constants.OBJECT_FQ_NAME.equals(fqn)),
                                               Arrays.stream(hierarchy.getAllInterfaces()).map(IType::getFullyQualifiedName))).collect(Collectors.toList());
        } catch (JavaModelException e) {
            LOGGER.log(Level.WARNING, "Error collecting unrestricted bean types for: " + type.getFullyQualifiedName(), e);
            return List.of();
        }
    }

    /**
     * Returns {@code true} when {@code resolvedFQName} matches (by raw-type erasure)
     * any entry in {@code unrestrictedTypes}. The comparison strips generic parameters
     * so that e.g. {@code Shop} matches {@code Shop<Book>}.
     */
    private boolean isInUnrestrictedSet(String resolvedFQName, List<String> unrestrictedTypes) {
        String rawResolved = eraseGenericParameters(resolvedFQName);
        for (String candidate : unrestrictedTypes) {
            if (rawResolved.equals(eraseGenericParameters(candidate))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Strips {@code <...>} type parameters, returning the raw class name.
     */
    private String eraseGenericParameters(String name) {
        int idx = name.indexOf('<');
        return idx >= 0 ? name.substring(0, idx) : name;
    }

    /**
     * Resolves a JDT type signature to an {@link IType} by erasing generics and
     * delegating name resolution to {@link ManagedBean#getChildITypeByName}.
     */
    private IType resolveTypeFromSignature(IType declaringType, String typeSignature) {
        try {
            String typeName = Signature.toString(Signature.getTypeErasure(typeSignature));
            return ManagedBean.getChildITypeByName(declaringType, typeName);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error resolving type from signature: " + typeSignature, e);
            return null;
        }
    }
}
