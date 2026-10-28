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
import jakarta.persistence.AssociationOverrides;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;

/**
 * Invalid: @AssociationOverrides container with three nested @AssociationOverride entries.
 * The first and third specify both joinColumns and joinTable; the second specifies neither.
 * Expected: two AssociationOverrideBothJoinColumnsAndJoinTable diagnostics, one on the
 * first and one on the third entry; none on the second.
 */
@Entity
@AssociationOverrides({
    @AssociationOverride(
        name        = "supervisor",
        joinColumns = @JoinColumn(name = "SUPER_ID"),
        joinTable   = @JoinTable(name = "EMP_SUPER")
    ),
    @AssociationOverride(name = "id"),
    @AssociationOverride(
        name        = "teamLead",
        joinColumns = @JoinColumn(name = "LEAD_ID"),
        joinTable   = @JoinTable(name = "EMP_LEAD")
    )
})
public class InvalidContainerMultipleBothAttributes extends NamedPerson {
}
