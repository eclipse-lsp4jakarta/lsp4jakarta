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
 * Valid @IdClass usage where the relationship @Id field points to an entity
 * ({@link OrderWithEmbeddedId}) that uses {@code @EmbeddedId} (spec §2.4.1.1,
 * third bullet). The key class must hold the embeddable type
 * ({@link OrderEmbeddedId}) for the "order" field — which it does.
 * Expects no type-mismatch diagnostics.
 */
@Entity
@IdClass(IdClassManyToOneEmbeddedIdValidPK.class)
public class IdClassManyToOneEmbeddedIdValid {

    @Id
    private String empName;

    @Id
    @ManyToOne
    private OrderWithEmbeddedId order;

    public IdClassManyToOneEmbeddedIdValid() {
    }
}
