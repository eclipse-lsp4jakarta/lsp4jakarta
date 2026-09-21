package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Entity
@IdClass(InvalidIdClass.class)
public class InvalidIdClassStructure {

    @Id
    private String firstName;

    @Id
    private String lastName;

    public InvalidIdClassStructure() {
    }
}

class InvalidIdClass {

    private InvalidIdClass(String value) {
    }
}
