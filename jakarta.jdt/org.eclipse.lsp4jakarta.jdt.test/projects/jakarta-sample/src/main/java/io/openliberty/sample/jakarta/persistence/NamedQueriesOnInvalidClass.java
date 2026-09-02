package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;

@NamedQueries({ @NamedQuery(name = "User.findAll", query = "SELECT u FROM User u") })
public class NamedQueriesOnInvalidClass {
}
