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

import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Test resource for CDI §2.2.1 legal bean type diagnostics:
 * a bare type variable (T or T[]) is not a legal bean type at injection points.
 *
 * Expected diagnostics:
 * - Line 33: @Inject field of type T          → InvalidBareTypeVariableInInjectField
 * - Line 37: @Inject field of type T[]        → InvalidBareTypeVariableInInjectField
 * - Line 43: @Inject method param of type T   → InvalidBareTypeVariableInInjectMethodParam
 * - Line 47: @Inject method param of type T[] → InvalidBareTypeVariableInInjectMethodParam
 *
 * No diagnostic expected on:
 * - Line 54: @Inject field of type List<T>    (parameterized type with type variable — different rule, @Dependent OK)
 * - Line 58: plain String @Inject field       (concrete type — legal)
 */
@Dependent
public class BareTypeVariableInjectBean<T> {

    // Invalid: bare type variable T as @Inject field type
    @Inject
    T bareTypeField;

    // Invalid: array of bare type variable T[] as @Inject field type
    @Inject
    T[] bareTypeArrayField;

    // Invalid: bare type variable T as @Inject method parameter
    @Inject
    public void setBareType(T value) {
    }

    // Invalid: array of bare type variable T[] as @Inject method parameter
    @Inject
    public void setBareTypeArray(T[] values) {
    }

    // Valid: parameterized type with type variable (List<T>)
    @Inject
    List<T> genericList;

    // Valid: concrete type — no diagnostic
    @Inject
    String concreteField;
}
