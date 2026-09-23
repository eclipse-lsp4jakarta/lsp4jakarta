package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.SecondaryTables;

// @SecondaryTables with a nested @SecondaryTable that has empty 'name' should trigger a diagnostic
@Entity
@SecondaryTables({
    @SecondaryTable(name = "")
})
public class SecondaryTablesWithEmptyName {
    @Id
    private Long id;
}
