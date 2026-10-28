package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

/**
 * Parent entity whose primary key is the composite @EmbeddedId type ProjectId.
 */
@Entity
public class ParentWithEmbeddedId {

    @EmbeddedId
    private ProjectId id;
}
