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
package org.eclipse.lsp4jakarta.jdt.core.java.diagnostics;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;

/**
 * Constructor information diagnostics helper for a given method.
 *
 * @author Archana Iyer
 *
 */
public final class ConstructorInfoDiagnosticHelper {

    private final boolean hasConstructor;
    private final boolean hasValidPublicNoArgsConstructor;
    private final boolean hasValidProtectedNoArgsConstructor;

    private ConstructorInfoDiagnosticHelper(boolean hasConstructor,
                                            boolean hasValidPublicNoArgsConstructor,
                                            boolean hasValidProtectedNoArgsConstructor) {
        this.hasConstructor = hasConstructor;
        this.hasValidPublicNoArgsConstructor = hasValidPublicNoArgsConstructor;
        this.hasValidProtectedNoArgsConstructor = hasValidProtectedNoArgsConstructor;
    }

    public boolean hasConstructor() {
        return hasConstructor;
    }

    public boolean hasValidPublicNoArgsConstructor() {
        return hasValidPublicNoArgsConstructor;
    }

    public boolean hasValidProtectedNoArgsConstructor() {
        return hasValidProtectedNoArgsConstructor;
    }

    @Override
    public String toString() {
        return "ConstructorInfoDiagnosticHelper [hasConstructor=" + hasConstructor
               + ", hasValidPublicNoArgsConstructor=" + hasValidPublicNoArgsConstructor
               + ", hasValidProtectedNoArgsConstructor=" + hasValidProtectedNoArgsConstructor + "]";
    }

    /**
     * Factory utility method checks the constructor existence and returns the constructor information
     *
     * @param method
     * @return
     * @throws JavaModelException
     */
    public static ConstructorInfoDiagnosticHelper getConstructorInfo(IMethod method) throws JavaModelException {
        boolean hasConstructor = false;
        boolean hasValidPublicNoArgs = false;
        boolean hasValidProtectedNoArgs = false;
        if (DiagnosticUtils.isConstructorMethod(method)) {
            hasConstructor = true; // Check explicit constructor declaration
            String[] params = method.getParameterTypes();
            int flags = method.getFlags();

            if (params.length == 0) { // User-defined no-args constructor
                if (Flags.isPublic(flags)) {
                    hasValidPublicNoArgs = true;
                }
                if (Flags.isProtected(flags)) {
                    hasValidProtectedNoArgs = true;
                }
            }
        }
        return new ConstructorInfoDiagnosticHelper(hasConstructor, hasValidPublicNoArgs, hasValidProtectedNoArgs); // This ensures that the values don't get changed
    }
}
