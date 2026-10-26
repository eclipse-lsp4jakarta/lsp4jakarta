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
 * Plain intermediate class (NOT annotated with @MappedSuperclass) that extends
 * the @MappedSuperclass Person. Used for chains with a non-mapped intermediate.
 */
public abstract class PlainPerson extends Person {
}
