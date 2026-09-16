package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SecondaryTable;

// @SecondaryTable with empty 'name' attribute should trigger a diagnostic
@Entity
@SecondaryTable(name = "")
public class SecondaryTableEmptyName {
    @Id
    private Long id;
}
