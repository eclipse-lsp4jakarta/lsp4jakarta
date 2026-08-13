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

/**
 * A bean annotated with a custom normal scope (@CustomNormalScope).
 * Used as a superclass to verify that @Specializes accepts custom-scoped beans.
 */
@CustomNormalScope
public class CustomScopedBean {
    public String greet() {
        return "Hello from CustomScopedBean";
    }
}
