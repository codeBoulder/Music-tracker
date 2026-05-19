package prog.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import prog.dao.TrackDao;
import prog.models.*;
import prog.exception.ServiceException;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("TrackService unit tests")
class TrackServiceTest {

    @Mock
    private TrackDao trackDao;

    @InjectMocks
    private TrackService trackService;

    private RockTrack validRock;
    private PopTrack  validPop;
    private JazzTrack validJazz;

    @BeforeEach
    void setUp() {
        validRock = new RockTrack(1, "Back in Black", "AC/DC",       255, "Hard Rock");
        validPop  = new PopTrack(2,  "Shape of You",  "Ed Sheeran",  233, true);
        validJazz = new JazzTrack(3, "So What",        "Miles Davis", 300, "Modal");
    }

    @Test
    @DisplayName("addTrack: валідний трек — викликає dao.save")
    void addTrack_valid_callsSave() throws Exception {
        trackService.addTrack(validRock);
        verify(trackDao, times(1)).save(validRock);
    }

    @Test
    @DisplayName("addTrack: PopTrack теж зберігається успішно")
    void addTrack_popTrack_success() throws Exception {
        trackService.addTrack(validPop);
        verify(trackDao).save(validPop);
    }

    @Test
    @DisplayName("addTrack: null трек → ServiceException")
    void addTrack_null_throws() {
        assertThrows(ServiceException.class, () -> trackService.addTrack(null));
        verifyNoInteractions(trackDao);
    }

    @Test
    @DisplayName("addTrack: порожня назва → ServiceException")
    void addTrack_blankTitle_throws() {
        RockTrack bad = new RockTrack(0, "  ", "AC/DC", 200, "");
        assertThrows(ServiceException.class, () -> trackService.addTrack(bad));
        verifyNoInteractions(trackDao);
    }

    @Test
    @DisplayName("addTrack: null назва → ServiceException")
    void addTrack_nullTitle_throws() {
        RockTrack bad = new RockTrack(0, null, "AC/DC", 200, "");
        assertThrows(ServiceException.class, () -> trackService.addTrack(bad));
    }

    @Test
    @DisplayName("addTrack: порожній виконавець → ServiceException")
    void addTrack_blankArtist_throws() {
        RockTrack bad = new RockTrack(0, "Song", "", 200, "");
        assertThrows(ServiceException.class, () -> trackService.addTrack(bad));
    }

    @Test
    @DisplayName("addTrack: null виконавець → ServiceException")
    void addTrack_nullArtist_throws() {
        RockTrack bad = new RockTrack(0, "Song", null, 200, "");
        assertThrows(ServiceException.class, () -> trackService.addTrack(bad));
    }

    @Test
    @DisplayName("addTrack: тривалість 0 → ServiceException")
    void addTrack_zeroDuration_throws() {
        RockTrack bad = new RockTrack(0, "Song", "Artist", 0, "");
        assertThrows(ServiceException.class, () -> trackService.addTrack(bad));
    }

    @Test
    @DisplayName("addTrack: від'ємна тривалість → ServiceException")
    void addTrack_negativeDuration_throws() {
        RockTrack bad = new RockTrack(0, "Song", "Artist", -5, "");
        assertThrows(ServiceException.class, () -> trackService.addTrack(bad));
    }

    @Test
    @DisplayName("addTrack: SQLException у DAO → ServiceException з повідомленням")
    void addTrack_daoThrowsSql_wrapsToServiceException() throws Exception {
        doThrow(new SQLException("DB error")).when(trackDao).save(any());
        ServiceException ex = assertThrows(ServiceException.class,
                () -> trackService.addTrack(validRock));
        assertTrue(ex.getMessage().contains("DB error"));
    }


    @Test
    @DisplayName("updateTrack: валідний трек з id > 0 — викликає dao.update")
    void updateTrack_valid_callsUpdate() throws Exception {
        trackService.updateTrack(validRock);
        verify(trackDao, times(1)).update(validRock);
    }

    @Test
    @DisplayName("updateTrack: id = 0 → ServiceException")
    void updateTrack_zeroId_throws() {
        RockTrack bad = new RockTrack(0, "Song", "Artist", 200, "");
        assertThrows(ServiceException.class, () -> trackService.updateTrack(bad));
        verifyNoInteractions(trackDao);
    }

    @Test
    @DisplayName("updateTrack: від'ємний id → ServiceException")
    void updateTrack_negativeId_throws() {
        RockTrack bad = new RockTrack(-1, "Song", "Artist", 200, "");
        assertThrows(ServiceException.class, () -> trackService.updateTrack(bad));
    }

    @Test
    @DisplayName("updateTrack: null трек → ServiceException")
    void updateTrack_null_throws() {
        assertThrows(ServiceException.class, () -> trackService.updateTrack(null));
    }

