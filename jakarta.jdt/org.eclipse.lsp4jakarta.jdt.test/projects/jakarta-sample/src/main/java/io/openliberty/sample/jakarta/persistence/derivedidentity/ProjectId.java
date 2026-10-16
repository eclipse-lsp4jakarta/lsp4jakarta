package io.openliberty.sample.jakarta.persistence.derivedidentity;

import java.io.Serializable;
import jakarta.persistence.Embeddable;

/**
 * @EmbeddedId class used by entities in the derivedidentity test package.
 * Contains a long empId and a long projectId — used to test @MapsId attribute binding.
 */
@Embeddable
public class ProjectId implements Serializable {
    private static final long serialVersionUID = 1L;
    public long empId;
    public long projectId;
}
