package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

/**
 * Invalid: @Convert applied to @Id and @Version fields.
 */
@Entity
public class ConvertAnnotationOnRestrictedTarget {

    // Invalid: @Convert on @Id field
    @Id
    @Convert(converter = Object.class)
    private Long id;

    // Invalid: @Convert on @Version field
    @Version
    @Convert(converter = Object.class)
    private int version;

    private String name;
}
