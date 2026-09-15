package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.Stateful;

/**
 * Invalid session bean - has only a private constructor (no public no-arg constructor).
 */

@Stateful
public class InvalidStatefulBeanPrivate {
    
    private int counter;
    
    private InvalidStatefulBeanPrivate(int initialValue) {
        this.counter = initialValue;
    }
    
    public void increment() {
        counter++;
    }
    
    public int getCounter() {
        return counter;
    }
}
