package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

// @SequenceGenerator with empty 'name' on type, field, and method simultaneously —
// each occurrence should independently trigger a diagnostic
@Entity
@SequenceGenerator(name = "")
public class SequenceGeneratorEmptyNameOnTypeFieldMethod {
    @Id
    private Long id;

    @SequenceGenerator(name = "")
    private String data;

    @SequenceGenerator(name = "")
    public String getData() {
        return this.data;
    }
}
