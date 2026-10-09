package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Converter;

// Invalid: plain class with @Converter but no AttributeConverter implementation
@Converter
public class ConverterWithoutAttributeConverter {

    public String helperMethod(String input) {
        return input.toUpperCase();
    }
}
