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
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Invalid: class-level @AttributeOverride on an entity that has no superclass,
 * so "city" cannot be an attribute inherited from a @MappedSuperclass.
 * Expected: diagnostic InvalidAttributeOverrideName on the @AttributeOverride annotation.
 */
@Entity
@AttributeOverride(name = "city", column = @Column(name = "EMP_CITY"))
public class InvalidOverrideNoSuperclass {
    @Id
    private Long id;
    private String city;
}
