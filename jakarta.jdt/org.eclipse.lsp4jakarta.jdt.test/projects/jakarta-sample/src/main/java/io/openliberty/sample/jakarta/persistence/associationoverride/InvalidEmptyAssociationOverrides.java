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

import jakarta.persistence.AssociationOverrides;
import jakarta.persistence.Entity;

/**
 * Invalid: @AssociationOverrides container with no nested @AssociationOverride entries.
 * Expected: diagnostic AssociationOverridesEmptyContainer.
 */
@Entity
@AssociationOverrides({})
public class InvalidEmptyAssociationOverrides extends Person {
}
