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

            // Collect types in this CU that are annotated with @Specializes
            List<IType> specializersInUnit = new ArrayList<>();
            for (IType type : typesInUnit) {
                if (DiagnosticUtils.isMatchedAnnotation(unit, type.getAnnotations(), Constants.SPECIALIZES_FQ_NAME)) {
                    specializersInUnit.add(type);
                }
            }

            if (specializersInUnit.isEmpty()) {
                return diagnostics;
            }

            // Validate each @Specializes type in this CU
            for (IType type : specializersInUnit) {
                // Rule 1: direct superclass must be a scoped CDI bean
                validateSpecializes(type, uri, context, diagnostics);

                // Rule 2: must not declare an explicit bean name via @Named
                for (IAnnotation annotation : type.getAnnotations()) {
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
                String supertypeFqName = resolveUltimateBaseFqName(type);
                if (supertypeFqName != null && hasInconsistentSpecialization(type, supertypeFqName)) {
                    Range range = PositionUtils.toNameRange(type, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("InconsistentSpecialization",
                                                                                 type.getElementName(),
                                                                                 supertypeFqName),
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
     * Returns {@code true} if more than one subtype of {@code ultimateBaseFqName}
     * carries {@code @Specializes}, indicating inconsistent specialization.
     *
     * <p>Builds an {@link ITypeHierarchy} rooted at the ultimate base and checks
     * all subtypes — both direct and transitive — for the {@code @Specializes}
     * annotation.
     *
     * @param specializerType the type being validated (used to resolve the project)
     * @param ultimateBaseFqName the fully-qualified name of the ultimate base bean
     * @return {@code true} if the specialization is inconsistent
     * @throws JavaModelException if the Java model cannot be accessed
     */
    private boolean hasInconsistentSpecialization(IType specializerType,
                                                  String ultimateBaseFqName) throws JavaModelException {
        IType baseType = specializerType.getJavaProject().findType(ultimateBaseFqName);
        if (baseType == null) {
            return false;
        }
        ITypeHierarchy hierarchy = baseType.newTypeHierarchy(null);
        IType[] subtypes = hierarchy.getAllSubtypes(baseType);
        int specializerCount = 0;
        for (IType subtype : subtypes) {
            if (DiagnosticUtils.isMatchedAnnotation(subtype.getCompilationUnit(),
                                                    subtype.getAnnotations(),
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
     * Walks the @Specializes chain transitively to find the ultimate base bean FQ name.
     * Returns null if there is no explicit superclass or the name cannot be resolved.
     *
     * @param type the type to resolve
     * @return the ultimate base FQ class name, or null
     */
    private String resolveUltimateBaseFqName(IType type) {
        try {
            String superclassName = type.getSuperclassName();
            if (superclassName == null) {
                return null;
            }
            String fqName = ManagedBean.getFullyQualifiedClassName(type, superclassName);
            if (fqName == null || "java.lang.Object".equals(fqName)) {
                return null;
            }
            IType superType = type.getJavaProject().findType(fqName);
            if (superType != null &&
                DiagnosticUtils.isMatchedAnnotation(superType.getCompilationUnit(),
                                                    superType.getAnnotations(),
                                                    Constants.SPECIALIZES_FQ_NAME)) {
                return resolveUltimateBaseFqName(superType);
            }
            return fqName;
        } catch (JavaModelException e) {
            LOGGER.log(Level.WARNING, "Unable to resolve ultimate base for type: " + type.getElementName(), e);
            return null;
        }
    }
}
