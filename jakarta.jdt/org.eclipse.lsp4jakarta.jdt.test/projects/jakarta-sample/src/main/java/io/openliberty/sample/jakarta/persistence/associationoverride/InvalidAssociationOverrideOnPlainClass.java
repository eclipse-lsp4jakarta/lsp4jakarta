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
import jakarta.persistence.JoinColumn;

/**
 * Invalid: @AssociationOverride applied to a plain class that is neither
 * @Entity, @MappedSuperclass, nor @Embeddable.
 * Expected: diagnostic AssociationOverrideOnInvalidTarget.
 */
@AssociationOverride(name = "address", joinColumns = @JoinColumn(name = "ADDR_ID"))
public class InvalidAssociationOverrideOnPlainClass {
    private String name;
}
