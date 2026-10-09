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
 * Invalid: container with "id" (valid) and "bonus" (invalid — not in Person).
 * Expected: diagnostic on "bonus" entry only.
 */
@Entity
@AttributeOverrides({
    @AttributeOverride(name = "id",    column = @Column(name = "MGR_ID")),
    @AttributeOverride(name = "bonus", column = @Column(name = "MGR_BONUS"))
})
public class InvalidContainerOneEntry extends Person {
}
