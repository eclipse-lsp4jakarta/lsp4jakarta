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
 * Valid: Extends BaseServiceWithScope (a CDI bean) AND implements an interface.
 * The direct superclass is a valid bean, so @Specializes should NOT trigger a diagnostic.
 */
@Specializes
@ApplicationScoped
public class SpecializesWithBeanSuperclassAndInterface extends BaseServiceWithScope implements BaseServiceInterface {
    @Override
    public String greet() { return "Custom Hello"; }
}
