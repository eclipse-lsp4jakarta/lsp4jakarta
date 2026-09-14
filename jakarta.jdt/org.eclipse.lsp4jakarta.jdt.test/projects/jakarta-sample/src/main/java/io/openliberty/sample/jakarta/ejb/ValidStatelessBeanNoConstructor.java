package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.Stateless;

/**
 * Valid session bean with no user-defined constructor.
 */

@Stateless
public class ValidStatelessBeanNoConstructor {
    
    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
