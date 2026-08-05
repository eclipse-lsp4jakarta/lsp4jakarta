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
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.Map;

/**
 * Invalid: name="city" on a Map @ElementCollection without required "key." or "value." prefix.
 * Expected: diagnostic InvalidAttributeOverrideName on the @AttributeOverride annotation.
 */
@Entity
public class InvalidMapMissingPrefix {
    @Id
    private Long id;

    @ElementCollection
    @AttributeOverride(name = "city", column = @Column(name = "PROP_CITY"))
    private Map<String, Address> locations;
}
