/*******************************************************************************
* Copyright (c) 2026 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package io.openliberty.sample.jakarta.persistence.attributeoverride;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Valid: @AttributeOverride on a property-based (getter) @Embedded accessor
 * with name="city" which exists in Address.
 * Expected: no diagnostic.
 */
@Entity
public class ValidPropertyBasedOverride {
    @Id
    private Long id;
    private Address address;

    @Embedded
    @AttributeOverride(name = "city", column = @Column(name = "ADDR_CITY"))
    public Address getAddress() {
        return address;
    }
}
