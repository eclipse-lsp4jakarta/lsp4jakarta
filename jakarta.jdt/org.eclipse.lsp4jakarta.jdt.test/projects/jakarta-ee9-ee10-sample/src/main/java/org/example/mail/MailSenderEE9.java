package org.example.mail;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Sample Mail sender using:
 *  - Jakarta Mail 2.0 (EE 9)
 *
 * Replaces jakarta.inject-api:2.0.1 which shares the same version across EE 9, EE 10 and EE 11
 * and therefore cannot be used as a version-distinct EE 9 marker dependency.
 */
public class MailSenderEE9 {

    public void send(String to, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "localhost");

        Session session = Session.getInstance(props);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("no-reply@example.org"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}
