package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.Stateful;
import jakarta.ejb.Stateless;

@Stateless
@Stateful
public class InvalidConflictingStatelessStateful {

    public InvalidConflictingStatelessStateful() {
    }

    public String hello() {
        return "Hello";
    }
}
