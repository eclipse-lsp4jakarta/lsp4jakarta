package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.NamedNativeQueries;
import jakarta.persistence.NamedNativeQuery;

@NamedNativeQueries({ @NamedNativeQuery(name = "User.findById", query = "SELECT * FROM USER WHERE ID = ?", resultClass = NamedNativeQueriesOnInvalidClass.class) })
public class NamedNativeQueriesOnInvalidClass {
}
