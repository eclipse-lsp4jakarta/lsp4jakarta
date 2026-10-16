package io.openliberty.sample.jakarta.persistence.derivedidentity;

import jakarta.persistence.Embeddable;

/**
 * Embeddable key whose parentKey attribute has the same type as the primary key
 * of ParentWithEmbeddedId (ProjectId); wrongKey does not.
 */
@Embeddable
public class ChildKey {
    public ProjectId parentKey;
    public String wrongKey;
    public int seq;
}
