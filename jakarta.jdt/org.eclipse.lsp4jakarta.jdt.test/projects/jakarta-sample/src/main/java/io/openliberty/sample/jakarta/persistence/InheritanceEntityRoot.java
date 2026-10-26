package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

// ✅ Valid: @Entity + @Inheritance, no @Entity ancestor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class InheritanceEntityRoot {
    @Id
    private Long id;
}
