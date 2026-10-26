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

import java.io.Serializable;

/**
 * Key class for {@link IdClassOneToOneTypeMismatch}: holds String dept instead of int,
 * which mismatches the parent entity's PK type.
 */
public class IdClassOneToOneTypeMismatchPK implements Serializable {

    private static final long serialVersionUID = 1L;

    public String empName;
    public String dept;

    public IdClassOneToOneTypeMismatchPK() {
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof IdClassOneToOneTypeMismatchPK;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
