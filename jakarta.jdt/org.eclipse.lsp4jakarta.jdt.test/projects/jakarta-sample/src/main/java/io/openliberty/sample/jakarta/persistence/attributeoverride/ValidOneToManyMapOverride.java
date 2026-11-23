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
import jakarta.persistence.OneToMany;
import java.util.Map;

/**
 * Valid: name="key.city" on a Map-typed @OneToMany — "key." prefix present,
 * "city" exists in the key type Address.
 * Expected: no diagnostic.
 */
@Entity
public class ValidOneToManyMapOverride {
    @Id
    private Long id;

    @OneToMany
    @AttributeOverride(name = "key.city", column = @Column(name = "KEY_CITY"))
    private Map<Address, Person> byAddress;
}
