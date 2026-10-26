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
import jakarta.persistence.Id;

/**
 * Invalid: @AttributeOverrides container on a plain Long @Id field that is not
 * annotated with @Embedded, @EmbeddedId, or @ElementCollection.
 * Expected: diagnostic AttributeOverrideOnNonEmbeddedField on the @AttributeOverrides annotation.
 */
@Entity
public class InvalidAttributeOverrideOnIdField {
    @Id
    @AttributeOverrides({
        @AttributeOverride(name = "name", column = @Column(name = "EMP_NAME"))
    })
    private Long id;
}
