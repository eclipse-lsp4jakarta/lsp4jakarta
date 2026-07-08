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
 *     IBM Corporation - initial implementation
 *******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.internal.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Jakarta Security diagnostic constants.
 */
public class Constants {
    /* Annotation Constants */
    public static final String LDAP_IDENTITY_STORE_DEFINITION_FQ_NAME = "jakarta.security.enterprise.identitystore.LdapIdentityStoreDefinition";
    public static final String DATABASE_IDENTITY_STORE_DEFINITION_FQ_NAME = "jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition";
    public static final String APPLICATION_SCOPED_FQ_NAME = "jakarta.enterprise.context.ApplicationScoped";
    public static final String DEFAULT_FQ_NAME = "jakarta.enterprise.inject.Default";

    /* CDI Scope Annotations */
    public static final Set<String> SCOPE_FQ_NAMES = new HashSet<>(Arrays.asList(
                                                                                 "jakarta.enterprise.context.Dependent",
                                                                                 "jakarta.enterprise.context.ApplicationScoped",
                                                                                 "jakarta.enterprise.context.ConversationScoped",
                                                                                 "jakarta.enterprise.context.RequestScoped",
                                                                                 "jakarta.enterprise.context.SessionScoped",
                                                                                 "jakarta.enterprise.context.NormalScope",
                                                                                 "jakarta.interceptor.Interceptor",
                                                                                 "jakarta.decorator.Decorator"));

    /* Diagnostic Source */
    public static final String DIAGNOSTIC_SOURCE = "jakarta-security";

    /* Diagnostic Codes */
    public static final String DIAGNOSTIC_CODE_MISSING_APPLICATION_SCOPED = "MissingApplicationScopedAnnotation";
    public static final String DIAGNOSTIC_CODE_INVALID_SCOPE = "InvalidIdentityStoreScope";
}
