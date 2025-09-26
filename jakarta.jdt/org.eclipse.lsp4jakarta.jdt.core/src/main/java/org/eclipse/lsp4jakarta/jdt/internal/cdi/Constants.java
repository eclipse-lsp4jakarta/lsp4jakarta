/*******************************************************************************
* Copyright (c) 2021, 2025 IBM Corporation.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Hani Damlaj
*******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.internal.cdi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Context and Dependency Injection (CDI) diagnostic constants.
 */
public class Constants {
    /* Annotation Constants */
    public static final String PRODUCES_FQ_NAME = "jakarta.enterprise.inject.Produces";
    public static final String INJECT_FQ_NAME = "jakarta.inject.Inject";
    public static final String DISPOSES_FQ_NAME = "jakarta.enterprise.inject.Disposes";
    public static final String OBSERVES_FQ_NAME = "jakarta.enterprise.event.Observes";
    public static final String OBSERVES_ASYNC_FQ_NAME = "jakarta.enterprise.event.ObservesAsync";
    public static final String DEPENDENT_FQ_NAME = "jakarta.enterprise.context.Dependent";

    public static final String DIAGNOSTIC_SOURCE = "jakarta-cdi";
    public static final String DIAGNOSTIC_CODE = "InvalidManagedBeanAnnotation";
    public static final String DIAGNOSTIC_CODE_SCOPEDECL = "InvalidScopeDecl";
    public static final String DIAGNOSTIC_CODE_PRODUCES_INJECT = "RemoveProducesOrInject";

    public static final String CONSTRUCTOR_DIAGNOSTIC_CODE = "InvalidManagedBeanConstructor";

    public static final String DIAGNOSTIC_CODE_INVALID_INJECT_PARAM = "RemoveInjectOrConflictedAnnotations";
    public static final String DIAGNOSTIC_CODE_INVALID_PRODUCES_PARAM = "RemoveProducesOrConflictedAnnotations";
    public static final String DIAGNOSTIC_CODE_INVALID_DISPOSES_PARAM = "RemoveDisposesOrConflictedAnnotations";

    public static final String DIAGNOSTIC_CODE_REDUNDANT_DISPOSES = "RemoveExtraDisposes";
    public static final String[] INVALID_INITIALIZER_PARAMS_FQ = { DISPOSES_FQ_NAME, OBSERVES_FQ_NAME,
                                                                   OBSERVES_ASYNC_FQ_NAME };
    public static final String[] INVALID_PRODUCER_PARAMS_FQ = { DISPOSES_FQ_NAME, OBSERVES_FQ_NAME,
                                                                OBSERVES_ASYNC_FQ_NAME };

    //Added as part of fix that adds two quick fixes which are mutually exclusive issue #540
    public static final String[] INVALID_DISPOSER_FQ_PARAMS = { DISPOSES_FQ_NAME };
    public static final String[] INVALID_DISPOSER_FQ_CONFLICTED_PARAMS = { OBSERVES_FQ_NAME, OBSERVES_ASYNC_FQ_NAME };
    // List can be found in the cdi doc here:
    // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#bean_defining_annotations
    public static final Set<String> SCOPES = new HashSet<String>(Arrays.asList("Dependent", "ApplicationScoped", "ConversationScoped", "RequestScoped", "SessionScoped",
                                                                               "NormalScope", "Interceptor", "Decorator", "Stereotype"));
    public static final Set<String> SCOPE_FQ_NAMES = new HashSet<String>(Arrays.asList(DEPENDENT_FQ_NAME, "jakarta.enterprise.context.ApplicationScoped",
                                                                                       "jakarta.enterprise.context.ConversationScoped", "jakarta.enterprise.context.RequestScoped",
                                                                                       "jakarta.enterprise.context.SessionScoped", "jakarta.enterprise.context.NormalScope",
                                                                                       "jakarta.Interceptor", "jakarta.Decorator", "jakarta.enterprise.inject.Stereotype"));
}
