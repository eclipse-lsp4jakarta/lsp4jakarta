package io.openliberty.sample.jakarta.cdi;

/**
 * A class annotated with @NotAScope, which is a plain annotation with no @NormalScope
 * meta-annotation. This class is NOT a CDI bean.
 */
@NotAScope
public class NotAScopedBean {
    public String greet() {
        return "Hello from NotAScopedBean";
    }
}
