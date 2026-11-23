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
import java.util.List;

/**
 * Invalid: @AttributeOverride on a @OneToMany whose type is a List, not a Map.
 * Only Map-typed @OneToMany / @ManyToMany members are valid override targets.
 * Expected: diagnostic AttributeOverrideOnNonEmbeddedField on the @AttributeOverride annotation.
 */
@Entity
public class InvalidOneToManyListOverride {
    @Id
    private Long id;

    @OneToMany
    @AttributeOverride(name = "city", column = @Column(name = "EMP_CITY"))
    private List<Person> people;
}
