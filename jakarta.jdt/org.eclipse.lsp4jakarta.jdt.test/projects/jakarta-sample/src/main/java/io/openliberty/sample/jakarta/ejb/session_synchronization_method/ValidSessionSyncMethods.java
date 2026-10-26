package io.openliberty.sample.jakarta.ejb.session_synchronization_method;

import jakarta.ejb.Stateful;
import jakarta.ejb.AfterBegin;
import jakarta.ejb.BeforeCompletion;
import jakarta.ejb.AfterCompletion;

/**
 * Valid session bean - all session synchronization methods comply with the spec.
 */
@Stateful
public class ValidSessionSyncMethods {

    // Valid: not final, not static, returns void
    @AfterBegin
    public void afterBegin() {
    }

    // Valid: not final, not static, returns void
    @BeforeCompletion
    public void beforeCompletion() {
    }

    // Valid: not final, not static, returns void, and has exactly one boolean parameter
    @AfterCompletion
    public void afterCompletion(boolean committed) {
    }
}
