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
package org.eclipse.lsp4jakarta.jdt.internal.cdi;

/**
 * Distinguishes the two {@code ObserverMethod.notify} overloads that a custom
 * {@code ObserverMethod} implementation may override.
 */
enum NotifyVariant {
    /** The {@code notify(T event)} overload. */
    EVENT,
    /** The {@code notify(EventContext<T> eventContext)} overload. */
    EVENT_CONTEXT
}
