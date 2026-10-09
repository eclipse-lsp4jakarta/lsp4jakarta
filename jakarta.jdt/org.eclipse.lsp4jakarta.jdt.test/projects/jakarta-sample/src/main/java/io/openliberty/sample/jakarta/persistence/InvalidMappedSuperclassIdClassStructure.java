package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
@IdClass(InvalidMappedSuperclassIdClass.class)
public class InvalidMappedSuperclassIdClassStructure {

    @Id
    private String firstName;

    @Id
    private String lastName;
}

class InvalidMappedSuperclassIdClass {

    private InvalidMappedSuperclassIdClass(String value) {
    }
}
