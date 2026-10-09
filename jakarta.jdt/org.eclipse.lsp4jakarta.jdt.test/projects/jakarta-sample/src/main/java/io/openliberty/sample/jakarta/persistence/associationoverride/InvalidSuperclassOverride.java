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
import jakarta.persistence.JoinColumn;

/**
 * Invalid: name="mentor" does not exist in Person (only "supervisor" and "id").
 * Expected: diagnostic InvalidAssociationOverrideName on the class-level @AssociationOverride.
 */
@Entity
@AssociationOverride(name = "mentor", joinColumns = @JoinColumn(name = "MENTOR_ID"))
public class InvalidSuperclassOverride extends Person {
}
