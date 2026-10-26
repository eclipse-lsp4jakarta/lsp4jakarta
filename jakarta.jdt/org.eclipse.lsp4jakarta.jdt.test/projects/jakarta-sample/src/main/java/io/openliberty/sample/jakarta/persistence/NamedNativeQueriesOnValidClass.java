package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedNativeQueries;
import jakarta.persistence.NamedNativeQuery;

@Entity
@NamedNativeQueries({ @NamedNativeQuery(name = "Base.findById", query = "SELECT * FROM BASE WHERE ID = ?", resultClass = NamedNativeQueriesOnValidClass.class) })
public class NamedNativeQueriesOnValidClass {
    @Id
    private Long id;
}
