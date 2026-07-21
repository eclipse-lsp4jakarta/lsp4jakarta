package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.Singleton;

/**
 * Invalid session bean - defines finalize() method.
 */

@Singleton
public class InvalidSingletonBeanFinalize {
    
    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    protected void finalize() throws Throwable {
        // Session beans must not define finalize()
        super.finalize();
    }
}
