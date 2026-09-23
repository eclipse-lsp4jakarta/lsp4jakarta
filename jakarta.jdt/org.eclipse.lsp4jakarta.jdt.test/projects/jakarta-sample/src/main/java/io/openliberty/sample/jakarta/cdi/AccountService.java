package io.openliberty.sample.jakarta.cdi;

/**
 * Minimal business interface used by decorator scope-validation test fixtures.
 */
public interface AccountService {
    void processAccount(String accountId);
}
