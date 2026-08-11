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
import jakarta.persistence.JoinTable;

/**
 * Invalid: @AssociationOverride on a property-based (getter) @Embedded accessor
 * specifies both joinColumns and joinTable.
 * Expected: diagnostic AssociationOverrideBothJoinColumnsAndJoinTable.
 */
@Entity
public class InvalidPropertyBasedBothAttributes {
    @Id
    private Long id;
    private Department dept;

    @Embedded
    @AssociationOverride(
        name        = "manager",
        joinColumns = @JoinColumn(name = "MGR_ID"),
        joinTable   = @JoinTable(name = "EMP_MGR")
    )
    public Department getDept() {
        return dept;
    }
}
