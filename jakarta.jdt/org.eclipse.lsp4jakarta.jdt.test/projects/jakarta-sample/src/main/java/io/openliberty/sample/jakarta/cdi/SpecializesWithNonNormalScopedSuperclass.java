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

import jakarta.enterprise.inject.Specializes;

/**
 * Invalid: NotAScopedBean is annotated with @NotAScope, which is a plain annotation
 * not meta-annotated with @NormalScope. It is therefore NOT a CDI bean.
 * @Specializes here should trigger a diagnostic error.
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#direct_and_indirect_specialization">CDI 3.0 §4.3</a>
 */
@Specializes
public class SpecializesWithNonNormalScopedSuperclass extends NotAScopedBean {
    @Override
    public String greet() {
        return "Hello from SpecializesWithNonNormalScopedSuperclass";
    }
}
