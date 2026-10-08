package io.openliberty.sample.jakarta.ejb.session_synchronization_method;

import jakarta.ejb.AfterBegin;
import jakarta.ejb.BeforeCompletion;
import jakarta.ejb.AfterCompletion;

/**
 * Non-session-bean class with session synchronization annotations.
 * No diagnostics should be produced because the class is not a session bean.
 */
public class NonSessionBeanWithSessionSyncMethods {

    // No session bean annotation on the class — violations below should NOT be flagged
    @AfterBegin
    public final void beginSync() {
    }

    @BeforeCompletion
    public static void beforeCommit() {
    }

    @AfterCompletion
    public boolean afterComplete(boolean committed) {
        return committed;
    }
}
