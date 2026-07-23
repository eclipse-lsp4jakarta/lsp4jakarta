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
 * Valid: BaseServiceWithScope is annotated with @ApplicationScoped (a valid CDI bean).
 * @Specializes here should NOT trigger a diagnostic.
 */
@Specializes
@ApplicationScoped
public class SpecializesWithBeanSuperclass extends BaseServiceWithScope {
    @Override
    public String greet() { return "Custom Hello"; }
}
