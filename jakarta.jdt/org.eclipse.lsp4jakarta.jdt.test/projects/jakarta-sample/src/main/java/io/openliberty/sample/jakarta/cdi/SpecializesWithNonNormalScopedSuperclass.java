package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.inject.Specializes;

/**
 * Invalid: NotAScopedBean is annotated with @NotAScope, which is a plain annotation
 * not meta-annotated with @NormalScope. It is therefore NOT a CDI bean.
 * @Specializes here should trigger a diagnostic error.
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#direct_and_indirect_specialization">CDI 3.0 §4.3</a>
 */
@Specializes
public class SpecializesWithNonNormalScopedSuperclass extends NotAScopedBean {
    @Override
    public String greet() {
        return "Hello from SpecializesWithNonNormalScopedSuperclass";
    }
}
