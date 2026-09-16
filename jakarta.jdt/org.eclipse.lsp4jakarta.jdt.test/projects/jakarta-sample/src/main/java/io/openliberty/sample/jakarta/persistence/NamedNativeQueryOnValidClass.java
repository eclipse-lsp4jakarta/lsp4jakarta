package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.NamedNativeQuery;

@MappedSuperclass
@NamedNativeQuery(name = "Base.findById", query = "SELECT * FROM BASE WHERE ID = ?", resultClass = NamedNativeQueryOnValidClass.class)
public abstract class NamedNativeQueryOnValidClass {
    @Id
    private Long id;
}
