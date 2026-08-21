package io.openliberty.sample.jakarta.persistence;

/**
 * A plain (non-entity) class with no persistence annotations.
 * The guard in NamedEntityGraphDiagnosticsParticipant must skip
 * the project-wide scan for this file entirely.
 */
public class NonEntityClass {

    private String value;

    public NonEntityClass() {
    }
}
