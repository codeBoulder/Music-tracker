package prog.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private static final String SENDER_EMAIL = "arsengorobets1@gmail.com";
    private static final String APP_PASSWORD = "cpml cgjk virl nhmx";
    private static final String RECEIVER_EMAIL = "arsengorobets1@gmail.com"; // може бути тим самим, що й SENDER

    public void sendException(String operation, Exception exception) {

        CompletableFuture.runAsync(() -> {
            try {
                sendEmail(operation, exception);
            } catch (Exception e) {
                logger.error("Не вдалося відправити email про помилку", e);
            }
        });
    }

    private void sendEmail(String operation, Exception ex) throws MessagingException {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(RECEIVER_EMAIL));
        message.setSubject("⚠️ КРИТИЧНА ПОМИЛКА: Music Collection App");

        String emailText = String.format(
                "Сталася помилка під час виконання операції: %s\n\n" +
                        "Тип винятку: %s\n" +
                        "Повідомлення: %s\n\n" +
                        "Будь ласка, перевірте лог-файли для отримання StackTrace.",
                operation, ex.getClass().getName(), ex.getMessage()
        );

        message.setText(emailText);

        Transport.send(message);
        logger.info("Email з повідомленням про помилку успішно відправлено!");
    }
}