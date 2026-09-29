package io.openliberty.sample.jakarta.ejb.session_synchronization_method;

import jakarta.ejb.Stateful;
import jakarta.ejb.AfterCompletion;

/**
 * Invalid session bean - @AfterCompletion method must declare exactly one boolean parameter.
 * This class has no parameters (missing the required boolean).
 */
@Stateful
public class InvalidAfterCompletionNoParam {

    // Error: @AfterCompletion method must have exactly one boolean parameter
    @AfterCompletion
    public void afterCompletion() {
    }
}
