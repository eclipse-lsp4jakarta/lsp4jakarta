package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

// ❌ Invalid: plain class has @Inheritance but no @Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class InheritanceOnPlainClass {
    private Long id;
}
