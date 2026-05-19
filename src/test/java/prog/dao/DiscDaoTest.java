package prog.dao;

import org.junit.jupiter.api.*;
import prog.models.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Інтеграційні тести для DiscDao.
 * Використовує SQLite in-memory базу з повною схемою (tracks + discs + disc_tracks).
 */
@DisplayName("DiscDao integration tests")
class DiscDaoTest {

    private static Connection connection;
    private TrackDao trackDao;
    private DiscDao  discDao;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        // Вмикаємо підтримку foreign keys для SQLite
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
    }

    @BeforeEach
    void setUpSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tracks (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    title        TEXT    NOT NULL,
                    artist       TEXT    NOT NULL,
                    duration_sec INTEGER NOT NULL,
                    genre        TEXT    NOT NULL,
                    extra_field  TEXT
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS discs (
                    id    INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL
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
        }
        trackDao = new TrackDao(connection);
        discDao  = new DiscDao(connection);
    }

    @AfterEach
    void tearDownSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS disc_tracks");
            stmt.execute("DROP TABLE IF EXISTS discs");
            stmt.execute("DROP TABLE IF EXISTS tracks");
        }
    }

    @AfterAll
    static void closeDatabase() throws SQLException {
        connection.close();
    }

    // --- Допоміжний метод: зберегти трек та повернути збережений об'єкт ---
    private RockTrack saveRockTrack(String title, String artist, int duration) throws SQLException {
        RockTrack track = new RockTrack(0, title, artist, duration, "");
        trackDao.save(track);
        return track;
    }

    // =========================================================
    //  save
    // =========================================================

    @Test
    @DisplayName("save: диск отримує id після збереження")
    void save_setsDiscId() throws SQLException {
        Disc disc = new Disc(0, "My Album");
        discDao.save(disc);
        assertTrue(disc.getId() > 0);
    }

    @Test
    @DisplayName("save: диск зі списком треків — зв'язки зберігаються")
    void save_withTracks_tracksLinked() throws SQLException {
        RockTrack t1 = saveRockTrack("Song 1", "A1", 200);
        RockTrack t2 = saveRockTrack("Song 2", "A2", 250);

        Disc disc = new Disc(0, "Album With Tracks");
        disc.addTrack(t1);
        disc.addTrack(t2);
        discDao.save(disc);

        Disc loaded = discDao.findById(disc.getId());
        assertNotNull(loaded);
        assertEquals(2, loaded.getTracks().size());
    }

    @Test
    @DisplayName("save: диск без треків — зберігається успішно")
    void save_emptyDisc_noException() throws SQLException {
        Disc disc = new Disc(0, "Empty Album");
        assertDoesNotThrow(() -> discDao.save(disc));
        assertTrue(disc.getId() > 0);
    }

    @Test
    @DisplayName("save: порядок треків зберігається")
    void save_tracksOrderPreserved() throws SQLException {
        RockTrack t1 = saveRockTrack("First",  "A", 100);
        RockTrack t2 = saveRockTrack("Second", "B", 200);
        RockTrack t3 = saveRockTrack("Third",  "C", 300);

        Disc disc = new Disc(0, "Ordered Album");
        disc.addTrack(t1);
        disc.addTrack(t2);
        disc.addTrack(t3);
        discDao.save(disc);

        Disc loaded = discDao.findById(disc.getId());
        List<MusicTrack> tracks = loaded.getTracks();
        assertEquals("First",  tracks.get(0).getTitle());
        assertEquals("Second", tracks.get(1).getTitle());
        assertEquals("Third",  tracks.get(2).getTitle());
    }

    // =========================================================
    //  update
    // =========================================================

    @Test
    @DisplayName("update: назва диска змінюється")
    void update_changesTitle() throws SQLException {
        Disc disc = new Disc(0, "Old Title");
        discDao.save(disc);

        disc.setTitle("New Title");
        discDao.update(disc);

        Disc loaded = discDao.findById(disc.getId());
        assertEquals("New Title", loaded.getTitle());
    }

    @Test
    @DisplayName("update: список треків повністю замінюється")
    void update_replacesTrackList() throws SQLException {
        RockTrack t1 = saveRockTrack("Original Track", "A", 200);
        RockTrack t2 = saveRockTrack("New Track",      "B", 150);

        Disc disc = new Disc(0, "Album");
        disc.addTrack(t1);
        discDao.save(disc);

        // Замінюємо список
        disc.setTracks(List.of(t2));
        discDao.update(disc);

        Disc loaded = discDao.findById(disc.getId());
        assertEquals(1, loaded.getTracks().size());
        assertEquals("New Track", loaded.getTracks().get(0).getTitle());
    }

    @Test
    @DisplayName("update: після оновлення старі зв'язки видалено, нові додано")
    void update_oldTracksRemovedNewAdded() throws SQLException {
        RockTrack t1 = saveRockTrack("T1", "A", 100);
        RockTrack t2 = saveRockTrack("T2", "B", 200);
        RockTrack t3 = saveRockTrack("T3", "C", 300);

        Disc disc = new Disc(0, "Disc");
        disc.addTrack(t1);
        disc.addTrack(t2);
        discDao.save(disc);

        disc.setTracks(List.of(t3));
        discDao.update(disc);

        Disc loaded = discDao.findById(disc.getId());
        assertEquals(1, loaded.getTracks().size());
        assertEquals("T3", loaded.getTracks().get(0).getTitle());
    }

    // =========================================================
    //  delete
    // =========================================================

    @Test
    @DisplayName("delete: диск більше не знаходиться після видалення")
    void delete_discNotFoundAfterDelete() throws SQLException {
        Disc disc = new Disc(0, "To Delete");
        discDao.save(disc);
        int id = disc.getId();

        discDao.delete(id);

        assertNull(discDao.findById(id));
    }

    @Test
    @DisplayName("delete: зв'язки disc_tracks теж видаляються")
    void delete_discTracksRemovedToo() throws SQLException {
        RockTrack track = saveRockTrack("Track", "A", 200);
        Disc disc = new Disc(0, "Album");
        disc.addTrack(track);
        discDao.save(disc);
        int discId = disc.getId();

        discDao.delete(discId);

        // Перевіряємо disc_tracks напряму
        try (var ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM disc_tracks WHERE disc_id=?")) {
            ps.setInt(1, discId);
            var rs = ps.executeQuery();
            assertEquals(0, rs.getInt(1));
        }
    }

    @Test
    @DisplayName("delete: неіснуючий id — не кидає виняток")
    void delete_nonExistentId_noException() {
        assertDoesNotThrow(() -> discDao.delete(9999));
    }

    // =========================================================
    //  findAll
    // =========================================================

    @Test
    @DisplayName("findAll: порожня таблиця → порожній список")
    void findAll_emptyTable() throws SQLException {
        assertTrue(discDao.findAll().isEmpty());
    }

    @Test
    @DisplayName("findAll: повертає всі збережені диски")
    void findAll_returnsAllDiscs() throws SQLException {
        discDao.save(new Disc(0, "Album 1"));
        discDao.save(new Disc(0, "Album 2"));
        discDao.save(new Disc(0, "Album 3"));

        List<Disc> discs = discDao.findAll();
        assertEquals(3, discs.size());
    }

    @Test
    @DisplayName("findAll: кожен диск містить свої треки")
    void findAll_eachDiscHasItsOwnTracks() throws SQLException {
        RockTrack t1 = saveRockTrack("T1", "A", 100);
        RockTrack t2 = saveRockTrack("T2", "B", 200);

        Disc d1 = new Disc(0, "D1");
        d1.addTrack(t1);
        discDao.save(d1);

        Disc d2 = new Disc(0, "D2");
        d2.addTrack(t2);
        discDao.save(d2);

        List<Disc> discs = discDao.findAll();
        assertEquals(2, discs.size());

        Disc loaded1 = discs.stream().filter(d -> d.getTitle().equals("D1")).findFirst().orElseThrow();
        Disc loaded2 = discs.stream().filter(d -> d.getTitle().equals("D2")).findFirst().orElseThrow();

        assertEquals(1, loaded1.getTracks().size());
        assertEquals("T1", loaded1.getTracks().get(0).getTitle());

        assertEquals(1, loaded2.getTracks().size());
        assertEquals("T2", loaded2.getTracks().get(0).getTitle());
    }

    // =========================================================
    //  findById
    // =========================================================

    @Test
    @DisplayName("findById: неіснуючий id → null")
    void findById_notFound_returnsNull() throws SQLException {
        assertNull(discDao.findById(9999));
    }

    @Test
    @DisplayName("findById: повертає диск з коректними треками")
    void findById_returnsDiscWithTracks() throws SQLException {
        RockTrack track = saveRockTrack("Rock Song", "Band", 240);
        Disc disc = new Disc(0, "Best of Rock");
        disc.addTrack(track);
        discDao.save(disc);

        Disc found = discDao.findById(disc.getId());
        assertNotNull(found);
        assertEquals("Best of Rock", found.getTitle());
        assertEquals(1, found.getTracks().size());
        assertEquals("Rock Song", found.getTracks().get(0).getTitle());
    }

    @Test
    @DisplayName("findById: диск без треків — повертається з порожнім списком")
    void findById_discWithoutTracks_emptyList() throws SQLException {
        Disc disc = new Disc(0, "Empty");
        discDao.save(disc);

        Disc found = discDao.findById(disc.getId());
        assertNotNull(found);
        assertTrue(found.getTracks().isEmpty());
    }
}
