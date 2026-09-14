package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.Stateless;

/**
 * Invalid session bean - has a public parameterized constructor (no public no-arg constructor).
 */

@Stateless
public class InvalidStatelessBeanPublic {
    
    private String name;

    public InvalidStatelessBeanPublic(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
