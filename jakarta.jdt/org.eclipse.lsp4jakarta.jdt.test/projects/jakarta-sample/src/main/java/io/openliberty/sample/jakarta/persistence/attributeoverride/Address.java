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

/**
 * Embeddable with two fields: street and city.
 * Used as the target type for @AttributeOverride validation tests.
 */
@Embeddable
public class Address {
    private String street;
    private String city;
}
