package io.openliberty.sample.jakarta.ejb.session_synchronization_method;

import jakarta.ejb.Stateful;
import jakarta.ejb.BeforeCompletion;

/**
 * Invalid session bean - @BeforeCompletion method must not be declared static.
 */
@Stateful
public class InvalidStaticSessionSyncMethod {

    // Error: @BeforeCompletion method must not be declared static
    @BeforeCompletion
    public static void beforeCommit() {
    }
}
