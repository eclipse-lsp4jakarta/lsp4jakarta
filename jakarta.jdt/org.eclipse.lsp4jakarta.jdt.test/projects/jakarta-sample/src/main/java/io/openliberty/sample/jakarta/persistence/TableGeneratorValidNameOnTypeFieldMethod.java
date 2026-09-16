package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.TableGenerator;

// @TableGenerator with a non-empty 'name' on type, field, and method —
// no diagnostic should be reported for any of these occurrences
@Entity
@TableGenerator(name = "typeGen")
public class TableGeneratorValidNameOnTypeFieldMethod {
    @Id
    private Long id;

    @TableGenerator(name = "fieldGen")
    private String data;

    @TableGenerator(name = "methodGen")
    public String getData() {
        return this.data;
    }
}
