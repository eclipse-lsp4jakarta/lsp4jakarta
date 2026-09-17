package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SecondaryTables;

// @SecondaryTables with an empty array should trigger a diagnostic
@Entity
@SecondaryTables({})
public class SecondaryTablesMissingSecondaryTableMapping {
    @Id
    private Long id;
}
