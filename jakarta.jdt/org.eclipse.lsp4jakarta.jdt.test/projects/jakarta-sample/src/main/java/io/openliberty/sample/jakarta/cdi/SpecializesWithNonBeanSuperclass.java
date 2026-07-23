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
 * Invalid: BaseServiceNoScope has no scope annotation, so it is not a CDI bean.
 * @Specializes here should trigger a diagnostic error.
 */
@Specializes
@ApplicationScoped
public class SpecializesWithNonBeanSuperclass extends BaseServiceNoScope {
    @Override
    public String greet() { return "Custom Hello"; }
}
