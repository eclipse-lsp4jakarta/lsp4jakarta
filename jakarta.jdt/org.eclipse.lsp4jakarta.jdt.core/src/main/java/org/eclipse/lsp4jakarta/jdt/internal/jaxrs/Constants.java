/*******************************************************************************
* Copyright (c) 2023, 2025 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation, Matthew Shocrylas - initial API and implementation.
*     Bera Sogut
*******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.internal.jaxrs;

/**
 * JAX-RS diagnostics constants.
 */
public class Constants {

    public static final String DIAGNOSTIC_SOURCE = "jakarta-jaxrs";
    public static final String RESOURCE_METHOD = "ResourceMethod";
    public static final String PATH_ANNOTATION = "jakarta.ws.rs.Path";
    public static final String PROVIDER_ANNOTATION = "jakarta.ws.rs.ext.Provider";

    public final static String[] SET_OF_METHOD_DESIGNATORS_ANNOTATIONS = {
                                                                           "jakarta.ws.rs.GET",
                                                                           "jakarta.ws.rs.POST",
                                                                           "jakarta.ws.rs.PUT",
                                                                           "jakarta.ws.rs.DELETE",
                                                                           "jakarta.ws.rs.PATCH",
                                                                           "jakarta.ws.rs.HEAD",
                                                                           "jakarta.ws.rs.OPTIONS" };
    public final static String[] SET_OF_NON_ENTITY_PARAM_ANNOTATIONS = {
                                                                         "jakarta.ws.rs.FormParam",
                                                                         "jakarta.ws.rs.MatrixParam",
                                                                         "jakarta.ws.rs.QueryParam",
                                                                         "jakarta.ws.rs.PathParam",
                                                                         "jakarta.ws.rs.CookieParam",
                                                                         "jakarta.ws.rs.HeaderParam",
                                                                         "jakarta.ws.rs.core.Context",
                                                                         "jakarta.ws.rs.BeanParam" };
    public final static String[] SET_OF_JAXRS_ANNOTATIONS1 = {
                                                               PATH_ANNOTATION,
                                                               PROVIDER_ANNOTATION };

}
