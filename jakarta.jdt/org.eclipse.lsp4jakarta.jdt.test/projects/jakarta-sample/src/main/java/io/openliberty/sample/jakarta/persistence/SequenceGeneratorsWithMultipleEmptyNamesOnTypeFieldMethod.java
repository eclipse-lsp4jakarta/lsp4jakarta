package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.SequenceGenerators;

// @SequenceGenerators with multiple nested @SequenceGenerator entries where more than one has an empty
// name should report a diagnostic for each invalid nested annotation on type, field, and method
@Entity
@SequenceGenerators({ @SequenceGenerator(name = "   "), @SequenceGenerator(name = "name1"), @SequenceGenerator(name = "") })
public class SequenceGeneratorsWithMultipleEmptyNamesOnTypeFieldMethod {
    @Id
    private Long id;

    @SequenceGenerators({ @SequenceGenerator(name = "   "), @SequenceGenerator(name = "name1"), @SequenceGenerator(name = "") })
    private String data;

    @SequenceGenerators({ @SequenceGenerator(name = "   "), @SequenceGenerator(name = "name1"), @SequenceGenerator(name = "") })
    public String getData() {
        return this.data;
    }
}
