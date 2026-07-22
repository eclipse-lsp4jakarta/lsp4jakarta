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

package org.eclipse.lsp4jakarta.jdt.internal.ejb;

/**
 * Enterprise JavaBeans (EJB) diagnostic constants.
 */
public class Constants {
    /* Annotation Constants */
    public static final String STATELESS_FQ_NAME = "jakarta.ejb.Stateless";
    public static final String STATEFUL_FQ_NAME = "jakarta.ejb.Stateful";
    public static final String SINGLETON_FQ_NAME = "jakarta.ejb.Singleton";

    /* Session synchronization annotation constants */
    public static final String AFTER_BEGIN_FQ_NAME = "jakarta.ejb.AfterBegin";
    public static final String BEFORE_COMPLETION_FQ_NAME = "jakarta.ejb.BeforeCompletion";
    public static final String AFTER_COMPLETION_FQ_NAME = "jakarta.ejb.AfterCompletion";

    public static final String DIAGNOSTIC_SOURCE = "jakarta-ejb";
    public static final String DIAGNOSTIC_CODE_MISSING_PUBLIC_CONSTRUCTOR = "MissingPublicNoArgConstructor";

    public static final String FINALIZE_METHOD_NAME = "finalize";

    /** JVM return-type descriptor for {@code void}. */
    public static final String VOID_RETURN_TYPE = "V";

    public static final String[] SESSION_BEAN_ANNOTATIONS = {
                                                              STATELESS_FQ_NAME,
                                                              STATEFUL_FQ_NAME,
                                                              SINGLETON_FQ_NAME
    };

    public static final String[] SESSION_SYNC_ANNOTATIONS = {
                                                              AFTER_BEGIN_FQ_NAME,
                                                              BEFORE_COMPLETION_FQ_NAME,
                                                              AFTER_COMPLETION_FQ_NAME
    };
}