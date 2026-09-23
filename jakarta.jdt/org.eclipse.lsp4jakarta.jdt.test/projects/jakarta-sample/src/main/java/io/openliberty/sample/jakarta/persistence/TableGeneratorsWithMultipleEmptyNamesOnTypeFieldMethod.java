package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.TableGenerators;

// @TableGenerators with multiple nested @TableGenerator entries where more than one has an empty
// name should report a diagnostic for each invalid nested annotation on type, field, and method
@Entity
@TableGenerators({ @TableGenerator(name = "   "), @TableGenerator(name = "name1"), @TableGenerator(name = "") })
public class TableGeneratorsWithMultipleEmptyNamesOnTypeFieldMethod {
    @Id
    private Long id;

    @TableGenerators({ @TableGenerator(name = "   "), @TableGenerator(name = "name1"), @TableGenerator(name = "") })
    private String data;

    @TableGenerators({ @TableGenerator(name = "   "), @TableGenerator(name = "name1"), @TableGenerator(name = "") })
    public String getData() {
        return this.data;
    }
}
