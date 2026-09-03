package io.openliberty.sample.jakarta.persistence.embeddedid;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public class EmployeeWithValidEmbeddedIdOnMethod {

    private EmployeeIdWithEmbeddable id;

    @EmbeddedId
    public EmployeeIdWithEmbeddable getId() { // ✅ EmployeeIdWithEmbeddable has @Embeddable
        return id;
    }

    public void setId(EmployeeIdWithEmbeddable id) {
        this.id = id;
    }
}
