package io.openliberty.sample.jakarta.ejb.session_synchronization_method;

import jakarta.ejb.Stateful;
import jakarta.ejb.AfterCompletion;

/**
 * Invalid session bean - @AfterCompletion method has multiple params, none of which is boolean.
 * QuickFix should keep the first param but change its type to boolean, and remove the rest.
 */
@Stateful
public class InvalidAfterCompletionMultiParamNoBoolean {

    // Error: @AfterCompletion method must have exactly one boolean parameter
    @AfterCompletion
    public void afterCompletion(String status, int extra) {
    }
}
