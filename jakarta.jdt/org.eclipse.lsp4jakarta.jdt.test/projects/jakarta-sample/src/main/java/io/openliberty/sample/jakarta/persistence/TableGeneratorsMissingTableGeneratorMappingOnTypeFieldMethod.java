package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.TableGenerators;

// @TableGenerators with empty arrays on type, field, and method simultaneously —
// each occurrence should independently trigger a diagnostic
@Entity
@TableGenerators({})
public class TableGeneratorsMissingTableGeneratorMappingOnTypeFieldMethod {
    @Id
    private Long id;

    @TableGenerators({})
    private String data;

    @TableGenerators({})
    public String getData() {
        return this.data;
    }
}
