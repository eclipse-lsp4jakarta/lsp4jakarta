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

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

/**
 * Embeddable with a nested embeddable field (zipcode).
 * Fields: street, city, zipcode (of type Zipcode).
 * Used for dot-notation override validation tests.
 */
@Embeddable
public class AddressWithZipcode {
    private String street;
    private String city;

    @Embedded
    private Zipcode zipcode;
}
