package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.Stateless;

/**
 * Invalid session bean - has only a private constructor (no public no-arg constructor).
 */

@Stateless
public class InvalidStatelessBeanPrivate {
    
    private String name;

    private InvalidStatelessBeanPrivate(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
