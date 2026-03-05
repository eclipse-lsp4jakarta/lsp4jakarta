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
 *		IBM Corporation, Archana Iyer R - initial implementation
 *******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.interceptor;

import java.util.Set;

/**
 * Interceptor constants.
 */
public class Constants {

    /* INTERCEPTOR_FQ_NAME */
    public static final String INTERCEPTOR_FQ_NAME = "jakarta.interceptor.Interceptor";

    /* Source */
    public static final String DIAGNOSTIC_SOURCE = "jakarta-interceptor";

    private static final String AROUND_CONSTRUCT_FQ_NAME = "jakarta.interceptor.AroundConstruct";

    private static final String POST_CONSTRUCT_FQ_NAME = "jakarta.annotation.PostConstruct";

    private static final String AROUND_INVOKE_FQ_NAME = "jakarta.interceptor.AroundInvoke";

    private static final String PRE_DESTROY_FQ_NAME = "jakarta.annotation.PreDestroy";

    private static final String AROUND_TIMEOUT_FQ_NAME = "jakarta.interceptor.AroundTimeout";

    public static final Set<String> INTERCEPTOR_METHODS = Set.of(AROUND_INVOKE_FQ_NAME, AROUND_CONSTRUCT_FQ_NAME, AROUND_TIMEOUT_FQ_NAME, PRE_DESTROY_FQ_NAME,
                                                                 POST_CONSTRUCT_FQ_NAME);

    public static final String JAKARTA_INTERCEPTOR_INVOCATION_CONTEXT = "jakarta.interceptor.InvocationContext";

    public static final String PROCEED = "proceed";

    public static final String INTERCEPTOR_IMPORT = "jakarta.interceptor";

}
