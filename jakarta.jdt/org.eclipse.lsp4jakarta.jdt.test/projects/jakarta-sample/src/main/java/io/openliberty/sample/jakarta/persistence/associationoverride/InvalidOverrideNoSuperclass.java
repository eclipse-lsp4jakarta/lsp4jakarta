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

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Invalid: class-level @AssociationOverride on an entity that has no superclass,
 * so "supervisor" cannot be an association inherited from a @MappedSuperclass.
 * Expected: diagnostic InvalidAssociationOverrideName on the @AssociationOverride annotation.
 */
@Entity
@AssociationOverride(name = "supervisor", joinColumns = @JoinColumn(name = "SUP_ID"))
public class InvalidOverrideNoSuperclass {
    @Id
    private Long id;

    @ManyToOne
    private Employee supervisor;
}
