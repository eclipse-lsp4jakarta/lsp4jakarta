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

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;

@Entity
@NamedEntityGraph(
    name = "Author.withInvalidAttribute",
    attributeNodes = {
        @NamedAttributeNode("name"),
        @NamedAttributeNode("nonExistentField")
    }
)
public class NamedEntityGraphInvalidAttribute {

    @Id
    private Long id;

    private String name;

    public NamedEntityGraphInvalidAttribute() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
