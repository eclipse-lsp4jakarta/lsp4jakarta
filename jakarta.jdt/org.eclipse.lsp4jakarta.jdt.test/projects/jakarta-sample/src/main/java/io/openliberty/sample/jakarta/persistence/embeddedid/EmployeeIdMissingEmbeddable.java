package io.openliberty.sample.jakarta.persistence.embeddedid;

import java.io.Serializable;

// This key class intentionally lacks @Embeddable to trigger the diagnostic
public class EmployeeIdMissingEmbeddable implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long deptId;
    private Long empId;
}
