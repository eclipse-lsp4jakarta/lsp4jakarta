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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal;

import java.util.logging.Logger;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Utility class for scanning source types across a Java project.
 *
 * <p>Provides a project-wide type visitor facility and related functional
 * interfaces used by diagnostic participants that require cross-file analysis.
 */
public class SourceTypeScanner {

    private static final Logger LOGGER = Logger.getLogger(SourceTypeScanner.class.getName());

    private SourceTypeScanner() {
        // utility class — not instantiable
    }

    /**
     * A checked-exception consumer for {@link IAnnotation} values.
     *
     * <p>Used wherever JDT model traversal needs to pass each visited annotation
     * to a callback that may throw {@link JavaModelException} — for example when
     * iterating nested annotations inside a container annotation.
     *
     * <p>{@link java.util.function.Consumer} cannot be used directly here because
     * Java's standard functional interfaces do not declare checked exceptions.
     */
    @FunctionalInterface
    public interface AnnotationConsumer {
        /**
         * Processes the given annotation.
         *
         * @param ann the annotation to process
         * @throws JavaModelException if a JDT model error occurs
         */
        void accept(IAnnotation ann) throws JavaModelException;
    }

    /**
     * A checked-exception visitor called once per source {@link IType} during a
     * project-wide scan via {@link #scanSourceTypes}.
     */
    @FunctionalInterface
    public interface TypeVisitor {
        /**
         * Called for each source type in the project.
         *
         * @param unit the compilation unit that owns {@code type}
         * @param type the type currently being visited
         * @throws JavaModelException if the JDT model cannot be accessed
         */
        void visit(ICompilationUnit unit, IType type) throws JavaModelException;
    }

    /**
     * Visits every source {@link IType} in the given {@link IJavaProject} and
     * passes each one to {@code visitor}.
     *
     * <p>Only source roots (kind {@link IPackageFragmentRoot#K_SOURCE}) are
     * visited. The traversal uses the JDT project model directly, so it is
     * always consistent with the workspace state without requiring the JDT
     * search index to be up to date.
     *
     * @param javaProject the project whose sources are scanned
     * @param visitor called once for every source type found
     */
    public static void scanSourceTypes(IJavaProject javaProject, TypeVisitor visitor) {
        try {
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
                            try {
                                visitor.visit(cu, type);
                            } catch (JavaModelException e) {
                                LOGGER.warning("[scanSourceTypes] JavaModelException on type "
                                               + type.getFullyQualifiedName() + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (JavaModelException e) {
            LOGGER.warning("[scanSourceTypes] Failed to scan project sources: " + e.getMessage());
        }
    }
}
