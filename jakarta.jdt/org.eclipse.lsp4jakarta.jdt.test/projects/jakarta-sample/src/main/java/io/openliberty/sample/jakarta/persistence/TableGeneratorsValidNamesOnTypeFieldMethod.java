package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.TableGenerators;

// @TableGenerators where all nested @TableGenerator entries have non-empty names on type, field,
// and method — no diagnostic should be reported for any of these occurrences
@Entity
@TableGenerators({ @TableGenerator(name = "typeGen1"), @TableGenerator(name = "typeGen2") })
public class TableGeneratorsValidNamesOnTypeFieldMethod {
    @Id
    private Long id;

    @TableGenerators({ @TableGenerator(name = "fieldGen1"), @TableGenerator(name = "fieldGen2") })
    private String data;

    @TableGenerators({ @TableGenerator(name = "methodGen1"), @TableGenerator(name = "methodGen2") })
    public String getData() {
        return this.data;
    }
}
