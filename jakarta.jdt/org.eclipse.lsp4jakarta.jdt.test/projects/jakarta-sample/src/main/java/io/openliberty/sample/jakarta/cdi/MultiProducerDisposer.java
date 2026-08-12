package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

/**
 * Valid: the class produces a {@code Session} (field) AND disposes a {@code Session}.
 * It <em>also</em> produces a {@code Connection} (method) but disposes nothing for it —
 * that is not an error (no disposer required).
 * This exercises the case where multiple producer types exist in the same class.
 */
@ApplicationScoped
public class MultiProducerDisposer {

    /** Produces a Session via a field. */
    @Produces
    private Session session = new Session();

    /** Produces a Connection via a method. */
    @Produces
    public Connection produceConnection() {
        return new Connection();
    }

    /** Valid disposer — matches the @Produces Session field above. */
    public void cleanupSession(@Disposes Session s) {
        s.close();
    }

    public static class Connection {
        public void close() {}
    }

    public static class Session {
        public void close() {}
    }
}
