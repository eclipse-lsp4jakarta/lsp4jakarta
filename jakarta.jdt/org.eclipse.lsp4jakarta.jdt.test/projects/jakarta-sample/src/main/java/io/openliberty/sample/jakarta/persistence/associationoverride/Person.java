/*******************************************************************************
* Copyright (c) 2026 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package io.openliberty.sample.jakarta.persistence.associationoverride;

import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

/**
 * MappedSuperclass with relationship fields: id (scalar) and supervisor (association).
 * Used as the superclass target for @AssociationOverride validation tests.
 */
@MappedSuperclass
public abstract class Person {
    @Id
    protected Long id;

    @ManyToOne
    protected Employee supervisor;
}
