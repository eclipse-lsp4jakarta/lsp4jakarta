package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.MessageDriven;
import java.io.Serializable;

@MessageDriven(
    activationConfig = {
        @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
        @jakarta.ejb.ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/MyQueue")
    }
)
public class MessageDrivenBeanWrongInterface implements Serializable {
    // Implements wrong interface - should trigger a diagnostic
    
    public void someMethod() {
        System.out.println("This is not a message listener method");
    }
}