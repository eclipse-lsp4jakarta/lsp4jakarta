package io.openliberty.sample.jakarta.ejb.session_synchronization_method;

import jakarta.ejb.Stateful;
import jakarta.ejb.AfterCompletion;

/**
 * Invalid session bean - @AfterCompletion method has multiple params, all of which are boolean.
 * QuickFix should keep the first boolean param and remove the remaining ones.
 */
@Stateful
public class InvalidAfterCompletionMultiParamAllBoolean {

    // Error: @AfterCompletion method must have exactly one boolean parameter
    @AfterCompletion
    public void afterCompletion(boolean committed, boolean extra1, boolean extra2) {
    }
}
