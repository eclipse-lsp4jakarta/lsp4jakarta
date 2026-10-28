package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Valid @Convert usages — no diagnostics expected.
 */
@Entity
public class ConvertAnnotationValid {

    @Id
    private Long id;

    // Valid: converter class specified
    @Convert(converter = Object.class)
    private Boolean active;

    // Valid: disableConversion = true
    @Convert(disableConversion = true)
    private String rawData;
}
