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

/**
 * Invalid: @AssociationOverrides container with two entries that share the same
 * name "supervisor". The second entry is a duplicate.
 * Expected: diagnostic AssociationOverridesDuplicateName on the second entry.
 */
@Entity
@AssociationOverrides({
    @AssociationOverride(name = "supervisor", joinColumns = @JoinColumn(name = "SUP_ID")),
    @AssociationOverride(name = "supervisor", joinColumns = @JoinColumn(name = "SUP2_ID"))
})
public class InvalidDuplicateAssociationOverrideNames extends Person {
}
