package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

/**
 * Valid: a producer *field* is co-located with the disposer in the same class.
 */
@ApplicationScoped
public class ValidProducerFieldDisposer {

    @Produces
    private Connection connection = new Connection();

    public void cleanup(@Disposes Connection conn) {
        conn.close();
    }

    public static class Connection {
        public void close() {}
    }
}
