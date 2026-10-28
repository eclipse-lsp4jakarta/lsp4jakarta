package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Invalid: multiple @Convert annotations on the same field.
 */
@Entity
public class ConvertAnnotationMultiple {

    @Id
    private Long id;

    // Invalid: two @Convert annotations on the same field
    @Convert(converter = Object.class)
    @Convert(converter = Object.class)
    private String status;
}
