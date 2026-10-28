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
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/**
 * Valid: @AttributeOverrides container — both "address" and "id" exist in Person.
 * Expected: no diagnostic.
 */
@Entity
@AttributeOverrides({
    @AttributeOverride(name = "address", column = @Column(name = "MGR_ADDR")),
    @AttributeOverride(name = "id",      column = @Column(name = "MGR_ID"))
})
public class ValidContainerOverride extends Person {
}
