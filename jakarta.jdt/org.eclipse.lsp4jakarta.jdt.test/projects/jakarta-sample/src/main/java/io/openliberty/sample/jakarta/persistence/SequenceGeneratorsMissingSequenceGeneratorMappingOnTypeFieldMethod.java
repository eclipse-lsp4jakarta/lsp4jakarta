package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerators;

// @SequenceGenerators with empty arrays on type, field, and method simultaneously —
// each occurrence should independently trigger a diagnostic
@Entity
@SequenceGenerators({})
public class SequenceGeneratorsMissingSequenceGeneratorMappingOnTypeFieldMethod {
    @Id
    private Long id;

    @SequenceGenerators({})
    private String data;

    @SequenceGenerators({})
    public String getData() {
        return this.data;
    }
}
