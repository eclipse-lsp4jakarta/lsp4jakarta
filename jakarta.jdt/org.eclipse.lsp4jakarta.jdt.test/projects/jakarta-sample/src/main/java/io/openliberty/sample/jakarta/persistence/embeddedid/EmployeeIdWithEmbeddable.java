package io.openliberty.sample.jakarta.persistence.embeddedid;

import java.io.Serializable;
import jakarta.persistence.Embeddable;

// Correctly annotated with @Embeddable — used to verify the valid case
@Embeddable
public class EmployeeIdWithEmbeddable implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long deptId;
    private Long empId;
}
