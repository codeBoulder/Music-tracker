package prog.dao;

import org.junit.jupiter.api.*;
import prog.models.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TrackDao integration tests")
class TrackDaoTest {

    private static Connection connection;
    private TrackDao trackDao;

    private static final String CREATE_TRACKS = """
        CREATE TABLE IF NOT EXISTS tracks (
            id           INTEGER PRIMARY KEY AUTOINCREMENT,
            title        TEXT    NOT NULL,
            artist       TEXT    NOT NULL,
            duration_sec INTEGER NOT NULL,
            genre        TEXT    NOT NULL,
            extra_field  TEXT
        )
    """;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
    }

    @BeforeEach
    void setUpTable() throws SQLException {
        // Створюємо таблицю перед кожним тестом
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(CREATE_TRACKS);
        }
        trackDao = new TrackDao(connection);
    }

    @AfterEach
    void tearDownTable() throws SQLException {
        // Дропаємо таблицю щоб кожен тест мав чистий стан
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS tracks");
        }
    }

    @AfterAll
    static void closeDatabase() throws SQLException {
        connection.close();
    }

    @Test
    @DisplayName("save RockTrack: встановлює id після збереження")
    void save_rockTrack_setsId() throws SQLException {
        RockTrack track = new RockTrack(0, "Back in Black", "AC/DC", 255, "Hard Rock");
        trackDao.save(track);
        assertTrue(track.getId() > 0, "id повинен бути встановлений після save()");
    }

    @Test
    @DisplayName("save PopTrack: зберігається та знаходиться через findById")
    void save_popTrack_canBeFoundById() throws SQLException {
        PopTrack pop = new PopTrack(0, "Shape of You", "Ed Sheeran", 233, true);
        trackDao.save(pop);

        MusicTrack found = trackDao.findById(pop.getId());
        assertNotNull(found);
        assertInstanceOf(PopTrack.class, found);
        assertEquals("Shape of You", found.getTitle());
        assertEquals("Ed Sheeran",   found.getArtist());
        assertEquals(233,             found.getDurationSeconds());
        assertTrue(((PopTrack) found).isHasVideoClip());
    }

    @Test
    @DisplayName("save JazzTrack: зберігається з полем mood")
    void save_jazzTrack_moodPersisted() throws SQLException {
        JazzTrack jazz = new JazzTrack(0, "So What", "Miles Davis", 565, "Modal");
        trackDao.save(jazz);

        MusicTrack found = trackDao.findById(jazz.getId());
        assertInstanceOf(JazzTrack.class, found);
        assertEquals("Modal", ((JazzTrack) found).getMood());
    }

    @Test
    @DisplayName("save ClassicalTrack: зберігається з полем composerEra")
    void save_classicalTrack_eraPersisted() throws SQLException {
        ClassicalTrack ct = new ClassicalTrack(0, "Moonlight Sonata", "Beethoven", 900, "Romantic");
        trackDao.save(ct);

        MusicTrack found = trackDao.findById(ct.getId());
        assertInstanceOf(ClassicalTrack.class, found);
        assertEquals("Romantic", ((ClassicalTrack) found).getComposerEra());
    }

    @Test
    @DisplayName("save PopTrack з hasVideoClip = false: зберігається коректно")
    void save_popTrack_noVideo_persisted() throws SQLException {
        PopTrack pop = new PopTrack(0, "T", "A", 120, false);
        trackDao.save(pop);
        MusicTrack found = trackDao.findById(pop.getId());
        assertFalse(((PopTrack) found).isHasVideoClip());
    }

    @Test
    @DisplayName("save RockTrack: subgenre зберігається")
    void save_rockTrack_subgenrePersisted() throws SQLException {
        RockTrack rock = new RockTrack(0, "Smells Like Teen Spirit", "Nirvana", 301, "Grunge");
        trackDao.save(rock);
        MusicTrack found = trackDao.findById(rock.getId());
        assertEquals("Grunge", ((RockTrack) found).getSubgenre());
    }

    @Test
    @DisplayName("update: змінює назву та тривалість трека")
    void update_changesTitleAndDuration() throws SQLException {
        RockTrack track = new RockTrack(0, "Original", "Artist", 200, "Punk");
        trackDao.save(track);

        track.setTitle("Updated Title");
        track.setDurationSeconds(350);
        trackDao.update(track);

        MusicTrack found = trackDao.findById(track.getId());
        assertEquals("Updated Title", found.getTitle());
        assertEquals(350,             found.getDurationSeconds());
    }

    @Test
    @DisplayName("update: зміна жанру (Rock → Jazz) відображається при findById")
    void update_changesGenre() throws SQLException {
        RockTrack rock = new RockTrack(0, "My Song", "Artist", 200, "Hard");
        trackDao.save(rock);

        JazzTrack jazz = new JazzTrack(rock.getId(), "My Song", "Artist", 200, "Smooth");
        trackDao.update(jazz);

        MusicTrack found = trackDao.findById(rock.getId());
        assertInstanceOf(JazzTrack.class, found);
        assertEquals("Jazz", found.getGenre());
    }

    @Test
    @DisplayName("delete: трек більше не знаходиться через findById")
    void delete_trackNotFoundAfterDelete() throws SQLException {
        RockTrack track = new RockTrack(0, "Delete Me", "Artist", 100, "");
        trackDao.save(track);
        int id = track.getId();

        trackDao.delete(id);

        assertNull(trackDao.findById(id));
    }

    @Test
    @DisplayName("delete: неіснуючий id — не кидає виняток")
    void delete_nonExistentId_noException() {
        assertDoesNotThrow(() -> trackDao.delete(9999));
    }

    @Test
    @DisplayName("findAll: порожня таблиця — порожній список")
    void findAll_emptyTable() throws SQLException {
        List<MusicTrack> tracks = trackDao.findAll();
        assertTrue(tracks.isEmpty());
    }

    @Test
    @DisplayName("findAll: повертає всі збережені треки")
    void findAll_returnsAllTracks() throws SQLException {
        trackDao.save(new RockTrack(0, "Song1", "A1", 200, "Punk"));
        trackDao.save(new PopTrack(0,  "Song2", "A2", 180, false));
        trackDao.save(new JazzTrack(0, "Song3", "A3", 300, "Bebop"));

        List<MusicTrack> tracks = trackDao.findAll();
        assertEquals(3, tracks.size());
    }

    @Test
    @DisplayName("findAll: повертає треки відсортовані за id")
    void findAll_orderedById() throws SQLException {
        trackDao.save(new RockTrack(0, "A", "X", 200, ""));
        trackDao.save(new PopTrack(0,  "B", "Y", 180, true));

        List<MusicTrack> tracks = trackDao.findAll();
        assertTrue(tracks.get(0).getId() < tracks.get(1).getId());
    }

    @Test
    @DisplayName("findAll: правильно маппить різні жанри")
    void findAll_mapsAllGenresCorrectly() throws SQLException {
        trackDao.save(new RockTrack(0,     "R", "A", 200, "Punk"));
        trackDao.save(new PopTrack(0,      "P", "B", 180, true));
        trackDao.save(new JazzTrack(0,     "J", "C", 300, "Smooth"));
        trackDao.save(new ClassicalTrack(0,"C", "D", 900, "Baroque"));

        List<MusicTrack> tracks = trackDao.findAll();
        assertEquals(4, tracks.size());

        long rockCount = tracks.stream().filter(t -> t instanceof RockTrack).count();
        long popCount  = tracks.stream().filter(t -> t instanceof PopTrack).count();
        long jazzCount = tracks.stream().filter(t -> t instanceof JazzTrack).count();
        long classCount= tracks.stream().filter(t -> t instanceof ClassicalTrack).count();

        assertEquals(1, rockCount);
        assertEquals(1, popCount);
        assertEquals(1, jazzCount);
        assertEquals(1, classCount);
    }

    @Test
    @DisplayName("findById: неіснуючий id → null")
    void findById_notFound_returnsNull() throws SQLException {
        assertNull(trackDao.findById(9999));
    }

    @Test
    @DisplayName("findById: повертає правильний трек")
    void findById_returnsCorrectTrack() throws SQLException {
        RockTrack saved = new RockTrack(0, "My Rock", "Band", 240, "Alternative");
        trackDao.save(saved);

        MusicTrack found = trackDao.findById(saved.getId());
        assertNotNull(found);
        assertEquals("My Rock",      found.getTitle());
        assertEquals("Band",          found.getArtist());
        assertEquals(240,             found.getDurationSeconds());
        assertEquals("Rock",          found.getGenre());
    }

    @Test
    @DisplayName("findById: невідомий genre у БД маппиться як RockTrack (default гілка switch)")
    void findById_unknownGenre_mapsToDefault() throws SQLException {
        // Вставляємо вручну рядок з невідомим жанром
        try (var ps = connection.prepareStatement(
                "INSERT INTO tracks (title, artist, duration_sec, genre, extra_field) VALUES (?,?,?,?,?)")) {
            ps.setString(1, "Unknown Genre Song");
            ps.setString(2, "Artist");
            ps.setInt(3, 100);
            ps.setString(4, "Electronic"); // невідомий жанр
            ps.setString(5, null);
            ps.executeUpdate();
        }

        // Дістаємо id останнього рядку
        int id;
        try (var rs = connection.createStatement().executeQuery("SELECT last_insert_rowid()")) {
            id = rs.getInt(1);
        }

        MusicTrack track = trackDao.findById(id);
        assertNotNull(track);
        // default гілка у mapRow() повертає RockTrack
        assertInstanceOf(RockTrack.class, track);
    }
}