    @Test
    @DisplayName("updateTrack: SQLException у DAO → ServiceException")
    void updateTrack_daoThrows_wraps() throws Exception {
        doThrow(new SQLException("update fail")).when(trackDao).update(any());
        ServiceException ex = assertThrows(ServiceException.class,
                () -> trackService.updateTrack(validRock));
        assertTrue(ex.getMessage().contains("update fail"));
    }

    @Test
    @DisplayName("deleteTrack: валідний id — викликає dao.delete")
    void deleteTrack_valid_callsDelete() throws Exception {
        trackService.deleteTrack(1);
        verify(trackDao, times(1)).delete(1);
    }

    @Test
    @DisplayName("deleteTrack: id = 0 → ServiceException")
    void deleteTrack_zeroId_throws() {
        assertThrows(ServiceException.class, () -> trackService.deleteTrack(0));
        verifyNoInteractions(trackDao);
    }

    @Test
    @DisplayName("deleteTrack: від'ємний id → ServiceException")
    void deleteTrack_negativeId_throws() {
        assertThrows(ServiceException.class, () -> trackService.deleteTrack(-1));
    }

    @Test
    @DisplayName("deleteTrack: SQLException у DAO → ServiceException")
    void deleteTrack_daoThrows_wraps() throws Exception {
        doThrow(new SQLException("delete fail")).when(trackDao).delete(anyInt());
        ServiceException ex = assertThrows(ServiceException.class,
                () -> trackService.deleteTrack(1));
        assertTrue(ex.getMessage().contains("delete fail"));
    }

    @Test
    @DisplayName("getAllTracks: повертає список з DAO")
    void getAllTracks_returnsDaoResult() throws Exception {
        List<MusicTrack> expected = List.of(validRock, validPop);
        when(trackDao.findAll()).thenReturn(expected);

        List<MusicTrack> result = trackService.getAllTracks();
        assertEquals(2, result.size());
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("getAllTracks: порожній список — повертає порожній список")
    void getAllTracks_emptyDao_returnsEmpty() throws Exception {
        when(trackDao.findAll()).thenReturn(List.of());
        List<MusicTrack> result = trackService.getAllTracks();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAllTracks: SQLException → ServiceException")
    void getAllTracks_daoThrows_wraps() throws Exception {
        when(trackDao.findAll()).thenThrow(new SQLException("read fail"));
        assertThrows(ServiceException.class, () -> trackService.getAllTracks());
    }

    @Test
    @DisplayName("findByDurationRange: повертає треки в діапазоні [200, 260]")
    void findByDurationRange_returnsMatchingTracks() throws Exception {
        // rock=255, pop=233, jazz=300
        when(trackDao.findAll()).thenReturn(List.of(validRock, validPop, validJazz));

        List<MusicTrack> result = trackService.findByDurationRange(200, 260);

        assertEquals(2, result.size());
        assertTrue(result.contains(validRock));
        assertTrue(result.contains(validPop));
        assertFalse(result.contains(validJazz));
    }

    @Test
    @DisplayName("findByDurationRange: межі включні — трек рівно на межі входить")
    void findByDurationRange_boundaryInclusive() throws Exception {
        when(trackDao.findAll()).thenReturn(List.of(validRock)); // 255 сек
        List<MusicTrack> result = trackService.findByDurationRange(255, 255);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findByDurationRange: жоден трек не відповідає — порожній список")
    void findByDurationRange_noMatch_returnsEmpty() throws Exception {
        when(trackDao.findAll()).thenReturn(List.of(validRock, validPop));
        List<MusicTrack> result = trackService.findByDurationRange(1, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByDurationRange: мінімум < 0 → ServiceException")
    void findByDurationRange_negativeMin_throws() {
        assertThrows(ServiceException.class,
                () -> trackService.findByDurationRange(-1, 300));
        verifyNoInteractions(trackDao);
    }

    @Test
    @DisplayName("findByDurationRange: max < min → ServiceException")
    void findByDurationRange_maxLessThanMin_throws() {
        assertThrows(ServiceException.class,
                () -> trackService.findByDurationRange(300, 100));
        verifyNoInteractions(trackDao);
    }

    @Test
    @DisplayName("findByDurationRange: min = 0 та max = 0 — перевірка граничного валідного випадку")
    void findByDurationRange_zeroRange_valid() throws Exception {
        when(trackDao.findAll()).thenReturn(List.of());
        // 0, 0 — мін >= 0 і max >= min, тому валідний
        List<MusicTrack> result = trackService.findByDurationRange(0, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByDurationRange: getAllTracks кидає помилку — пробрасує ServiceException")
    void findByDurationRange_daoThrows_wraps() throws Exception {
        when(trackDao.findAll()).thenThrow(new SQLException("fail"));
        assertThrows(ServiceException.class,
                () -> trackService.findByDurationRange(0, 500));
    }
}
