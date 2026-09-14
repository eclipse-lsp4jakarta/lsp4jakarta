package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.Stateless;

/**
 * Valid session bean - does not define finalize() method.
 */

@Stateless
public class ValidStatelessBeanNoFinalize {
    
    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
