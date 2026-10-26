package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.NamedNativeQuery;

@NamedNativeQuery(name = "User.findById", query = "SELECT * FROM USER WHERE ID = ?", resultClass = NamedNativeQueryOnInvalidClass.class)
public class NamedNativeQueryOnInvalidClass {
}
