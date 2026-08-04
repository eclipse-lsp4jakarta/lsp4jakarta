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
 * Invalid: name="zipcode" does not exist in Address (only "street" and "city").
 * Expected: diagnostic InvalidAttributeOverrideName on the @AttributeOverride annotation.
 */
@Entity
public class InvalidEmbeddableOverride {
    @Id
    private Long id;

    @Embedded
    @AttributeOverride(name = "zipcode", column = @Column(name = "ADDR_ZIP"))
    private Address address;
}
