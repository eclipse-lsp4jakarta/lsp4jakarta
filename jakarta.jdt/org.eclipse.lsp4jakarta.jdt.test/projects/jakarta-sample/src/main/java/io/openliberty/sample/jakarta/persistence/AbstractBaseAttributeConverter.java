package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.AttributeConverter;

// Abstract base that implements AttributeConverter — used to test inherited implementation.
public abstract class AbstractBaseAttributeConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        return (attribute != null && attribute) ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        return "Y".equals(dbData);
    }
}
