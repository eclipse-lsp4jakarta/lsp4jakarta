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
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
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

/** Validates CDI specialization: superclass must be a scoped bean, no duplicate specialization of the same base. */
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
            IType[] typesInUnit = unit.getAllTypes();

            // Validate each type annotated with @Specializes
            for (IType type : typesInUnit) {
                IAnnotation[] annotations = type.getAnnotations();
                if (!DiagnosticUtils.isMatchedAnnotation(unit, annotations, Constants.SPECIALIZES_FQ_NAME)) {
                    continue;
                }

                // Rule 1: direct superclass must be a scoped CDI bean
                validateSpecializes(type, uri, context, diagnostics);

                // Rule 2: must not declare an explicit bean name via @Named
                for (IAnnotation annotation : annotations) {
                    if (DiagnosticUtils.isMatchedAnnotation(unit, annotation, Constants.NAMED_FQ_NAME)) {
                        Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage("SpecializedBeanWithNamedAnnotation", type.getElementName()),
                                                                 range,
                                                                 Constants.DIAGNOSTIC_SOURCE, null,
                                                                 ErrorCode.InvalidSpecializedBeanWithNamedAnnotation,
                                                                 DiagnosticSeverity.Error));
                        break;
                    }
                }

                // Rule 3: inconsistent specialization -- more than one bean specializes the same base
                IType directSuperType = resolveDirectSuperclass(type);
                if (directSuperType != null && hasInconsistentSpecialization(directSuperType)) {
                    Range range = PositionUtils.toNameRange(type, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("InconsistentSpecialization",
                                                                                 type.getElementName(),
                                                                                 directSuperType.getFullyQualifiedName()),
                                                             range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidInconsistentSpecialization,
                                                             DiagnosticSeverity.Error));
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
     * @param type the type to validate
     * @param uri the file URI
     * @param context the diagnostics context
     * @param diagnostics the list to add diagnostics to
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private void validateSpecializes(IType type, String uri, JavaDiagnosticsContext context,
                                     List<Diagnostic> diagnostics) throws JavaModelException {
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
            directSuperclassIsBean = TypeHierarchyUtils.directSuperclassHasAnnotationWithMetaAnnotation(
                                                                                                        type, Constants.NORMAL_SCOPE_FQ_NAME);
        }
        if (directSuperclassIsBean) {
            return;
        }
        Range range = PositionUtils.toNameRange(type, context.getUtils());
        diagnostics.add(context.createDiagnostic(uri,
                                                 Messages.getMessage("InvalidSpecializesAnnotationOnNonBeanSuperclass"),
                                                 range,
                                                 Constants.DIAGNOSTIC_SOURCE, null,
                                                 ErrorCode.InvalidSpecializesAnnotationOnNonBeanSuperclass,
                                                 DiagnosticSeverity.Error));
    }

    /**
     * Returns {@code true} if more than one direct subclass of {@code directSuperType}
     * carries {@code @Specializes}, indicating inconsistent specialization.
     *
     * <p>Builds an {@link ITypeHierarchy} rooted at the direct superclass and checks
     * immediate subclasses for the {@code @Specializes} annotation.
     *
     * @param directSuperType the direct superclass type
     * @return {@code true} if multiple direct subclasses specialize the base bean
     * @throws JavaModelException if the Java model cannot be accessed
     */
    private boolean hasInconsistentSpecialization(IType directSuperType) throws JavaModelException {
        ITypeHierarchy hierarchy = directSuperType.newTypeHierarchy(null);
        IType[] subclasses = hierarchy.getSubclasses(directSuperType);
        int specializerCount = 0;
        for (IType subclass : subclasses) {
            if (DiagnosticUtils.isMatchedAnnotation(subclass.getCompilationUnit(),
                                                    subclass.getAnnotations(),
                                                    Constants.SPECIALIZES_FQ_NAME)) {
                specializerCount++;
                if (specializerCount > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Resolves the direct superclass IType for the given type.
     * Returns null if there is no explicit superclass or if it cannot be resolved.
     *
     * @param type the type whose superclass is to be resolved
     * @return the direct superclass IType, or null
     */
    private IType resolveDirectSuperclass(IType type) {
        try {
            String superclassName = type.getSuperclassName();
            if (superclassName == null || "Object".equals(superclassName) || "java.lang.Object".equals(superclassName)) {
                return null;
            }
            return ManagedBean.getChildITypeByName(type, superclassName);
        } catch (JavaModelException e) {
            LOGGER.log(Level.WARNING, "Unable to resolve direct superclass for type: " + type.getElementName(), e);
            return null;
        }
    }
}
