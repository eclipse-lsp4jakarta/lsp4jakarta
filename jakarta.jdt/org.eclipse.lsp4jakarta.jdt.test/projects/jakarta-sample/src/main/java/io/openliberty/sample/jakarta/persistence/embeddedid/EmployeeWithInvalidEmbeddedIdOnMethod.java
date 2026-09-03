package io.openliberty.sample.jakarta.persistence.embeddedid;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public class EmployeeWithInvalidEmbeddedIdOnMethod {

    private EmployeeIdMissingEmbeddable id;

    @EmbeddedId
    public EmployeeIdMissingEmbeddable getId() { // ❌ EmployeeIdMissingEmbeddable lacks @Embeddable
        return id;
    }

    public void setId(EmployeeIdMissingEmbeddable id) {
        this.id = id;
    }
}
