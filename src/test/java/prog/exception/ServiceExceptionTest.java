package prog.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ServiceExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // Дано (Arrange)
        String expectedMessage = "Помилка завантаження бази даних";

        // Коли (Act)
        ServiceException exception = new ServiceException(expectedMessage);

        // Тоді (Assert)
        assertNotNull(exception, "Об'єкт винятку не повинен бути null");
        assertEquals(expectedMessage, exception.getMessage(), "Повідомлення винятку має збігатися з переданим");
        assertNull(exception.getCause(), "Причина (cause) має бути null, якщо вона не передавалася");
    }

    @Test
    void testConstructorWithMessageAndCause() {
        // Дано (Arrange)
        String expectedMessage = "Помилка під час збереження треку";
        Throwable expectedCause = new RuntimeException("Помилка SQL з'єднання");

        // Коли (Act)
        ServiceException exception = new ServiceException(expectedMessage, expectedCause);

        // Тоді (Assert)
        assertNotNull(exception, "Об'єкт винятку не повинен бути null");
        assertEquals(expectedMessage, exception.getMessage(), "Повідомлення винятку має збігатися з переданим");
        assertEquals(expectedCause, exception.getCause(), "Причина (cause) має збігатися з переданою");
    }
}