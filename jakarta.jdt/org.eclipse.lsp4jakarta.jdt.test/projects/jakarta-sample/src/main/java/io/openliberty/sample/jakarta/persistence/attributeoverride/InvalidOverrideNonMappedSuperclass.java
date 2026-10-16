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
 * Invalid: class-level @AttributeOverride with name="city" which exists in PlainBase,
 * but PlainBase is not annotated with @MappedSuperclass.
 * Expected: diagnostic InvalidAttributeOverrideName on the @AttributeOverride annotation.
 */
@Entity
@AttributeOverride(name = "city", column = @Column(name = "EMP_CITY"))
public class InvalidOverrideNonMappedSuperclass extends PlainBase {
    @Id
    private Long id;
}
