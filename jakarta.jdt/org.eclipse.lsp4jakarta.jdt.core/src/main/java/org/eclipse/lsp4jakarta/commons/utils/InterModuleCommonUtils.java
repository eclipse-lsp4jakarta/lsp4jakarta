/*******************************************************************************
* Copyright (c) 2026 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.commons.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.interceptor.Constants;

/**
 * Utilities for working with cross module common functionalities of annotations, fields, methods etc.
 */
public class InterModuleCommonUtils {

    private static final Logger LOGGER = Logger.getLogger(InterModuleCommonUtils.class.getName());

    /**
     * Checks if type is of Interceptor type. Returns true if it finds @Interceptor annotation or goes and checks if has references of Interceptor in the class
     *
     * @param type
     * @param unit
     * @return
     * @throws JavaModelException
     */
    public static boolean checkIsInterceptorType(IType type, ICompilationUnit unit) throws JavaModelException {
        boolean isInterceptorType = false;
        if (type != null) {
            isInterceptorType = Arrays.stream(type.getAnnotations()).filter(Objects::nonNull).anyMatch(annotation -> {
                try {
                    return DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(),
                                                                Constants.INTERCEPTOR_FQ_NAME);
                } catch (JavaModelException e) {
                    LOGGER.log(Level.WARNING, "Unable to find matching annotation", e.getMessage());
                    return false;
                }
            });
            if (!isInterceptorType) {
                return DiagnosticUtils.isImportReferencedJavaElement(unit, Constants.INTERCEPTOR_IMPORT);
            }
        }
        return isInterceptorType;
    }
}