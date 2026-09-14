package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.Singleton;
import jakarta.ejb.Stateful;

@Stateful
@Singleton
public class InvalidConflictingStatefulSingleton {

    public InvalidConflictingStatefulSingleton() {
    }

    public String hello() {
        return "Hello";
    }
}
