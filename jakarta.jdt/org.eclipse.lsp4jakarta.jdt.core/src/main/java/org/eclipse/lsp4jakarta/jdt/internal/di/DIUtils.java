/*******************************************************************************
* Copyright (c) 2026 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation, Archana Iyer - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.di;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.ManagedBean;

/**
 * CdiQualifierUtils
 */
public class DIUtils {

    private static final String QUALIFIER_META = "jakarta.inject.Qualifier";
    private static final Logger LOGGER = Logger.getLogger(DIUtils.class.getName());

    /**
     * @param annotation
     * @param unit
     * @param type
     * @return
     * @throws JavaModelException
     * @description Method is used to check if the passed annotation is a built in or custom Qualifier type
     */
    public static boolean isQualifier(IAnnotation annotation, ICompilationUnit unit, IType type) throws JavaModelException {
        boolean hasBuiltInQualifier = Constants.BUILT_IN_QUALIFIERS.stream().anyMatch(qualifier -> {
            try {
                return DiagnosticUtils.isMatchedAnnotation(unit, annotation, qualifier);
            } catch (JavaModelException e) {
                LOGGER.log(Level.INFO, "Unable to fetch qualifier information", e.getMessage());
                return false;
            }
        });
        if (!hasBuiltInQualifier) {
            String annotationFQ = ManagedBean.getFullyQualifiedClassName(type, annotation.getElementName());
            IJavaProject project = annotation.getJavaProject();
            if (project != null && annotationFQ != null) {
                IType customAnnotationType = project.findType(annotationFQ);
                ICompilationUnit customClassCompilerUnit = customAnnotationType.getCompilationUnit();
                if (customAnnotationType != null) {
                    for (IAnnotation meta : customAnnotationType.getAnnotations()) {
                        return DiagnosticUtils.isMatchedAnnotation(customClassCompilerUnit, meta, QUALIFIER_META);
                    }
                }
            }
        }
        return hasBuiltInQualifier;
    }
}