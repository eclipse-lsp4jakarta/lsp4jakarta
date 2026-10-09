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
 * Nested embeddable with a single association field: coordinator.
 * Used as the nested type in dot-notation @AssociationOverride validation tests.
 */
@Embeddable
public class SubTeam {
    @ManyToOne
    private Employee coordinator;
}
