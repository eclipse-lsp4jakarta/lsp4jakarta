package io.openliberty.sample.jakarta.ejb.session_synchronization_method;

import jakarta.ejb.Stateful;
import jakarta.ejb.AfterBegin;
import jakarta.ejb.BeforeCompletion;
import jakarta.ejb.AfterCompletion;

/**
 * Invalid session bean - session synchronization methods with mixed illegal modifiers.
 * Each method combines more than one violation to exercise multiple diagnostics on a single method.
 */
@Stateful
public class InvalidMixedModifiersSessionSyncMethod {

    // Errors: must not be declared final AND must not be declared static
    @AfterBegin
    public static final void beginSyncMixed() {
    }

    // Errors: must not be declared static AND must be of type void
    @BeforeCompletion
    public static boolean beforeCommitMixed() {
        return false;
    }

    // Errors: must not be declared final AND must be of type void
    @AfterCompletion
    public final int afterCompleteMixed(boolean committed) {
        return 0;
    }
}
