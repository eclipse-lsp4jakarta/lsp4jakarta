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

import jakarta.persistence.Embeddable;
import java.io.Serializable;

/**
 * Embeddable composite PK for {@link OrderWithEmbeddedId}.
 * Used as the {@code @EmbeddedId} target in @IdClass EmbeddedId test scenarios.
 */
@Embeddable
public class OrderEmbeddedId implements Serializable {

    private static final long serialVersionUID = 1L;

    public int orderId;
    public String region;

    public OrderEmbeddedId() {
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof OrderEmbeddedId;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
