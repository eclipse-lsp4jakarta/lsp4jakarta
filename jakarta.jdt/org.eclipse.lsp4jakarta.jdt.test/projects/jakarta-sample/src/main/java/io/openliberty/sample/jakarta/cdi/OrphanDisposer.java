package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;

/**
 * Invalid: a disposer method with no matching producer in this class.
 */
@ApplicationScoped
public class OrphanDisposer {

    public void cleanup(@Disposes Connection conn) {
        conn.close();
    }

    public static class Connection {
        public void close() {}
    }
}
