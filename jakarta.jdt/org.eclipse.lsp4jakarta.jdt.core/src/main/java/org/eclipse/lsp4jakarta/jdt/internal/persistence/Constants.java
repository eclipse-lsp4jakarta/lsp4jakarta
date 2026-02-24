/*******************************************************************************
* Copyright (c) 2020, 2026 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation, Ankush Sharma - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.internal.persistence;

/**
 * Persistence diagnostic constants.
 */
public class Constants {
    /* Annotation Constants */
    public static final String ENTITY = "jakarta.persistence.Entity";
    public static final String MAPKEY = "jakarta.persistence.MapKey";
    public static final String MAPKEYCLASS = "jakarta.persistence.MapKeyClass";
    public static final String MAPKEYJOINCOLUMN = "jakarta.persistence.MapKeyJoinColumn";
    public static final String ID = "jakarta.persistence.Id";
    public static final String TEMPORAL = "jakarta.persistence.Temporal";

    /* Annotation Fields */
    public static final String NAME = "name";
    public static final String REFERENCEDCOLUMNNAME = "referencedColumnName";

    /* Source */
    public static final String DIAGNOSTIC_SOURCE = "jakarta-persistence";

    public static final String[] SET_OF_PERSISTENCE_ANNOTATIONS = { MAPKEY, MAPKEYCLASS, MAPKEYJOINCOLUMN };
    public static final String[] SET_OF_PRIMARY_KEY_DATE_ANNOTATIONS = { ID, TEMPORAL };

    public static final String UTIL_DATE = "java.util.Date";
    public static final String TEMPORAL_TYPE_DATE = "TemporalType.DATE";
}