package io.openliberty.sample.jakarta.persistence;

// Intentionally NOT annotated with @Embeddable — triggers the diagnostic
// when an @Embedded field references this type.
public class EmbeddedAddress {
    private String street;
    private String city;
}
