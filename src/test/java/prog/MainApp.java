package prog;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prog.dao.DatabaseManager;
import prog.services.EmailNotificationService; // Додали імпорт нашого сервісу

public class MainApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    private final EmailNotificationService emailService = new EmailNotificationService();

    @Override
    public void start(Stage primaryStage) throws Exception {

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.error("Критична невідловлена помилка у потоці: {}", thread.getName(), throwable);

            Exception exceptionToSend = (throwable instanceof Exception)
                    ? (Exception) throwable
                    : new Exception(throwable);

            emailService.sendException("Глобальний збіг застосунку в потоці " + thread.getName(), exceptionToSend);
        });

        DatabaseManager.getInstance();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 900, 650);

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