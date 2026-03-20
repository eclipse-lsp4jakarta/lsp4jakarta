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
*     IBM Corporation, Archana Iyer R - initial implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.interceptor;

import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaErrorCode;

/**
 * Interceptor error codes.
 */
public enum ErrorCode implements IJavaErrorCode {
    InvalidInterceptorNoArgsConstructorMissing,
    InvalidInterceptorAnnotationOnAbstractClass;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCode() {
        return name();
    }
}
