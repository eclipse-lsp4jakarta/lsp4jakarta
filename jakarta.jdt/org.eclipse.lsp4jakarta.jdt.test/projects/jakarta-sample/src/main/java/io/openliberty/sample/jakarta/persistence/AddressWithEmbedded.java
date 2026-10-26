package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Embeddable;

// Correctly annotated with @Embeddable — used to verify the valid case
// where an @Embedded field references a properly annotated embeddable type.
@Embeddable
public class AddressWithEmbedded {
    private String street;
    private String city;
}
