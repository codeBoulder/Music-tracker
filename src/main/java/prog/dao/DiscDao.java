package prog.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prog.models.Disc;
import prog.models.MusicTrack;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DiscDao {

    private static final Logger logger = LoggerFactory.getLogger(DiscDao.class);

    private final Connection connection;
    private final TrackDao trackDao;

    public DiscDao(Connection connection) {
        this.connection = connection;
        this.trackDao = new TrackDao(connection);
    }


    public void save(Disc disc) throws SQLException {
        String sql = "INSERT INTO discs (title) VALUES (?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, disc.getTitle());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                disc.setId(keys.getInt(1));
            }
        }
        saveDiscTracks(disc);
        logger.info("Disc saved: '{}'", disc.getTitle());
    }


    public void update(Disc disc) throws SQLException {
        String sql = "UPDATE discs SET title=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, disc.getTitle());
            ps.setInt(2, disc.getId());
            ps.executeUpdate();
        }
        deleteDiscTracks(disc.getId());
        saveDiscTracks(disc);
        logger.info("Disc updated: id={}", disc.getId());
    }

    public void delete(int discId) throws SQLException {
        deleteDiscTracks(discId);
        String sql = "DELETE FROM discs WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, discId);
            ps.executeUpdate();
        }
        logger.info("Disc deleted: id={}", discId);
    }


    public List<Disc> findAll() throws SQLException {
        List<Disc> discs = new ArrayList<>();
        String sql = "SELECT * FROM discs ORDER BY id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Disc disc = new Disc(rs.getInt("id"), rs.getString("title"));
                List<MusicTrack> tracks = loadTracksForDisc(disc.getId());
                disc.setTracks(tracks);
                discs.add(disc);
            }
        }
        return discs;
    }

    public Disc findById(int id) throws SQLException {
        String sql = "SELECT * FROM discs WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Disc disc = new Disc(rs.getInt("id"), rs.getString("title"));
                disc.setTracks(loadTracksForDisc(disc.getId()));
                return disc;
            }
        }
        return null;
    }

    private void saveDiscTracks(Disc disc) throws SQLException {
        String sql = "INSERT INTO disc_tracks (disc_id, track_id, position) VALUES (?, ?, ?)";
        List<MusicTrack> tracks = disc.getTracks();
        for (int i = 0; i < tracks.size(); i++) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, disc.getId());
                ps.setInt(2, tracks.get(i).getId());
                ps.setInt(3, i);
                ps.executeUpdate();
            }
        }
    }

    private void deleteDiscTracks(int discId) throws SQLException {
        String sql = "DELETE FROM disc_tracks WHERE disc_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, discId);
            ps.executeUpdate();
        }
    }

    private List<MusicTrack> loadTracksForDisc(int discId) throws SQLException {
        List<MusicTrack> tracks = new ArrayList<>();
        String sql = """
            SELECT t.* FROM tracks t
            JOIN disc_tracks dt ON t.id = dt.track_id
            WHERE dt.disc_id = ?
            ORDER BY dt.position
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, discId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tracks.add(new TrackDao(connection).findById(rs.getInt("id")));
            }
        }
        return tracks;
    }
}
