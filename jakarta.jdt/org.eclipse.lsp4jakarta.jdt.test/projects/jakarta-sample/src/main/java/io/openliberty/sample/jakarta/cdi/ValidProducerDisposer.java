package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

/**
 * Valid: a producer method is co-located with the disposer method in the same class.
 */
@ApplicationScoped
public class ValidProducerDisposer {

    @Produces
    public Connection produceConnection() {
        return new Connection();
    }

    public void cleanup(@Disposes Connection conn) {
        conn.close();
    }

    public static class Connection {
        public void close() {}
    }
}
