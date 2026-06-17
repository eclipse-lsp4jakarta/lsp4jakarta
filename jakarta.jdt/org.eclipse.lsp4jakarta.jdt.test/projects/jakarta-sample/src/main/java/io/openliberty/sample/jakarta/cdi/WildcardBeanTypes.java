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
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class WildcardBeanTypes {

    // Invalid: Wildcard type in @Inject field
    @Inject
    List<?> wildcardList;

    // Invalid: Wildcard type with extends in @Inject field
    @Inject
    List<? extends Number> extendsWildcardList;

    // Invalid: Wildcard type with super in @Inject field
    @Inject
    List<? super Integer> superWildcardList;

    // Invalid: Map with wildcard in @Inject field
    @Inject
    Map<String, ?> wildcardMap;

    // Valid: Concrete parameterized type in @Inject field
    @Inject
    List<String> concreteList;

    // Valid: Concrete parameterized type in @Inject field
    @Inject
    Map<String, Integer> concreteMap;

    // Invalid: Producer field with wildcard type
    @Produces
    List<?> producerWildcardList = null;

    // Invalid: Producer field with extends wildcard
    @Produces
    List<? extends Number> producerExtendsWildcardList = null;

    // Valid: Producer field with concrete type
    @Produces
    List<String> producerConcreteList = null;

    // Invalid: Producer method with wildcard return type
    @Produces
    public Map<String, ?> produceWildcardMap() {
        return null;
    }

    // Invalid: Producer method with extends wildcard return type
    @Produces
    public List<? extends Number> produceExtendsWildcardList() {
        return null;
    }

    // Invalid: Producer method with super wildcard return type
    @Produces
    public List<? super Integer> produceSuperWildcardList() {
        return null;
    }

    // Invalid: Producer method with array of wildcard type
    @Produces
    public List<?>[] produceWildcardArray() {
        return null;
    }

    // Invalid: Nested wildcard - Map with List containing wildcard
    @Inject
    Map<String, List<?>> nestedWildcardMap;

    // Invalid: Deeply nested wildcard - Map with Map containing wildcard
    @Inject
    Map<String, Map<Integer, ?>> deeplyNestedWildcardMap;

    // Invalid: Producer method with nested wildcard return type
    @Produces
    public Map<String, List<?>> produceNestedWildcardMap() {
        return null;
    }

    // Invalid: Array of nested wildcard type
    @Inject
    Map<String, List<?>>[] nestedWildcardArray;

    // Valid: Producer method with concrete return type
    @Produces
    public List<Double> produceConcreteList() {
        return null;
    }

    // Valid: Producer method with concrete Map
    @Produces
    public Map<String, Integer> produceConcreteMap() {
        return null;
    }

    // Valid: Nested concrete types without wildcards
    @Inject
    Map<String, List<Integer>> nestedConcreteMap;
}