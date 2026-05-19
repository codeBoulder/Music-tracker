package prog.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("EmailNotificationService unit tests")
class EmailNotificationServiceTest {

    @Test
    @DisplayName("sendException: успішно формує лист та викликає Transport.send у фоновому потоці")
    void testSendExceptionSuccess() {
        // Дано
        EmailNotificationService service = new EmailNotificationService();
        Exception testException = new RuntimeException("Помилка з'єднання з SQLite");

        // Створюємо перехоплювач для статичного методу Transport.send()
        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {

            // Коли — викликаємо метод асинхронної відправки
            assertDoesNotThrow(() -> service.sendException("Зчитування треків", testException));

            // Тоді — оскільки потік фоновий, використовуємо timeout(), щоб Mockito зачекав виконання
            mockedTransport.verify(
                    () -> Transport.send(any(Message.class)),
                    timeout(2000).times(1)
            );
        }
    }

    @Test
    @DisplayName("sendException: коректно обробляє MessagingException, якщо SMTP сервер недоступний")
    void testSendExceptionWhenMailServerFails() {
        // Дано
        EmailNotificationService service = new EmailNotificationService();
        Exception testException = new RuntimeException("Критичний збій системи");

        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            // Імітуємо ситуацію, коли Transport.send викидає помилку пошти (наприклад, немає інтернету)
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("Authentication failed / Connection timed out"));

            // Коли — сервіс не повинен "упустити" всю програму, а має просто залогувати помилку в catch
            assertDoesNotThrow(() -> service.sendException("Запис диска", testException));

            // Перевіряємо, що спроба відправки відбулася
            mockedTransport.verify(
                    () -> Transport.send(any(Message.class)),
                    timeout(2000).times(1)
            );
        }
    }
}