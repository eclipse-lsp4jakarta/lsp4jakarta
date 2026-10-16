package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;

/**
 * VALID (spec section 2.4.1.1, Example 5b/6b): the embedded id has the same type
 * as the parent's primary key (ProjectId), so @MapsId is specified without a value.
 * Expects: no diagnostics
 */
@Entity
public class ValidMapsIdValuelessEmbeddedId {

    @EmbeddedId
    private ProjectId id;

    @MapsId
    @OneToOne
    private ParentWithEmbeddedId parent;
}
