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
import jakarta.persistence.ManyToOne;

/**
 * Invalid @IdClass usage with a relationship @Id field (spec §2.4.1.1, third bullet).
 * The entity has a @ManyToOne @Id field whose parent entity (Department) has a simple
 * int primary key. The key class holds String for that field instead of int.
 * Expects:
 *   - IdClassMemberTypeMismatch on @Id field "dept"
 */
@Entity
@IdClass(IdClassManyToOneTypeMismatchPK.class)
public class IdClassManyToOneTypeMismatch {

    @Id
    private String empName;

    @Id
    @ManyToOne
    private Department dept;

    public IdClassManyToOneTypeMismatch() {
    }
}
