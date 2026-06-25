package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.Stateless;

/**
 * Valid session bean with proper public no-arg constructor.
 */

@Stateless
public class ValidStatelessBean {
    
    private String name;
    
    public ValidStatelessBean() {
    }
    
    public ValidStatelessBean(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
