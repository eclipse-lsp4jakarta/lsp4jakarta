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
 * Invalid: MiddleServiceNoScope has no scope annotation so it is NOT a CDI bean.
 * Even though GrandparentServiceWithScope (two levels up) is scoped, only the
 * direct superclass counts per CDI spec 3.1.4.
 * @Specializes here must trigger a diagnostic error.
 */
@Specializes
@ApplicationScoped
public class SpecializesWithGrandparentBeanOnly extends MiddleServiceNoScope {
    @Override
    public String greet() { return "Custom Hello from grandchild"; }
}
