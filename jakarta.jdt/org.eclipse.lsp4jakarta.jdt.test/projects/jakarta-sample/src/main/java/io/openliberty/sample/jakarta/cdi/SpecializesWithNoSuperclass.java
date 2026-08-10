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
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;

/**
 * Invalid: No superclass declared. @Specializes requires directly extending
 * a bean class, so this should trigger a diagnostic error.
 */
@Specializes
@ApplicationScoped
public class SpecializesWithNoSuperclass {
    public String greet() { return "Custom Hello"; }
}
