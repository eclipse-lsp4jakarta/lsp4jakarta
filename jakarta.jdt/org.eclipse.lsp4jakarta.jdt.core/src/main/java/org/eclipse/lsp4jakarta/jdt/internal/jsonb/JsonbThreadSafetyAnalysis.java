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
 * Accumulator for tracking Jsonb thread safety analysis within a method.
 * Tracks whether a method properly closes Jsonb instances when using thread sources.
 *
 * <p>This class is used to detect potential thread safety issues where Jsonb instances
 * are used in multi-threaded contexts without proper close() calls.
 */
class JsonbThreadSafetyAnalysis {

    /**
     * Indicates whether the method contains a close() invocation on Jsonb or related types
     * (Closeable, AutoCloseable).
     */
    boolean hasClose = false;

    /**
     * Indicates whether the method uses Jsonb instances (via method calls).
     * Only methods that use Jsonb AND have thread sources should generate diagnostics.
     */
    boolean methodUsesJsonb = false;

    /**
     * Count of thread source invocations found in the method.
     * Thread sources include ExecutorService, CompletableFuture, Thread, Timer, etc.
     */
    int threadSourceCount = 0;
}
