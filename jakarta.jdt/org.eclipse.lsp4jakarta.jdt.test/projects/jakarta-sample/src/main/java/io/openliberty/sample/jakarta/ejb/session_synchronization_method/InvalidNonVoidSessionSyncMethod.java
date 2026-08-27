package io.openliberty.sample.jakarta.ejb.session_synchronization_method;

import jakarta.ejb.Stateful;
import jakarta.ejb.AfterCompletion;

/**
 * Invalid session bean - @AfterCompletion method must return void.
 */
@Stateful
public class InvalidNonVoidSessionSyncMethod {

    // Error: @AfterCompletion method must return void
    @AfterCompletion
    public boolean afterComplete(boolean committed) {
        return committed;
    }
}
