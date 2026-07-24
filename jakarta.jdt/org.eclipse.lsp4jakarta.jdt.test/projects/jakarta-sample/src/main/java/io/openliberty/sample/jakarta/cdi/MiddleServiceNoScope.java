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
 * A middle class that extends GrandparentServiceWithScope but has NO scope annotation.
 * This class is NOT a valid CDI bean by itself.
 * Used as the direct superclass in the grandparent-only scope test.
 */
public class MiddleServiceNoScope extends GrandparentServiceWithScope {
    @Override
    public String greet() { return "Hello from middle"; }
}
