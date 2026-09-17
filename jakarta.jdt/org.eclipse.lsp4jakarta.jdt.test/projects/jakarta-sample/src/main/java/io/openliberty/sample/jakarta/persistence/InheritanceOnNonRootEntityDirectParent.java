package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

// ❌ Invalid: direct @Entity parent — InheritanceEntityRoot is the actual root
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class InheritanceOnNonRootEntityDirectParent extends InheritanceEntityRoot {
    @Id
    private String extra;
}
