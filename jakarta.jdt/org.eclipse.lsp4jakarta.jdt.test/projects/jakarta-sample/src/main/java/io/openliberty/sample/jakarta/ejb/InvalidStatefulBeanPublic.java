package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.Stateful;

/**
 * Invalid session bean - has only a public parameterized constructor (no public no-arg constructor).
 */

@Stateful
public class InvalidStatefulBeanPublic {
    
    private int counter;
    
    public InvalidStatefulBeanPublic(int initialValue) {
        this.counter = initialValue;
    }
    
    public void increment() {
        counter++;
    }
    
    public int getCounter() {
        return counter;
    }
}
