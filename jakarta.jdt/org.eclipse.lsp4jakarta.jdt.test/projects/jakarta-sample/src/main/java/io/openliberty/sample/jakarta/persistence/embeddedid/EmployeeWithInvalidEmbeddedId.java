package io.openliberty.sample.jakarta.persistence.embeddedid;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public class EmployeeWithInvalidEmbeddedId {

    @EmbeddedId
    private EmployeeIdMissingEmbeddable id; // ❌ EmployeeIdMissingEmbeddable lacks @Embeddable
}
