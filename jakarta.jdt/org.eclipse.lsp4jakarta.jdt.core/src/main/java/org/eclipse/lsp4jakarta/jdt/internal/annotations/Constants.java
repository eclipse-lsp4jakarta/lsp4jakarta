/*******************************************************************************
* Copyright (c) 2021, 2026 IBM Corporation and others.
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

package org.eclipse.lsp4jakarta.jdt.internal.annotations;

/**
 * Annotation diagnostic constants.
 */
public class Constants {

    /* Diagnostics source key */
    public static final String DIAGNOSTIC_SOURCE = "jakarta-annotations";

    /* @Generated */
    public static final String GENERATED_FQ_NAME = "jakarta.annotation.Generated";
    public static final String ISO_8601_REGEX = "^([\\+-]?\\d{4}(?!\\d{2}\\b))((-?)((0[1-9]|1[0-2])(\\3([12]\\d|0[1-9]|3[01]))?|W([0-4]\\d|5[0-2])(-?[1-7])?|(00[1-9]|0[1-9]\\d|[12]\\d{2}|3([0-5]\\d|6[1-6])))([T\\s]((([01]\\d|2[0-3])((:?)[0-5]\\d)?|24\\:?00)([\\.,]\\d+(?!:))?)?(\\17[0-5]\\d([\\.,]\\d+)?)?([zZ]|([\\+-])([01]\\d|2[0-3]):?([0-5]\\d)?)?)?)?$";

    /* @Resource */
    public static final String RESOURCE_FQ_NAME = "jakarta.annotation.Resource";

    /* @Resources */
    public static final String RESOURCES_FQ_NAME = "jakarta.annotation.Resources";

    /* @Priority */
    public static final String PRIORITY_FQ_NAME = "jakarta.annotation.Priority";

    /* @PostConstruct */
    public static final String POST_CONSTRUCT_FQ_NAME = "jakarta.annotation.PostConstruct";

    /* @PreDesotroy */
    public static final String PRE_DESTROY_FQ_NAME = "jakarta.annotation.PreDestroy";

    public static final String EXCEPTION = "Exception";

    public static final String RUNTIME_EXCEPTION = "RuntimeException";
}
