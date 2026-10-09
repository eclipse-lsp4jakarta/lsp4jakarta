package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// Valid: @Converter class that correctly implements AttributeConverter
@Converter(autoApply = true)
public class ConverterWithAttributeConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        return (attribute != null && attribute) ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        return "Y".equals(dbData);
    }
}
