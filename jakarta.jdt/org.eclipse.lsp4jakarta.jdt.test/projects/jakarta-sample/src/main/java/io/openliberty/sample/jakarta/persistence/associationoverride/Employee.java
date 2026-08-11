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

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Simple entity used as a relationship target in association override tests.
 */
@Entity
public class Employee {
    @Id
    private Long id;
    private String name;
}
