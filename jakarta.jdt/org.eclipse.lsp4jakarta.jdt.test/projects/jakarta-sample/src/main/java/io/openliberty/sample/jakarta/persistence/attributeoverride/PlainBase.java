/*******************************************************************************
* Copyright (c) 2026 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package io.openliberty.sample.jakarta.persistence.attributeoverride;

/**
 * Plain superclass (NOT annotated with @MappedSuperclass) with field: city.
 * Used to verify that class-level @AttributeOverride only resolves against
 * attributes inherited from a @MappedSuperclass.
 */
public abstract class PlainBase {
    protected String city;
}
