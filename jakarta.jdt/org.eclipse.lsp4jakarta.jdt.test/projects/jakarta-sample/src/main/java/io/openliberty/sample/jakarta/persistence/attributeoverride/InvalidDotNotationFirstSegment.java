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
 * Invalid: dot-notation "location.zip" — first segment "location" does not exist
 * in AddressWithZipcode at all.
 * Expected: diagnostic InvalidAttributeOverrideName on the @AttributeOverride annotation.
 */
@Entity
public class InvalidDotNotationFirstSegment {
    @Id
    private Long id;

    @Embedded
    @AttributeOverride(name = "location.zip", column = @Column(name = "LOC_ZIP"))
    private AddressWithZipcode address;
}
