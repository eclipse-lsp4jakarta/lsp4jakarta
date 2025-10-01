package io.openliberty.sample.jakarta.annotations;

public class CustomUncheckedException extends IllegalArgumentException {
    public CustomUncheckedException(String message) {
        super(message);
    }
}
