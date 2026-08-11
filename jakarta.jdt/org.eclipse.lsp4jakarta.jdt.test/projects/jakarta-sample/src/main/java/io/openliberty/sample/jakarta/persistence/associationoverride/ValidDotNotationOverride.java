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
 * Valid: dot-notation name="subDept.coordinator" — "subDept" in DepartmentWithTeam,
 * "coordinator" in SubTeam.
 * Expected: no diagnostic.
 */
@Entity
public class ValidDotNotationOverride {
    @Id
    private Long id;

    @Embedded
    @AssociationOverride(name = "subDept.coordinator", joinColumns = @JoinColumn(name = "COORD_ID"))
    private DepartmentWithTeam dept;
}
