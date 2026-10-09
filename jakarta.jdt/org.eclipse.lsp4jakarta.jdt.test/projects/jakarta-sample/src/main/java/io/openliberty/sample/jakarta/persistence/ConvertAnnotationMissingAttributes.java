package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Invalid @Convert usages - missing converter or disableConversion=true.
 */
@Entity
public class ConvertAnnotationMissingAttributes {

    @Id
    private Long id;

    // Invalid: no converter or disableConversion specified
    @Convert
    private String data;

    // Invalid: disableConversion explicitly false, no converter specified
    @Convert(disableConversion = false)
    private String status;
}
