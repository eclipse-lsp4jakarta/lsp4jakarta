package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import java.util.Properties;

@ApplicationScoped
public class ProducerFieldWithNamed {
    
    // Invalid: Producer field with @Named annotation
    @Produces
    @Named("config")
    private Properties config = new Properties();
    
    // Invalid: Producer field with @Named annotation (no value)
    @Produces
    @Named
    private String greeting = "Hello"; 
    
    // Valid: Producer method with @Named annotation (methods are allowed)
    @Produces
    @Named("message")
    public String getMessage() {
        return "Valid producer method";
    }
    
    // Valid: Producer field without @Named annotation
    @Produces
    private Integer count = 0;
}
