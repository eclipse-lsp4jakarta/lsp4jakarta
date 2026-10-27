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
import jakarta.persistence.JoinTable;

/**
 * Valid: @AssociationOverride specifies joinTable only (no joinColumns).
 * Expected: no diagnostic.
 */
@Entity
@AssociationOverride(
    name      = "supervisor",
    joinTable = @JoinTable(name = "EMP_MGR")
)
public class ValidJoinTableOnly extends Person {
}
