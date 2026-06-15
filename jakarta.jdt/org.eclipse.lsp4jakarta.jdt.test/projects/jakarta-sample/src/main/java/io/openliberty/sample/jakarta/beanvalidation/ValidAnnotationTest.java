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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ValidAnnotationTest {

    // ========== INVALID CASES - Non-cascadable types (should trigger diagnostics) ==========

    // Primitive type
    @Valid
    private int primitiveIntField;

    // Boxed types
    @Valid
    private Integer boxedIntegerField;
    @Valid
    private Byte boxedByteField;
    @Valid
    private Short boxedShortField;
    @Valid
    private Long boxedLongField;
    @Valid
    private Float boxedFloatField;
    @Valid
    private Character boxedCharacterField;
    @Valid
    private Boolean boxedBooleanField;
    @Valid
    private Double boxedDoubleField;

    // String
    @Valid
    private String stringField;

    // BigDecimal and BigInteger
    @Valid
    private BigInteger bigIntegerField;
    @Valid
    private BigDecimal bigDecimalField;

    // Date types
    @Valid
    private Date dateField;
    @Valid
    private LocalDate localDateField;

    // UUID, URI, URL
    @Valid
    private java.util.UUID uuidField;
    @Valid
    private java.net.URI uriField;
    @Valid
    private java.net.URL urlField;

    // Enum
    @Valid
    private Status enumField;

    // Primitive arrays
    @Valid
    private int[] primitiveIntArray;
    @Valid
    private boolean[] primitiveBooleanArray;
    @Valid
    private double[] primitiveDoubleArray;

    // ========== VALID CASES - Cascadable types (should NOT trigger diagnostics) ==========

    // Complex object
    @Valid
    private Product product;

    // Collection
    @Valid
    private List<Product> products;

    // Object array
    @Valid
    private Product[] productArray;

    // Map
    @Valid
    private Map<String, Product> productMap;

    // ========== METHODS ==========

    // Invalid return types
    @Valid
    public int invalidPrimitiveMethod() {
        return 0;
    }

    @Valid
    public Long invalidBoxedMethod() {
        return 0L;
    }

    @Valid
    public int[] invalidPrimitiveArrayMethod() {
        return null;
    }

    // Valid return type
    @Valid
    public Product validMethod() {
        return null;
    }

    // Valid return type - collection
    @Valid
    public List<Product> validCollectionMethod() {
        return null;
    }

    // ========== PARAMETERS ==========

    // Invalid parameters
    public void invalidPrimitiveParam(@Valid int param) {
    }

    public void invalidBoxedParam(@Valid Integer param) {
    }

    public void invalidPrimitiveArrayParam(@Valid int[] param) {
    }

    // Valid parameters
    public void validParam(@Valid Product param) {
    }

    public void validCollectionParam(@Valid List<Product> param) {
    }
}

enum Status {
    ACTIVE, INACTIVE
}

class Product {
    private String name;
}