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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Test resource for CDI producer method and field type variable diagnostics.
 *
 * CDI 3.0 spec sections 3.2 (producer methods) and 3.3 (producer fields):
 *
 * Rule 1 — bare type variable is always a definition error:
 *   "If a producer method/field return type is a type variable or an array type whose
 *    component type is a type variable the container automatically detects the problem
 *    and treats it as a definition error."
 *
 * Rule 2 — parameterized type with type variable requires @Dependent scope:
 *   "If the producer method/field return type is a parameterized type with a type variable,
 *    it must have scope @Dependent. If it declares any scope other than @Dependent, the
 *    container automatically detects the problem and treats it as a definition error."
 */
@Dependent
public class ProducerMethodTypeVariable<T> {

    // -----------------------------------------------------------------------
    // Producer Methods — Rule 2 violations
    // -----------------------------------------------------------------------

    // Invalid: @Produces method returns List<T> with @ApplicationScoped
    @Produces
    @ApplicationScoped
    public List<T> produceListWithApplicationScope() {
        return new ArrayList<>();
    }

    // Invalid: @Produces method returns Map<String, T> with @RequestScoped
    @Produces
    @RequestScoped
    public Map<String, T> produceMapWithRequestScope() {
        return new HashMap<>();
    }

    // -----------------------------------------------------------------------
    // Producer Methods — Rule 2 valid
    // -----------------------------------------------------------------------

    // Valid: @Produces method returns List<T> with @Dependent
    @Produces
    @Dependent
    public List<T> produceListWithDependentScope() {
        return new ArrayList<>();
    }

    // Valid: @Produces method with type variable and no explicit scope (implicitly @Dependent)
    @Produces
    public List<T> produceListWithNoScope() {
        return new ArrayList<>();
    }

    // -----------------------------------------------------------------------
    // Producer Methods — Rule 1 violations
    // -----------------------------------------------------------------------

    // Invalid: @Produces method with bare type variable return type T
    @Produces
    public T produceBareTypeVariable() {
        return null;
    }

    // Invalid: @Produces method with array of type variable return type T[]
    @Produces
    public T[] produceBareTypeVariableArray() {
        return null;
    }

    // -----------------------------------------------------------------------
    // Producer Methods — no violation (concrete type, no type variable)
    // -----------------------------------------------------------------------

    // Valid: @Produces method returns concrete List<String> — any scope is fine
    @Produces
    @ApplicationScoped
    public List<String> produceConcreteList() {
        return new ArrayList<>();
    }

    // -----------------------------------------------------------------------
    // Producer Fields — Rule 2 violations
    // -----------------------------------------------------------------------

    // Invalid: @Produces field of type List<T> with @ApplicationScoped
    @Produces
    @ApplicationScoped
    List<T> producerFieldListWithApplicationScope = new ArrayList<>();

    // Invalid: @Produces field of type Map<String, T> with @RequestScoped
    @Produces
    @RequestScoped
    Map<String, T> producerFieldMapWithRequestScope = new HashMap<>();

    // -----------------------------------------------------------------------
    // Producer Fields — Rule 2 valid
    // -----------------------------------------------------------------------

    // Valid: @Produces field of type List<T> with @Dependent
    @Produces
    @Dependent
    List<T> producerFieldListWithDependentScope = new ArrayList<>();

    // Valid: @Produces field with type variable and no explicit scope (implicitly @Dependent)
    @Produces
    List<T> producerFieldListWithNoScope = new ArrayList<>();

    // -----------------------------------------------------------------------
    // Producer Fields — Rule 1 violations
    // -----------------------------------------------------------------------

    // Invalid: @Produces field of bare type variable T
    @Produces
    T producerFieldBareTypeVariable = null;

    // Invalid: @Produces field of array of type variable T[]
    @Produces
    T[] producerFieldBareTypeVariableArray = null;

    // -----------------------------------------------------------------------
    // Producer Fields — no violation (concrete type, no type variable)
    // -----------------------------------------------------------------------

    // Valid: @Produces field of concrete type List<String> — any scope is fine
    @Produces
    @ApplicationScoped
    List<String> producerConcreteField = new ArrayList<>();
}
