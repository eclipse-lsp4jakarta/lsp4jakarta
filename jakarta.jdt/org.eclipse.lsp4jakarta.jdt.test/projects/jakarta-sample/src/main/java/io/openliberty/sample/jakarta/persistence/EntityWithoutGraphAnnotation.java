package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * An @Entity class with NO @NamedEntityGraph or @NamedEntityGraphs annotation.
 * The guard in NamedEntityGraphDiagnosticsParticipant must skip
 * the project-wide scan for this file entirely.
 */
@Entity
public class EntityWithoutGraphAnnotation {

    @Id
    private Long id;

    private String name;

    public EntityWithoutGraphAnnotation() {
    }
}
