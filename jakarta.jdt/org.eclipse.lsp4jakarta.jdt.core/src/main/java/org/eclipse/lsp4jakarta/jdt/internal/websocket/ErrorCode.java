/*******************************************************************************
* Copyright (c) 2023, 2026 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.jdt.internal.websocket;

import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaErrorCode;

/**
 * WebSocket error code.
 */
public enum ErrorCode implements IJavaErrorCode {
    InvalidOnOpenParams,
    InvalidOnCloseParams,
    PathParamsMissingFromParam,
    PathParamDoesNotMatchEndpointURI,
    OnMessageDuplicateMethod,
    InvalidEndpointPathWithNoStartingSlash,
    InvalidEndpointPathWithRelativePaths,
    InvalidEndpointPathNotTempleateOrPartialURI,
    InvalidEndpointPathDuplicateVariable,
    missingPublicNoArgConstructor,
    DuplicateLifeCycleAnnotation;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCode() {
        return name();
    }
}
