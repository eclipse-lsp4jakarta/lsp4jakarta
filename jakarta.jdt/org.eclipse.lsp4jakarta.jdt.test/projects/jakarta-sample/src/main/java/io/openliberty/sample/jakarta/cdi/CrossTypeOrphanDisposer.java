package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

/**
 * Invalid: the producer creates a {@code Connection} but the disposer disposes a
 * {@code Session}.  Because there is no {@code @Produces Session} in this class,
 * the disposer is an orphan and must be flagged with
 * {@code InvalidOrphanDisposerMethod}.
 */
@ApplicationScoped
public class CrossTypeOrphanDisposer {

    @Produces
    public Connection produceConnection() {
        return new Connection();
    }

    /** Orphan — @Produces Session is absent; only @Produces Connection exists. */
    public void cleanupSession(@Disposes Session session) {
        session.close();
    }

    public static class Connection {
        public void close() {}
    }

    public static class Session {
        public void close() {}
    }
}
