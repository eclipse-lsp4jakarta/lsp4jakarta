package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.SecondaryTables;

// @SecondaryTables with multiple nested @SecondaryTable entries where more than one has an empty
// name should report a diagnostic for each invalid nested annotation on the type
@Entity
@SecondaryTables({ @SecondaryTable(name = "   "), @SecondaryTable(name = "name1"), @SecondaryTable(name = "") })
public class SecondaryTablesWithMultipleEmptyNames {
    @Id
    private Long id;
}
