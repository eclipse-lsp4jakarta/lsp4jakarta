package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

/**
 * VALID: the parent has a composite @EmbeddedId (ProjectId) and the @MapsId
 * attribute parentKey has that same type.
 * Expects: no diagnostics
 */
@Entity
public class ValidMapsIdCompositeParent {

    @EmbeddedId
    private ChildKey id;

    @MapsId("parentKey")
    @ManyToOne
    private ParentWithEmbeddedId parent;
}
