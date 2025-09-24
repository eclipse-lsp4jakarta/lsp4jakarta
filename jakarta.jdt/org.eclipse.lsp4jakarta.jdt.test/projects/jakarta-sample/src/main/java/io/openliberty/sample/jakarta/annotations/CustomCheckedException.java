package io.openliberty.sample.jakarta.annotations;

import java.io.IOException;

public class CustomCheckedException extends IOException {
    public CustomCheckedException(String message) {
        super(message);
    }
}