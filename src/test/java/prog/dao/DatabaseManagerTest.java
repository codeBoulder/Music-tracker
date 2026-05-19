package prog.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DatabaseManager unit tests")
class DatabaseManagerTest {

    @Test
    @DisplayName("getInstance: повертає той самий екземпляр (перевірка Singleton)")
    void testSingletonInstance() {
        // Коли
        DatabaseManager instance1 = DatabaseManager.getInstance();
        DatabaseManager instance2 = DatabaseManager.getInstance();

        // Тоді
        assertNotNull(instance1, "Екземпляр не повинен бути null");
        assertSame(instance1, instance2, "getInstance має повертати один і той самий об'єкт у пам'яті");
    }

    @Test
    @DisplayName("getConnection: повертає активне з'єднання з базою даних")
    void testConnectionIsNotNullAndOpen() throws SQLException {
        // Дано
        DatabaseManager dbManager = DatabaseManager.getInstance();

        // Коли
        Connection connection = dbManager.getConnection();

        // Тоді
        assertNotNull(connection, "З'єднання не повинно бути null");
        assertFalse(connection.isClosed(), "З'єднання має бути відкритим для роботи");
    }

    @Test
    @DisplayName("initSchema: успішно створює всі необхідні таблиці")
    void testSchemaInitialized() throws SQLException {
        // Дано
        DatabaseManager dbManager = DatabaseManager.getInstance();
        Connection connection = dbManager.getConnection();

        // Тоді перевіряємо наявність кожної таблиці через метадані JDBC
        assertTrue(tableExists(connection, "tracks"), "Таблиця 'tracks' має бути створена");
        assertTrue(tableExists(connection, "discs"), "Таблиця 'discs' має бути створена");
        assertTrue(tableExists(connection, "disc_tracks"), "Таблиця 'disc_tracks' має бути створена");
    }

    // =========================================================
    //  Допоміжні методи для тестів
    // =========================================================

    /**
     * Перевіряє, чи існує таблиця з вказаною назвою у базі даних.
     */
    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        // У SQLite назви таблиць зазвичай зберігаються в нижньому регістрі,
        // але getTables шукає без урахування регістру, якщо передати правильні параметри
        try (ResultSet resultSet = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return resultSet.next(); // Якщо є хоча б один запис, значить таблиця існує
        }
    }
}