package io.openliberty.sample.jakarta.persistence.entitylisteners;

/**
 * A class nested inside an interface is implicitly static per JLS 9.5.
 * It should therefore NOT be treated as a non-static inner class by
 * {@link org.eclipse.lsp4jakarta.jdt.internal.core.java.ManagedBean#isInnerClass}.
 */
public interface InterfaceHolderWithListener {

    // Nested inside an interface -> implicitly static; valid as an entity listener.
    class ListenerInsideInterface {
        public ListenerInsideInterface() {
        }
    }
}
