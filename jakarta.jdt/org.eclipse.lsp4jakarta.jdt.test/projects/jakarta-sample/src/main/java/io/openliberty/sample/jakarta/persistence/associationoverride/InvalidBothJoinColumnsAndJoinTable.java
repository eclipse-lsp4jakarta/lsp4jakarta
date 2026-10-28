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
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;

/**
 * Invalid: @AssociationOverride specifies both joinColumns and joinTable.
 * Expected: diagnostic AssociationOverrideBothJoinColumnsAndJoinTable.
 */
@Entity
@AssociationOverride(
    name        = "supervisor",
    joinColumns = @JoinColumn(name = "MGR_ID"),
    joinTable   = @JoinTable(name = "EMP_MGR")
)
public class InvalidBothJoinColumnsAndJoinTable extends Person {
}
