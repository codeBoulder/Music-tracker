package prog.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_URL = "jdbc:sqlite:music_collection.db";

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            logger.info("Connected to SQLite database: {}", DB_URL);
            initSchema();
        } catch (SQLException e) {
            logger.error("Failed to connect to database", e);
            throw new RuntimeException("Cannot connect to database", e);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initSchema() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS tracks (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                title           TEXT    NOT NULL,
                artist          TEXT    NOT NULL,
                duration_sec    INTEGER NOT NULL,
                genre           TEXT    NOT NULL,
                extra_field     TEXT
            )
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS discs (
                id    INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT    NOT NULL
            )
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS disc_tracks (
                disc_id  INTEGER NOT NULL,
                track_id INTEGER NOT NULL,
                position INTEGER NOT NULL,
                PRIMARY KEY (disc_id, track_id),
                FOREIGN KEY (disc_id)  REFERENCES discs(id)  ON DELETE CASCADE,
                FOREIGN KEY (track_id) REFERENCES tracks(id) ON DELETE CASCADE
            )
        """);

        stmt.close();
        logger.info("Database schema initialized");
    }
}
