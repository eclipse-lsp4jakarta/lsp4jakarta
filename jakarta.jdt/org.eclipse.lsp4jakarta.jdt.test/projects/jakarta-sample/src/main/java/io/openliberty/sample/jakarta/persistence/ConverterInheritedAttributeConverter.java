package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Converter;

// Valid: @Converter class that inherits AttributeConverter from a superclass — no diagnostic expected.
@Converter(autoApply = true)
public class ConverterInheritedAttributeConverter extends AbstractBaseAttributeConverter {
}
