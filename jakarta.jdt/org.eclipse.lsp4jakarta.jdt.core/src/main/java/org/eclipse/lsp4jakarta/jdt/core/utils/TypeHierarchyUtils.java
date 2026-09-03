/*******************************************************************************
* Copyright (c) 2021, 2026 IBM Corporation and others.
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

package org.eclipse.lsp4jakarta.jdt.core.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.ManagedBean;

/**
 * This class provides type hierarchy utilities for checking the
 * type hierarchy of {@code IType}.
 */
@SuppressWarnings("restriction")
public class TypeHierarchyUtils {

    private static final Logger LOGGER = Logger.getLogger(TypeHierarchyUtils.class.getName());

    public static final int HAS_SUPERTYPE = 1;

    private static final String OBJECT_FQ_NAME = "java.lang.Object";

    /**
     *
     * @param type The root type of which the super-types are checked.
     * @param superType The super-type to check for.
     * @return An integer: 1 for {@code type} certainly extends/implements
     *         {@code superType},
     *         -1 for {@code type} certainly does not extend/implement
     *         {@code superType},
     *         and 0 if unknown (in the case of incomplete type hierarchy)
     * @throws CoreException
     */
    public static int doesITypeHaveSuperType(IType type, String superType) throws CoreException {
        // Handle trivial case
        if (isSuperType(type, superType)) {
            return 1;
        }

        ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(DefaultWorkingCopyOwner.PRIMARY, null);
        int r = 0;

        // Check if the type's supertypes contain the superType
        IType[] parents = typeHierarchy.getAllSupertypes(type);
        for (IType parentType : parents) {
            if (isSuperType(parentType, superType)) {
                r = 1;
                break;
            }
        }

        // If we haven't found the supertype, check if the type indeed does not extend
        // superType, or if we don't know.
        // We don't know if we don't have the class declaration for ANY of the super
        // types
        if (r == 0) {
            boolean unknown = false;
            for (IType parentType : parents) {
                if (!parentType.getFullyQualifiedName().startsWith("java.") && !hasKnownDeclaration(parentType)) {
                    unknown = true;
                    break;
                }
            }
            if (!unknown) {
                r = -1;
            }
        }
        return r;
    }

    private static boolean hasKnownDeclaration(IType type) throws CoreException {
        String typeName = type.getElementName();
        final AtomicInteger references = new AtomicInteger(0);
        SearchEngine engine = new SearchEngine();
        SearchPattern pattern = SearchPattern.createPattern(typeName, IJavaSearchConstants.CLASS,
                                                            IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
        engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
                      createSearchScope(type.getJavaProject()), new SearchRequestor() {

                          @Override
                          public void acceptSearchMatch(SearchMatch match) throws CoreException {
                              Object o = match.getElement();
                              if (o instanceof IType) {
                                  IType t = (IType) o;
                                  if (t.getElementName().equals(typeName)) {
                                      references.incrementAndGet();
                                  }
                              }
                          }
                      }, null);
        if (references.get() > 0) {
            return true;
        }
        return false;
    }

    private static IJavaSearchScope createSearchScope(IJavaProject javaProject) throws CoreException {
        return SearchEngine.createJavaSearchScope(new IJavaProject[] { javaProject }, IJavaSearchScope.SOURCES);
    }

    /**
     * isSuperType
     * This checks whether the given type is a super type.
     *
     * @param type
     * @param superType
     * @return
     */
    private static boolean isSuperType(IType type, String superType) {

        if (superType.endsWith("." + type.getElementName())) {
            return type.getFullyQualifiedName().equals(superType);
        }
        return type.getElementName().equals(superType);

    }

    /**
     * @param type
     * @param hierarchy
     * @throws JavaModelException
     * @description This method traverses back to collect the super classes of the respective class
     */
    public static void collectSuperTypes(IType type, Set<IType> hierarchy) throws JavaModelException {
        if (type == null || hierarchy.contains(type))
            return;
        hierarchy.add(type);
        if (type.getSuperclassName() != null) {
            IType superType = ManagedBean.getChildITypeByName(type, type.getSuperclassName());
            collectSuperTypes(superType, hierarchy);
        }
    }

    /**
     * inheritsFrom
     * Check if specified superType is present or not in the type hierarchy
     *
     * @param fieldType
     * @param superType
     * @return
     * @throws CoreException
     */
    public static boolean inheritsFrom(IType fieldType, String superType) throws CoreException {
        return doesITypeHaveSuperType(fieldType, superType) == HAS_SUPERTYPE;
    }

