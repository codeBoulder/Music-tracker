package prog.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prog.dao.DiscDao;
import prog.models.Disc;
import prog.models.MusicTrack;
import prog.exception.ServiceException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DiscService {

    private static final Logger logger = LoggerFactory.getLogger(DiscService.class);

    private final DiscDao discDao;

    public DiscService(DiscDao discDao) {
        this.discDao = discDao;
    }

    public void saveDisc(Disc disc) throws ServiceException {
        if (disc == null || disc.getTitle() == null || disc.getTitle().isBlank()) {
            throw new ServiceException("Назва диска не може бути порожньою");
        }
        try {
            discDao.save(disc);
            logger.info("Disc saved: '{}'", disc.getTitle());
        } catch (SQLException e) {
            logger.error("Error saving disc", e);
            throw new ServiceException("Не вдалося зберегти диск: " + e.getMessage(), e);
        }
    }

    public void updateDisc(Disc disc) throws ServiceException {
        if (disc == null || disc.getId() <= 0) {
            throw new ServiceException("Невалідний диск для оновлення");
        }
        try {
            discDao.update(disc);
        } catch (SQLException e) {
            logger.error("Error updating disc", e);
            throw new ServiceException("Не вдалося оновити диск: " + e.getMessage(), e);
        }
    }

    public void deleteDisc(int discId) throws ServiceException {
        try {
            discDao.delete(discId);
            logger.info("Disc deleted: id={}", discId);
        } catch (SQLException e) {
            logger.error("Error deleting disc", e);
            throw new ServiceException("Не вдалося видалити диск: " + e.getMessage(), e);
        }
    }


    public List<Disc> getAllDiscs() throws ServiceException {
        try {
            return discDao.findAll();
        } catch (SQLException e) {
            logger.error("Error loading discs", e);
            throw new ServiceException("Не вдалося завантажити диски: " + e.getMessage(), e);
        }
    }


    public int calculateTotalDuration(Disc disc) {
        int total = 0;
        for (MusicTrack track : disc.getTracks()) {
            total += track.getDurationSeconds();
        }
        return total;
    }

    public String formatTotalDuration(Disc disc) {
        int total = calculateTotalDuration(disc);
        int hours   = total / 3600;
        int minutes = (total % 3600) / 60;
        int seconds = total % 60;
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        }
        return String.format("%dm %ds", minutes, seconds);
    }


    public List<MusicTrack> sortTracksByGenre(Disc disc) {
        List<MusicTrack> sorted = new ArrayList<>(disc.getTracks());

        for (int i = 1; i < sorted.size(); i++) {
            MusicTrack current = sorted.get(i);
            int j = i - 1;
            while (j >= 0 && sorted.get(j).getGenre().compareTo(current.getGenre()) > 0) {
                sorted.set(j + 1, sorted.get(j));
                j--;
            }
            sorted.set(j + 1, current);
        }

        logger.info("Tracks sorted by genre for disc: '{}'", disc.getTitle());
        return sorted;
    }
}
