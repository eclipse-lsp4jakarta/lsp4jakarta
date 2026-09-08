package io.openliberty.sample.jakarta.persistence;

import java.io.Serializable;

public class ValidIdClass implements Serializable {

    private static final long serialVersionUID = 1L;

    public ValidIdClass() {
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ValidIdClass;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
