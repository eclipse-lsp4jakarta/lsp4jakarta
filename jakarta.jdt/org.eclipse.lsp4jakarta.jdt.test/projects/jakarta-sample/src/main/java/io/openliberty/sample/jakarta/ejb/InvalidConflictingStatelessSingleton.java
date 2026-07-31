package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.Singleton;
import jakarta.ejb.Stateless;

@Stateless
@Singleton
public class InvalidConflictingStatelessSingleton {

    public InvalidConflictingStatelessSingleton() {
    }

    public String hello() {
        return "Hello";
    }
}
