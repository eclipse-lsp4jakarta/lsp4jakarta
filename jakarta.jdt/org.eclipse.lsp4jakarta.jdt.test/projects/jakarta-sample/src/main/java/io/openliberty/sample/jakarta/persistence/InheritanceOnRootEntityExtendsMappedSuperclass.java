package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

// ✅ Valid: @Entity + @Inheritance extending @MappedSuperclass.
// @MappedSuperclass is NOT @Entity — no @Entity ancestor exists in the
// superclass chain, so this class is correctly the hierarchy root.
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class InheritanceOnRootEntityExtendsMappedSuperclass extends BaseMappedSuperclass {
    private String name;
}
