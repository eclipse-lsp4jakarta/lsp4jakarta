/*******************************************************************************
* Copyright (c) 2021, 2025 IBM Corporation and others.
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

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;

/**
 * This class provides type hierarchy utilities for checking the
 * type hierarchy of {@code IType}.
 */
@SuppressWarnings("restriction")
public class TypeHierarchyUtils {

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

    private static boolean isSuperType(IType type, String superType) {

        if (superType.endsWith("." + type.getElementName())) {
            return type.getFullyQualifiedName().equals(superType);
        }
        return type.getElementName().equals(superType);

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
}
