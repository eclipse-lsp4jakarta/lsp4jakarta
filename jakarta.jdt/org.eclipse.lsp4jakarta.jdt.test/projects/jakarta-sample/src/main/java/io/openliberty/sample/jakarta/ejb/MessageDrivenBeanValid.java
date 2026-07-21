package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;

@MessageDriven(
    activationConfig = {
        @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
        @jakarta.ejb.ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/MyQueue")
    }
)
public class MessageDrivenBeanValid implements MessageListener {

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                String text = textMessage.getText();
                System.out.println("Received message: " + text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}