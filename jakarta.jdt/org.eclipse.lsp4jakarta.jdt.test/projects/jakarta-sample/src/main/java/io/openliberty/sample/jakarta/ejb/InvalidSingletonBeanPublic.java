package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.Singleton;

/**
 * Invalid session bean - has only a public parameterized constructor (no public no-arg constructor).
 */

@Singleton
public class InvalidSingletonBeanPublic {
    
    private String config;
    
    public InvalidSingletonBeanPublic(String config) {
        this.config = config;
    }
    
    public String getConfig() {
        return config;
    }
    
    public void setConfig(String config) {
        this.config = config;
    }
}
