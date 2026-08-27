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

/**
 * CDI diagnostics participant that detects inconsistent specialization.
 *
 * <p>Per CDI 3.0 specification §4.3.1 (Direct and indirect specialization):
 * a bean X <em>specializes</em> bean Y if X directly specializes Y, or
 * transitively specializes Y through a chain of {@code @Specializes} beans.
 * Per §5.1.3 (Inconsistent specialization):
 * "Suppose an enabled bean X specializes a second bean Y. If there is another
 * enabled bean that specializes Y we say that inconsistent specialization exists.
 * The container automatically detects inconsistent specialization and treats it
 * as a deployment problem."
 *
 * <p>When the current compilation unit contains a class annotated with
 * {@code @Specializes}, this participant scans all source types in the same
 * Java project. It resolves the <em>ultimate</em> base bean of each specializer
 * (following the transitive chain), then reports a diagnostic on any type in the
 * current file whose ultimate base is also the ultimate base of another
 * {@code @Specializes} type elsewhere in the project.
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#direct_and_indirect_specialization">CDI 3.0 §4.3.1</a>
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#inconsistent_specialization">CDI 3.0 §5.1.3</a>
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

            // Build a map of ultimate base FQ name → list of all @Specializes types across the project.
            // The ultimate base is resolved by walking the @Specializes chain transitively (§4.3.1).
            Map<String, List<IType>> specializersByUltimateBase = collectProjectSpecializersByUltimateBase(unit);

            // Check each specializer in the current CU for conflicts
            for (IType type : specializersInUnit) {
                String supertypeFqName = resolveUltimateBaseFqName(type);
                if (supertypeFqName == null) {
                    continue;
                }
                List<IType> allSpecializersOfBase = specializersByUltimateBase.get(supertypeFqName);
                // Inconsistent specialization (§5.1.3): more than one bean ultimately specializes the same base
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
     * Scans all source compilation units in the same Java project and builds a map from
     * the <em>ultimate base</em> fully-qualified name to the list of types annotated
     * with {@code @Specializes} that (directly or transitively) specialize that base.
     *
     * <p>The ultimate base is the first type in the {@code @Specializes} chain that
     * does not itself carry {@code @Specializes} — i.e. the root of the specialization
     * hierarchy. This correctly handles transitive specialization as defined in §4.3.1.
     *
     * @param currentUnit the compilation unit being validated (used to obtain the project)
     * @return map of ultimate base FQ name → list of specializer types across the project
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private Map<String, List<IType>> collectProjectSpecializersByUltimateBase(ICompilationUnit currentUnit) throws JavaModelException {
        Map<String, List<IType>> result = new HashMap<>();
        IJavaProject javaProject = currentUnit.getJavaProject();

        for (IPackageFragmentRoot root : javaProject.getPackageFragmentRoots()) {
            // Only scan source folders — skip binary JARs and class folders
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
     * Resolves the fully-qualified name of the <em>ultimate base bean</em> of the given
     * type by following the {@code @Specializes} chain transitively (§4.3.1).
     *
     * <p>Starting from the direct superclass of {@code type}, if that superclass itself
     * carries {@code @Specializes}, the walk continues up the chain until a superclass
     * without {@code @Specializes} is found. That type is the ultimate base.
     *
     * <p>Returns {@code null} if the type has no explicit superclass, if the superclass
     * is {@code java.lang.Object}, or if the name cannot be resolved.
     *
     * @param type the type whose ultimate base should be resolved
     * @return the ultimate base FQ class name, or {@code null}
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
            // Walk up the chain: if the direct superclass itself has @Specializes,
            // delegate to it so we find the common root of the entire chain.
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
