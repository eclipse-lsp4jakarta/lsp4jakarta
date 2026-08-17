package io.openliberty.sample.jakarta.ejb.session_synchronization_method;

import jakarta.ejb.Stateful;
import jakarta.ejb.AfterBegin;

/**
 * Invalid session bean - @AfterBegin method must not be declared final.
 */
@Stateful
public class InvalidFinalSessionSyncMethod {

    // Error: @AfterBegin method must not be declared final
    @AfterBegin
    public final void beginSync() {
    }
}
