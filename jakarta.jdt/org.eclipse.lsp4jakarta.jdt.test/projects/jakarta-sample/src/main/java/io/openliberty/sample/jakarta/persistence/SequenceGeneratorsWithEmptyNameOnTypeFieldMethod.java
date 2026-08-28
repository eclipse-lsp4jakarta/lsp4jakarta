package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.SequenceGenerators;

// @SequenceGenerators with a nested @SequenceGenerator(name = "") on type, field, and method —
// each nested empty name should independently trigger a diagnostic
@Entity
@SequenceGenerators({ @SequenceGenerator(name = "") })
public class SequenceGeneratorsWithEmptyNameOnTypeFieldMethod {
    @Id
    private Long id;

    @SequenceGenerators({ @SequenceGenerator(name = "") })
    private String data;

    @SequenceGenerators({ @SequenceGenerator(name = "") })
    public String getData() {
        return this.data;
    }
}
