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

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

/**
 * Related entity whose primary key is an {@code @EmbeddedId} of type
 * {@link OrderEmbeddedId}.
 * Used as the relationship target in @IdClass / @EmbeddedId test scenarios.
 */
@Entity
public class OrderWithEmbeddedId {

    @EmbeddedId
    private OrderEmbeddedId id;

    private String description;

    public OrderWithEmbeddedId() {
    }
}
