package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.TableGenerator;

// @TableGenerator with empty 'name' on type, field, and method simultaneously —
// each occurrence should independently trigger a diagnostic
@Entity
@TableGenerator(name = "")
public class TableGeneratorEmptyNameOnTypeFieldMethod {
    @Id
    private Long id;

    @TableGenerator(name = "")
    private String data;

    @TableGenerator(name = "")
    public String getData() {
        return this.data;
    }
}
