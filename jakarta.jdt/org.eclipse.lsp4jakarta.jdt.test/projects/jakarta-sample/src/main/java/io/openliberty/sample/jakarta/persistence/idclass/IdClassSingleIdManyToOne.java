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
 * Derived identity with a single @Id (spec §2.4.1): the only @Id is a @ManyToOne
 * whose parent (Department) has an int primary key. The key class holds String.
 * Expects:
 *   - IdClassMemberTypeMismatch on @Id field "dept"
 */
@Entity
@IdClass(IdClassSingleIdManyToOnePK.class)
public class IdClassSingleIdManyToOne {

    @Id
    @ManyToOne
    private Department dept;

    public IdClassSingleIdManyToOne() {
    }
}
