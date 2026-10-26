package io.openliberty.sample.jakarta.cdi;

/**
 * A bean annotated with a custom normal scope (@CustomNormalScope).
 * Used as a superclass to verify that @Specializes accepts custom-scoped beans.
 */
@CustomNormalScope
public class CustomScopedBean {
    public String greet() {
        return "Hello from CustomScopedBean";
    }
}
