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

/**
 * Negative test resource: all @Inject fields, @Inject method parameters,
 * @Produces fields, and @Produces methods use concrete parameterized types
 * without wildcards. No diagnostics should be reported for this class.
 */
@ApplicationScoped
public class ValidWildcardBeanTypes {

    // Valid: concrete parameterized types in @Inject fields
    @Inject
    List<String> concreteList;

    @Inject
    List<Integer> intList;

    @Inject
    Map<String, Integer> concreteMap;

    @Inject
    Map<String, List<Integer>> nestedConcreteMap;

    // Valid: raw type (not a parameterized type — no wildcard check applies)
    @Inject
    String simpleType;

    // Valid: producer fields with concrete parameterized types
    @Produces
    List<String> producerConcreteList = null;

    @Produces
    Map<String, Integer> producerConcreteMap = null;

    // Valid: producer methods with concrete return types
    @Produces
    public List<Double> produceConcreteList() {
        return null;
    }

    @Produces
    public Map<String, Integer> produceConcreteMap() {
        return null;
    }

    @Produces
    public Map<String, List<Number>> produceNestedConcreteMap() {
        return null;
    }

    // Valid: @Inject methods with concrete parameterized parameter types
    @Inject
    public void setConcreteList(List<String> list) {
    }

    @Inject
    public void setConcreteMap(Map<String, Integer> map) {
    }

    @Inject
    public void setMultipleConcreteParams(List<String> list, Map<String, Integer> map) {
    }

    @Inject
    public void setNestedConcreteParam(Map<String, List<Number>> map) {
    }
}
