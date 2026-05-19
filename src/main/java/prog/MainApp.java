package prog;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prog.dao.DatabaseManager;

public class MainApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
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
