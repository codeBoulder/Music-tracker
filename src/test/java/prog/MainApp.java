package prog;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prog.dao.DatabaseManager;
import prog.services.EmailNotificationService; // Додали імпорт нашого сервісу

/**
 * Точка входу JavaFX застосунку.
 */
public class MainApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    // Створюємо єдиний екземпляр сервісу для всього застосунку
    private final EmailNotificationService emailService = new EmailNotificationService();

    @Override
    public void start(Stage primaryStage) throws Exception {

        // КРИТИЧНЕ ВИПРАВЛЕННЯ: Глобальний перехоплювач усіх крашів програми
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            // 1. Обов'язково записуємо помилку в лог-файл через SLF4J
            logger.error("Критична невідловлена помилка у потоці: {}", thread.getName(), throwable);

            // 2. Автоматично надсилаємо звіт про цей краш на пошту
            Exception exceptionToSend = (throwable instanceof Exception)
                    ? (Exception) throwable
                    : new Exception(throwable);

            emailService.sendException("Глобальний збіг застосунку в потоці " + thread.getName(), exceptionToSend);
        });

        // Ініціалізуємо базу даних при старті
        DatabaseManager.getInstance();

        // Шлях до FXML-view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 900, 650);

        // Шлях до стилей CSS
        scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

        primaryStage.setTitle("Music Collection Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
        logger.info("Application started");
    }

    @Override
    public void stop() {
        logger.info("Application stopped");
    }

    public static void main(String[] args) {
        launch(args);
    }
}