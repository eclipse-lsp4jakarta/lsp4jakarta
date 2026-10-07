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

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;

/**
 * Embeddable with two relationship fields: manager and lead.
 * Used as the target type for @AssociationOverride validation tests.
 */
@Embeddable
public class Department {
    @ManyToOne
    private Employee manager;

    @ManyToOne
    private Employee lead;
}
