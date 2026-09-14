package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.SequenceGenerators;

// @SequenceGenerators where all nested @SequenceGenerator entries have non-empty names on type,
// field, and method — no diagnostic should be reported for any of these occurrences
@Entity
@SequenceGenerators({ @SequenceGenerator(name = "typeSeq1"), @SequenceGenerator(name = "typeSeq2") })
public class SequenceGeneratorsValidNamesOnTypeFieldMethod {
    @Id
    private Long id;

    @SequenceGenerators({ @SequenceGenerator(name = "fieldSeq1"), @SequenceGenerator(name = "fieldSeq2") })
    private String data;

    @SequenceGenerators({ @SequenceGenerator(name = "methodSeq1"), @SequenceGenerator(name = "methodSeq2") })
    public String getData() {
        return this.data;
    }
}
