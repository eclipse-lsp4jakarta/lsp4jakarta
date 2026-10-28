package io.openliberty.sample.jakarta.persistence.derivedidentity;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;

/**
 * @EmbeddedId class used by entities in the derivedidentity test package.
 * Contains a long empId and a long projectId — used to test @MapsId attribute binding.
 * Also used as an @IdClass, so it declares equals and hashCode.
 */
@Embeddable
public class ProjectId implements Serializable {
    private static final long serialVersionUID = 1L;
    public long empId;
    public long projectId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProjectId)) {
            return false;
        }
        ProjectId other = (ProjectId) o;
        return empId == other.empId && projectId == other.projectId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(empId, projectId);
    }
}
