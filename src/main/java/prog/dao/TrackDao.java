package prog.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prog.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrackDao {

    private static final Logger logger = LoggerFactory.getLogger(TrackDao.class);

    private final Connection connection;

    public TrackDao(Connection connection) {
        this.connection = connection;
    }


    public void save(MusicTrack track) throws SQLException {
        String sql = "INSERT INTO tracks (title, artist, duration_sec, genre, extra_field) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, track.getTitle());
            ps.setString(2, track.getArtist());
            ps.setInt(3, track.getDurationSeconds());
            ps.setString(4, track.getGenre());
            ps.setString(5, getExtraField(track));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                track.setId(keys.getInt(1));
            }
            logger.info("Track saved: {}", track.getTitle());
        }
    }

    public void update(MusicTrack track) throws SQLException {
        String sql = "UPDATE tracks SET title=?, artist=?, duration_sec=?, genre=?, extra_field=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, track.getTitle());
            ps.setString(2, track.getArtist());
            ps.setInt(3, track.getDurationSeconds());
            ps.setString(4, track.getGenre());
            ps.setString(5, getExtraField(track));
            ps.setInt(6, track.getId());
            ps.executeUpdate();
            logger.info("Track updated: id={}", track.getId());
        }
    }


    public void delete(int trackId) throws SQLException {
        String sql = "DELETE FROM tracks WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, trackId);
            ps.executeUpdate();
            logger.info("Track deleted: id={}", trackId);
        }
    }

    /**
     * Повертає всі треки з бази.
     */
    public List<MusicTrack> findAll() throws SQLException {
        List<MusicTrack> tracks = new ArrayList<>();
        String sql = "SELECT * FROM tracks ORDER BY id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tracks.add(mapRow(rs));
            }
        }
        return tracks;
    }

    /**
     * Знаходить трек за id.
     */
    public MusicTrack findById(int id) throws SQLException {
        String sql = "SELECT * FROM tracks WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        }
        return null;
    }

    // --- Допоміжні методи ---

    /**
     * Отримує специфічне поле залежно від жанру.
     */
    private String getExtraField(MusicTrack track) {
        if (track instanceof RockTrack rock) {
            return rock.getSubgenre();
        } else if (track instanceof PopTrack pop) {
            return String.valueOf(pop.isHasVideoClip());
        } else if (track instanceof JazzTrack jazz) {
            return jazz.getMood();
        } else if (track instanceof ClassicalTrack classical) {
            return classical.getComposerEra();
        }
        return null;
    }

    /**
     * Перетворює рядок ResultSet у відповідний об'єкт MusicTrack.
     */
    private MusicTrack mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String artist = rs.getString("artist");
        int duration = rs.getInt("duration_sec");
        String genre = rs.getString("genre");
        String extra = rs.getString("extra_field");

        return switch (genre) {
            case "Rock"      -> new RockTrack(id, title, artist, duration, extra != null ? extra : "");
            case "Pop"       -> new PopTrack(id, title, artist, duration, "true".equals(extra));
            case "Jazz"      -> new JazzTrack(id, title, artist, duration, extra != null ? extra : "");
            case "Classical" -> new ClassicalTrack(id, title, artist, duration, extra != null ? extra : "");
            default          -> new RockTrack(id, title, artist, duration, "");
        };
    }
}
