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

package org.eclipse.lsp4jakarta.jdt.internal.jsonb;

/**
 * Lightweight accumulator for tracking method invocation analysis.
 * Used to track whether a method has close() calls and thread source invocations.
 */
class MethodAnalysis {

    /**
     * Indicates whether the method contains a close() invocation on Jsonb or related types.
     */
    boolean hasClose = false;

    /**
     * Count of thread source invocations found in the method.
     */
    int threadSourceCount = 0;
}
