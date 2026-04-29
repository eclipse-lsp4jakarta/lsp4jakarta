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
package org.eclipse.lsp4jakarta.jdt.internal.interceptor;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;

/**
 * InterceptorUtils - util class for Interceptor
 */
public class InterceptorUtils {

    private static final Logger LOGGER = Logger.getLogger(InterceptorUtils.class.getName());

    /**
     * Method used to check if annotation matches Interceptor method annotations fully qualified name
     *
     * @param type
     * @param annotationName
     * @return
     * @throws JavaModelException
     */
    public static boolean isInterceptorAnnotation(IType type, String annotationName) throws JavaModelException {
        return Constants.INTERCEPTOR_METHODS.stream().anyMatch(annotation -> {
            try {
                return DiagnosticUtils.isMatchedJavaElement(type, annotationName, annotation);
            } catch (JavaModelException e) {
                LOGGER.log(Level.WARNING, "Unable to find matching annotation", e.getMessage());
                return false;
            }
        });
    }
}