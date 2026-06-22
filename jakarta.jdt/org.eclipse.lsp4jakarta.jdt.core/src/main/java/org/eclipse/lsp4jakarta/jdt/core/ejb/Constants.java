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
package org.eclipse.lsp4jakarta.jdt.core.ejb;

/**
 * Constants for Jakarta Enterprise Beans (EJB) diagnostics.
 */
public class Constants {

    /* Annotations */
    public static final String MESSAGE_DRIVEN = "MessageDriven";
    public static final String MESSAGE_DRIVEN_FQ_NAME = "jakarta.ejb.MessageDriven";

    /* Interfaces */
    public static final String MESSAGE_LISTENER = "MessageListener";
    public static final String MESSAGE_LISTENER_FQ_NAME = "jakarta.jms.MessageListener";

    /* Diagnostic codes */
    public static final String DIAGNOSTIC_SOURCE = "jakarta-ejb";
    public static final String DIAGNOSTIC_CODE = "ImplementMessageListener";
    public static final String DIAGNOSTIC_CODE_MESSAGE = "MessageDrivenMustImplementMessageListener";
}