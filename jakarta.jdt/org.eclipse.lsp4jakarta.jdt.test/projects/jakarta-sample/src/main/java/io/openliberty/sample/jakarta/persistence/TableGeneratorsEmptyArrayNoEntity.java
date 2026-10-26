package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.TableGenerators;

// @TableGenerators({}) on a class that does NOT carry @Entity.
// The empty-array guard is only applied to @Entity-annotated classes, so no
// diagnostic should be produced for this class.
@TableGenerators({})
public class TableGeneratorsEmptyArrayNoEntity {
    private Long id;
}
