package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.SecondaryTables;

// @SecondaryTables where all nested @SecondaryTable entries have non-empty names on the type —
// no diagnostic should be reported
@Entity
@SecondaryTables({ @SecondaryTable(name = "sec1"), @SecondaryTable(name = "sec2") })
public class SecondaryTablesValidNames {
    @Id
    private Long id;
}
