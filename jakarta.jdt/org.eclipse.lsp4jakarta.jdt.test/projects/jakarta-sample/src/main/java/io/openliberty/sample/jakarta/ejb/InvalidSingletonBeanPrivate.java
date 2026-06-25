package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.Singleton;

/**
 * Invalid session bean - has only a private constructor (no public no-arg constructor).
 */

@Singleton
public class InvalidSingletonBeanPrivate {
    
    private String config;
    
    private InvalidSingletonBeanPrivate(String config) {
        this.config = config;
    }
    
    public String getConfig() {
        return config;
    }
}
