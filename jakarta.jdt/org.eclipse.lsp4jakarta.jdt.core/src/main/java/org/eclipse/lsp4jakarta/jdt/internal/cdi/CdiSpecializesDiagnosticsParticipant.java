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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
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

/** Detects inconsistent specialization: more than one bean specializing the same base bean. */
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

            // Build a map of ultimate base FQ name to all @Specializes types across the project
            Map<String, List<IType>> specializersByUltimateBase = collectProjectSpecializersByUltimateBase(unit);

            // Report a diagnostic on any type whose ultimate base is shared by another specializer
            for (IType type : specializersInUnit) {
                String supertypeFqName = resolveUltimateBaseFqName(type);
                if (supertypeFqName == null) {
                    continue;
                }
                List<IType> allSpecializersOfBase = specializersByUltimateBase.get(supertypeFqName);
                if (allSpecializersOfBase != null && allSpecializersOfBase.size() > 1) {
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
            LOGGER.log(Level.SEVERE, "Error occurred while checking for inconsistent specialization", e);
        }

        return diagnostics;
    }

    /**
     * Scans all source types in the project and builds a map from ultimate base FQ name
     * to the list of @Specializes types that specialize it (directly or transitively).
     *
     * @param currentUnit the compilation unit being validated
     * @return map of ultimate base FQ name to list of specializer types
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private Map<String, List<IType>> collectProjectSpecializersByUltimateBase(ICompilationUnit currentUnit) throws JavaModelException {
        Map<String, List<IType>> result = new HashMap<>();
        IJavaProject javaProject = currentUnit.getJavaProject();

        for (IPackageFragmentRoot root : javaProject.getPackageFragmentRoots()) {
            if (root.getKind() != IPackageFragmentRoot.K_SOURCE) {
                continue;
            }
            for (IJavaElement child : root.getChildren()) {
                if (!(child instanceof IPackageFragment)) {
                    continue;
                }
                IPackageFragment pkg = (IPackageFragment) child;
                for (ICompilationUnit cu : pkg.getCompilationUnits()) {
                    for (IType type : cu.getAllTypes()) {
                        if (!DiagnosticUtils.isMatchedAnnotation(cu, type.getAnnotations(), Constants.SPECIALIZES_FQ_NAME)) {
                            continue;
                        }
                        String ultimateBaseFqName = resolveUltimateBaseFqName(type);
                        if (ultimateBaseFqName != null) {
                            result.computeIfAbsent(ultimateBaseFqName, k -> new ArrayList<>()).add(type);
                        }
                    }
                }
            }
        }
        return result;
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
