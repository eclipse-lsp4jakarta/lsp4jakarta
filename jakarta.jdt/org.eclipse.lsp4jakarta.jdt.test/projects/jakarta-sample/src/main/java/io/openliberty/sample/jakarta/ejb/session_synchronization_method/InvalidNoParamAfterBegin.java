package io.openliberty.sample.jakarta.ejb.session_synchronization_method;

import jakarta.ejb.Stateful;
import jakarta.ejb.AfterBegin;

/**
 * Invalid session bean - @AfterBegin method must not declare any parameters.
 */
@Stateful
public class InvalidNoParamAfterBegin {

    // Error: @AfterBegin method must not have parameters
    @AfterBegin
    public void afterBegin(String info) {
    }
}
