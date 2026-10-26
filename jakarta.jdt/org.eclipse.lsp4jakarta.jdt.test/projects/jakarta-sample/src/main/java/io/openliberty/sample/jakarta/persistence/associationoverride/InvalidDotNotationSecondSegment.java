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

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

/**
 * Invalid: dot-notation "subDept.owner" — first segment "subDept" exists in
 * DepartmentWithTeam, but second segment "owner" does not exist in SubTeam.
 * Expected: diagnostic InvalidAssociationOverrideName on the @AssociationOverride annotation.
 */
@Entity
public class InvalidDotNotationSecondSegment {
    @Id
    private Long id;

    @Embedded
    @AssociationOverride(name = "subDept.owner", joinColumns = @JoinColumn(name = "OWNER_ID"))
    private DepartmentWithTeam dept;
}
