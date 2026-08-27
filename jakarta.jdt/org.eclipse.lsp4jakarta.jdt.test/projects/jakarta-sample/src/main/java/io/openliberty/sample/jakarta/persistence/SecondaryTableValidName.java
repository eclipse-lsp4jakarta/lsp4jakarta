package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SecondaryTable;

// @SecondaryTable with a non-empty 'name' on the type —
// no diagnostic should be reported
@Entity
@SecondaryTable(name = "secondary")
public class SecondaryTableValidName {
    @Id
    private Long id;
}
