package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.NamedQuery;

@NamedQuery(name = "User.findAll", query = "SELECT u FROM User u")
public class NamedQueryOnInvalidClass {
}
