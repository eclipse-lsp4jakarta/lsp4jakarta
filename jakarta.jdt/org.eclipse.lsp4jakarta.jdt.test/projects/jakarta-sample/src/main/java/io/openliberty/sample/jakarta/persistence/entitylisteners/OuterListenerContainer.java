package io.openliberty.sample.jakarta.persistence.entitylisteners;

public class OuterListenerContainer {

    // Non-static inner class with implicit constructor
    public class NonStaticInnerImplicitListener {
    }

    // Non-static inner class with explicit constructor
    public class NonStaticInnerExplicitListener {
        public NonStaticInnerExplicitListener() {
        }
    }

    // Static nested class with implicit constructor
    public static class StaticNestedImplicitListener {
    }

    // Static nested class with explicit public constructor
    public static class StaticNestedExplicitListener {
        public StaticNestedExplicitListener() {
        }
    }
}
