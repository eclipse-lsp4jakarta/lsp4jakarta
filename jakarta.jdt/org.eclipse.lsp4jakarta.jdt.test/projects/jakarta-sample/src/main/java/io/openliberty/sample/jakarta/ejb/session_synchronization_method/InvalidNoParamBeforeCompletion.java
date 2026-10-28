package io.openliberty.sample.jakarta.ejb.session_synchronization_method;

import jakarta.ejb.Stateful;
import jakarta.ejb.BeforeCompletion;

/**
 * Invalid session bean - @BeforeCompletion method must not declare any parameters.
 */
@Stateful
public class InvalidNoParamBeforeCompletion {

    // Error: @BeforeCompletion method must not have parameters
    @BeforeCompletion
    public void beforeCompletion(int status) {
    }
}
