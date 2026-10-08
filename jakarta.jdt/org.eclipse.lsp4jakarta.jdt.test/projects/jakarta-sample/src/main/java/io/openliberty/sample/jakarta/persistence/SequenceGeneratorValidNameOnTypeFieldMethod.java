package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

// @SequenceGenerator with a non-empty 'name' on type, field, and method —
// no diagnostic should be reported for any of these occurrences
@Entity
@SequenceGenerator(name = "typeSeq")
public class SequenceGeneratorValidNameOnTypeFieldMethod {
    @Id
    private Long id;

    @SequenceGenerator(name = "fieldSeq")
    private String data;

    @SequenceGenerator(name = "methodSeq")
    public String getData() {
        return this.data;
    }
}
