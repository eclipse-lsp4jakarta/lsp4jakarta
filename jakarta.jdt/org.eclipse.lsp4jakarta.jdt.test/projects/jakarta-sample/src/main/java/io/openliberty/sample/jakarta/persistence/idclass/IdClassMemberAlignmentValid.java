/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.sample.jakarta.persistence.idclass;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

/**
 * Valid @IdClass usage: key class members match entity @Id fields by name and type.
 * Expects no diagnostics.
 */
@Entity
@IdClass(IdClassMemberAlignmentValidPK.class)
public class IdClassMemberAlignmentValid {

    @Id
    private String firstName;

    @Id
    private String lastName;

    private String department;

    public IdClassMemberAlignmentValid() {
    }
}
