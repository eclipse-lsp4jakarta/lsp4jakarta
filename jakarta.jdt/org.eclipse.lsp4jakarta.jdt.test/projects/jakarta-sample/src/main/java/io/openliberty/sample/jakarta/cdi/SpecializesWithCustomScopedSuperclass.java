package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.inject.Specializes;

/**
 * Valid: Extends a bean annotated with a custom @NormalScope-annotated scope (@CustomNormalScope).
 * Classes annotated with a custom normal scope are valid CDI beans, so @Specializes here
 * should NOT trigger a diagnostic.
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#direct_and_indirect_specialization">CDI 3.0 §4.3</a>
 */
@Specializes
@CustomNormalScope
public class SpecializesWithCustomScopedSuperclass extends CustomScopedBean {
    @Override
    public String greet() {
        return "Hello from SpecializesWithCustomScopedSuperclass";
    }
}
