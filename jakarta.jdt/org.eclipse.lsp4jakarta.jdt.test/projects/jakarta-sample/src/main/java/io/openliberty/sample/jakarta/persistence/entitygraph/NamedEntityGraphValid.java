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
package io.openliberty.sample.jakarta.persistence.entitygraph;

import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;

@Entity
@NamedEntityGraph(
    name = "Order.validGraph",
    attributeNodes = {
        @NamedAttributeNode("items"),
        @NamedAttributeNode("total")
    }
)
public class NamedEntityGraphValid {

    @Id
    private Long id;

    private List<String> items;

    private Double total;

    public NamedEntityGraphValid() {
    }

    public Long getId() {
        return id;
    }

    public List<String> getItems() {
        return items;
    }

    public Double getTotal() {
        return total;
    }
}
