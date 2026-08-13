package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

// ❌ Invalid: @Entity ancestor hidden behind non-entity gap
// Chain: InheritanceOnNonRootEntityWithGap(@Entity+@Inheritance)
//           -> InheritanceNonEntityGap (no @Entity, transparent)
//           -> InheritanceEntityRoot(@Entity) ← actual root
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class InheritanceOnNonRootEntityWithGap extends InheritanceNonEntityGap {
    @Id
    private String detail;
}