    /**
     * Gets all interfaces implemented by a type and its superclasses.
     *
     * @param type the type to inspect
     * @return an array of all interface types
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    public static IType[] getAllInterfaces(IType type) throws JavaModelException {
        ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
        return typeHierarchy.getAllInterfaces();
    }

    /**
     * Walks the full superclass chain of {@code type} and returns the first
     * ancestor {@link IType} that is annotated with {@code annotationFQName}.
     *
     * <p>The type itself is skipped — only superclasses are examined.</p>
     *
     * @param type the root type whose superclass chain is searched
     * @param annotationFQName the fully-qualified name of the annotation to look for
     *            (e.g. {@code "jakarta.persistence.Entity"})
     * @return the first superclass {@link IType} that carries the annotation,
     *         or {@code null} if none is found
     * @throws JavaModelException if the type hierarchy cannot be resolved
     */
    public static IType findSupertypeWithAnnotation(IType type, String annotationFQName) throws JavaModelException {
        Set<IType> hierarchy = new HashSet<>();
        collectSuperTypes(type, hierarchy);

        for (IType superType : hierarchy) {
            // Skip the type itself — only ancestors are of interest.
            if (superType.equals(type)) {
                continue;
            }
            try {
                if (DiagnosticUtils.isMatchedAnnotation(superType.getCompilationUnit(),
                                                        superType.getAnnotations(),
                                                        annotationFQName)) {
                    return superType;
                }
            } catch (JavaModelException e) {
                LOGGER.warning("Could not inspect annotations on superclass "
                               + superType.getFullyQualifiedName() + ": " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Walks the full superclass chain of {@code type} and returns the FQ name of the first
     * invalid annotation found in any ancestor, or {@code null} if none is found.
     *
     * <p>The type itself is skipped — only superclasses are examined. Every level of the
     * hierarchy is checked; the walk stops early when a match is found.</p>
     *
     * @param type the root type whose superclass chain is searched
     * @param invalidAnnotationFQNames the fully-qualified names of the annotations to treat
     *            as invalid (e.g. the scopes that are not permitted for this bean type)
     * @return the matched annotation FQ name if any ancestor carries one of the invalid
     *         annotations; {@code null} if no such ancestor exists
     * @throws JavaModelException if the type hierarchy cannot be resolved
     */

    public static String findSupertypeWithAnyAnnotation(IType type,
                                                        Collection<String> invalidAnnotationFQNames) throws JavaModelException {
        ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
        IType superclass = hierarchy.getSuperclass(type);

        while (superclass != null && !OBJECT_FQ_NAME.equals(superclass.getFullyQualifiedName())) {
            try {
                for (String annotationFQName : invalidAnnotationFQNames) {
                    if (DiagnosticUtils.isMatchedAnnotation(superclass.getCompilationUnit(),
                                                            superclass.getAnnotations(),
                                                            annotationFQName)) {
                        return annotationFQName;
                    }
                }
            } catch (JavaModelException e) {
                LOGGER.warning("Could not inspect annotations on superclass "
                               + superclass.getFullyQualifiedName() + ": " + e.getMessage());
            }
            superclass = hierarchy.getSuperclass(superclass);
        }
        return null;
    }

    /**
     * Returns {@code true} if the <em>direct</em> (immediate) superclass of
     * {@code type} carries {@code annotationFQName}.
     *
     * <p>Only the single class named by {@link IType#getSuperclassName()} is
     * inspected — grandparents and further ancestors are not considered.
     * This is intentionally distinct from {@link #findSupertypeWithAnnotation},
     * which walks the full hierarchy.</p>
     *
     * @param type the type whose direct superclass is checked
     * @param annotationFQName the fully-qualified annotation name to look for
     * @return {@code true} if the direct superclass carries the annotation;
     *         {@code false} if there is no direct superclass, the superclass
     *         cannot be resolved, or it does not carry the annotation
     * @throws JavaModelException if the Java model cannot be accessed
     */
    public static boolean directSuperClassHasAnnotation(IType type, String annotationFQName) throws JavaModelException {
        IType superclassType = resolveDirectSuperclass(type);
        if (superclassType == null) {
            return false;
        }
        return DiagnosticUtils.isMatchedAnnotation(superclassType.getCompilationUnit(),
                                                   superclassType.getAnnotations(),
                                                   annotationFQName);
    }

    /**
     * Returns {@code true} if the <em>direct</em> (immediate) superclass of
     * {@code type} carries any annotation that is itself meta-annotated with
     * {@code metaAnnotationFQName}.
     *
     * <p>This builds on {@link #hasAnnotation} by first resolving the direct
     * superclass via {@link #resolveDirectSuperclass}, then delegating each of
     * its annotations to
     * {@link ManagedBean#hasMetaAnnotation(IAnnotation, IType, ICompilationUnit, String)}
     * to check whether the annotation's own type carries the meta-annotation.
     * It is used, for example, to detect custom CDI scopes on a superclass: a
     * user-defined {@code @MyScope} annotation is a valid scope when its own
     * declaration is annotated with {@code @NormalScope}.</p>
     *
     * <p>Only the single class named by {@link IType#getSuperclassName()} is
     * inspected — grandparents and further ancestors are not considered.</p>
     *
     * @param type the type whose direct superclass is checked
     * @param metaAnnotationFQName the fully-qualified name of the meta-annotation
     *            to look for on the annotation types of the direct superclass
     *            (e.g. {@code "jakarta.enterprise.context.NormalScope"})
     * @return {@code true} if the direct superclass has at least one annotation
     *         whose own type carries {@code metaAnnotationFQName};
     *         {@code false} if there is no direct superclass, it cannot be
     *         resolved, or none of its annotations carry the meta-annotation
     * @throws JavaModelException if the Java model cannot be accessed
     */
    public static boolean directSuperclassHasAnnotationWithMetaAnnotation(IType type,
                                                                          String metaAnnotationFQName) throws JavaModelException {
        IType superclassType = resolveDirectSuperclass(type);
        if (superclassType == null) {
            return false;
        }
        return Arrays.stream(superclassType.getAnnotations()).anyMatch(annotation -> {
            try {
                return ManagedBean.hasMetaAnnotation(annotation, superclassType, superclassType.getCompilationUnit(), metaAnnotationFQName);
            } catch (JavaModelException e) {
                return false;
            }
        });
    }

    /**
     * Resolves and returns the direct (immediate) superclass {@link IType} of
     * {@code type}, or {@code null} if there is no superclass or it cannot be
     * resolved.
     *
     * @param type the type whose direct superclass is resolved
     * @return the resolved {@link IType} of the direct superclass, or {@code null}
     * @throws JavaModelException if the Java model cannot be accessed
     */
    private static IType resolveDirectSuperclass(IType type) throws JavaModelException {
        String superclassName = type.getSuperclassName();
        if (superclassName == null) {
            return null;
        }
        return ManagedBean.getChildITypeByName(type, superclassName);
    }
}
