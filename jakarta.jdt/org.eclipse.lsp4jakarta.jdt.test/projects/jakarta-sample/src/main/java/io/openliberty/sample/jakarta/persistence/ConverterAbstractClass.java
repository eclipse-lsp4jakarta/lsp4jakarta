package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Converter;

// Invalid: abstract class with @Converter but no AttributeConverter implementation
@Converter
public abstract class ConverterAbstractClass {

    public abstract String process(String input);
}
