package io.openliberty.sample.jakarta.persistence.embeddedid;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public class EmployeeWithValidEmbeddedId {

    @EmbeddedId
    private EmployeeIdWithEmbeddable id; // ✅ EmployeeIdWithEmbeddable has @Embeddable
}
