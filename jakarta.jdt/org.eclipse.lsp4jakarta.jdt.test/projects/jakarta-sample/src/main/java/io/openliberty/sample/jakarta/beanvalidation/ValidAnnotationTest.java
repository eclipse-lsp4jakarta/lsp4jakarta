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
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package io.openliberty.sample.jakarta.beanvalidation;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

public class ValidAnnotationTest {
    
    // Invalid cases - should trigger diagnostics
    @Valid
    private int primitiveInt; // Error: primitive type
    
    @Valid
    private Integer boxedInteger; // Error: boxed type
    
    @Valid
    private String stringField; // Error: String type
    
    @Valid
    private Double boxedDouble; // Error: boxed type
    
    @Valid
    private java.math.BigDecimal bigDecimal; // Error: BigDecimal
    
    // Valid cases - should NOT trigger diagnostics
    @Valid
    private Product product; // OK: complex object
    
    @Valid
    private List<Product> products; // OK: collection
    
    @Valid
    private Product[] productArray; // OK: array
    
    @Valid
    private Map<String, Product> productMap; // OK: map
    
    // Test on methods
    @Valid
    public int getInvalidMethod() { // Error: returns primitive
        return 0;
    }
    
    @Valid
    public Product getValidMethod() { // OK: returns complex object
        return null;
    }
    
    // Test on parameters
    public void invalidParam(@Valid int param) { // Error: primitive parameter
    }
    
    public void validParam(@Valid Product param) // OK: complex object parameter
    {
    }
    
    static class Product {
        private String name;
        private int price;
    }
}