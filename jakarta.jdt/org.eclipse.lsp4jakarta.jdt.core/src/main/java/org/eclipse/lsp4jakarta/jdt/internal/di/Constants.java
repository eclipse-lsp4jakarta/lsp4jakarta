/*******************************************************************************
* Copyright (c) 2021, 2026 IBM Corporation.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Himanshu Chotwani
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.di;

import java.util.List;
import java.util.Set;

/**
 * Dependency injection diagnostic constants.
 */
public class Constants {

    /* Annotation Constants */
    public static final String INJECT_FQ_NAME = "jakarta.inject.Inject";

    /* Diagnostics fields constants */
    public static final String DIAGNOSTIC_SOURCE = "jakarta-di";

    public static final Set<String> CDI_ANNOTATIONS_FQ = Set.of("jakarta.enterprise.context.ApplicationScoped",
                                                                "jakarta.enterprise.context.ConversationScoped", "jakarta.enterprise.context.RequestScoped",
                                                                "jakarta.enterprise.context.SessionScoped", "jakarta.enterprise.context.NormalScope",
                                                                "jakarta.Interceptor", "jakarta.Decorator", "jakarta.enterprise.inject.Stereotype",
                                                                "jakarta.enterprise.context.Dependent");

    public static final Set<String> BUILT_IN_QUALIFIERS = Set.of(
                                                                 "jakarta.enterprise.inject.Default",
                                                                 "jakarta.enterprise.inject.Any",
                                                                 "jakarta.inject.Named");

    public static final Set<String> IMPLICIT_QUALIFIERS = Set.of(
                                                                 "jakarta.enterprise.inject.Default",
                                                                 "jakarta.enterprise.inject.Any");
}
