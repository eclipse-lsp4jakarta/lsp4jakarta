/*******************************************************************************
* Copyright (c) 2026 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package io.openliberty.sample.jakarta.persistence.associationoverride;

import jakarta.persistence.AssociationOverrides;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Invalid: @AssociationOverrides({}) empty container on an @Embedded field.
 * Expected: diagnostic AssociationOverridesEmptyContainer.
 */
@Entity
public class InvalidEmptyAssociationOverridesOnField {
    @Id
    private Long id;

    @Embedded
    @AssociationOverrides({})
    private Department dept;
}
