package io.openliberty.sample.jakarta.ejb.session_synchronization_method;

import jakarta.ejb.Stateful;
import jakarta.ejb.AfterCompletion;

/**
 * Invalid session bean - @AfterCompletion method has multiple params, one of which is boolean.
 * QuickFix should remove all non-boolean params and keep the boolean one.
 */
@Stateful
public class InvalidAfterCompletionMultiParamWithBoolean {

    // Error: @AfterCompletion method must have exactly one boolean parameter
    @AfterCompletion
    public void afterCompletion(String status, boolean committed, int extra) {
    }
}
