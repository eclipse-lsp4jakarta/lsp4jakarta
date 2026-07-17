package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.TableGenerators;

// @TableGenerators with a nested @TableGenerator(name = "") on type, field, and method —
// each nested empty name should independently trigger a diagnostic
@Entity
@TableGenerators({ @TableGenerator(name = "") })
public class TableGeneratorsWithEmptyNameOnTypeFieldMethod {
    @Id
    private Long id;

    @TableGenerators({ @TableGenerator(name = "") })
    private String data;

    @TableGenerators({ @TableGenerator(name = "") })
    public String getData() {
        return this.data;
    }
}
