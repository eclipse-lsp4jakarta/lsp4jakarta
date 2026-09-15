package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.NamedQuery;

@MappedSuperclass
@NamedQuery(name = "User.findAll", query = "SELECT u FROM User u")
public class NamedQueryOnValidClass {
    @Id
    private Long id;
}
