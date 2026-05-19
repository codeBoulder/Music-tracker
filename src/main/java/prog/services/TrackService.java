package prog.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prog.dao.TrackDao;
import prog.models.MusicTrack;
import prog.exception.ServiceException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TrackService {

    private static final Logger logger = LoggerFactory.getLogger(TrackService.class);

    private final TrackDao trackDao;

    public TrackService(TrackDao trackDao) {
        this.trackDao = trackDao;
    }

    public void addTrack(MusicTrack track) throws ServiceException {
        validateTrack(track);
        try {
            trackDao.save(track);
            logger.info("Track added: '{}'", track.getTitle());
        } catch (SQLException e) {
            logger.error("Error saving track", e);
            throw new ServiceException("Не вдалося зберегти трек: " + e.getMessage(), e);
        }
    }


    public void updateTrack(MusicTrack track) throws ServiceException {
        validateTrack(track);
        if (track.getId() <= 0) {
            throw new ServiceException("Трек не має валідного id");
        }
        try {
            trackDao.update(track);
            logger.info("Track updated: id={}", track.getId());
        } catch (SQLException e) {
            logger.error("Error updating track", e);
            throw new ServiceException("Не вдалося оновити трек: " + e.getMessage(), e);
        }
    }

    public void deleteTrack(int trackId) throws ServiceException {
        if (trackId <= 0) {
            throw new ServiceException("Невалідний id трека");
        }
        try {
            trackDao.delete(trackId);
            logger.info("Track deleted: id={}", trackId);
        } catch (SQLException e) {
            logger.error("Error deleting track", e);
            throw new ServiceException("Не вдалося видалити трек: " + e.getMessage(), e);
        }
    }

    public List<MusicTrack> getAllTracks() throws ServiceException {
        try {
            return trackDao.findAll();
        } catch (SQLException e) {
            logger.error("Error loading tracks", e);
            throw new ServiceException("Не вдалося завантажити треки: " + e.getMessage(), e);
        }
    }


    public List<MusicTrack> findByDurationRange(int minSec, int maxSec) throws ServiceException {
        if (minSec < 0 || maxSec < minSec) {
            throw new ServiceException("Невалідний діапазон тривалості");
        }
        List<MusicTrack> allTracks = getAllTracks();
        List<MusicTrack> result = new ArrayList<>();
        for (MusicTrack track : allTracks) {
            if (track.getDurationSeconds() >= minSec && track.getDurationSeconds() <= maxSec) {
                result.add(track);
            }
        }
        logger.info("Found {} tracks in range [{}, {}]", result.size(), minSec, maxSec);
        return result;
    }


    private void validateTrack(MusicTrack track) throws ServiceException {
        if (track == null) {
            throw new ServiceException("Трек не може бути null");
        }
        if (track.getTitle() == null || track.getTitle().isBlank()) {
            throw new ServiceException("Назва треку не може бути порожньою");
        }
        if (track.getArtist() == null || track.getArtist().isBlank()) {
            throw new ServiceException("Виконавець не може бути порожнім");
        }
        if (track.getDurationSeconds() <= 0) {
            throw new ServiceException("Тривалість треку має бути більше 0 секунд");
        }
    }
}
