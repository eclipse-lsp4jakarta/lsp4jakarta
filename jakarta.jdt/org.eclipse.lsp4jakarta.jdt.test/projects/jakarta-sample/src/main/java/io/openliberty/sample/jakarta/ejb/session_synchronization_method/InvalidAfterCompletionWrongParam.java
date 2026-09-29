package io.openliberty.sample.jakarta.ejb.session_synchronization_method;

import jakarta.ejb.Stateful;
import jakarta.ejb.AfterCompletion;

/**
 * Invalid session bean - @AfterCompletion method must declare exactly one boolean parameter.
 * This class has a parameter of the wrong type (String instead of boolean).
 */
@Stateful
public class InvalidAfterCompletionWrongParam {

    // Error: @AfterCompletion method parameter must be boolean
    @AfterCompletion
    public void afterCompletion(String status) {
    }
}
