package prog.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import prog.dao.DiscDao;
import prog.models.*;
import prog.exception.ServiceException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Юніт-тести для DiscService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DiscService unit tests")
class DiscServiceTest {

    @Mock
    private DiscDao discDao;

    @InjectMocks
    private DiscService discService;

    private Disc disc;
    private RockTrack     rock;
    private PopTrack      pop;
    private JazzTrack     jazz;
    private ClassicalTrack classical;

    @BeforeEach
    void setUp() {
        disc      = new Disc(1, "Test Album");
        rock      = new RockTrack(1,     "Highway to Hell", "AC/DC",       208, "Hard Rock");
        pop       = new PopTrack(2,      "Shape of You",    "Ed Sheeran",  233, true);
        jazz      = new JazzTrack(3,     "So What",          "Miles Davis", 300, "Modal");
        classical = new ClassicalTrack(4,"Moonlight Sonata", "Beethoven",   900, "Romantic");
    }

    // =========================================================
    //  saveDisc
    // =========================================================

    @Test
    @DisplayName("saveDisc: валідний диск — викликає dao.save")
    void saveDisc_valid_callsSave() throws Exception {
        discService.saveDisc(disc);
        verify(discDao, times(1)).save(disc);
    }

    @Test
    @DisplayName("saveDisc: null диск → ServiceException, DAO не викликається")
    void saveDisc_null_throws() {
        assertThrows(ServiceException.class, () -> discService.saveDisc(null));
        verifyNoInteractions(discDao);
    }

    @Test
    @DisplayName("saveDisc: порожня назва → ServiceException")
    void saveDisc_blankTitle_throws() {
        Disc bad = new Disc(0, "  ");
        assertThrows(ServiceException.class, () -> discService.saveDisc(bad));
        verifyNoInteractions(discDao);
    }

    @Test
    @DisplayName("saveDisc: null назва → ServiceException")
    void saveDisc_nullTitle_throws() {
        Disc bad = new Disc(0, null);
        assertThrows(ServiceException.class, () -> discService.saveDisc(bad));
    }

    @Test
    @DisplayName("saveDisc: SQLException у DAO → ServiceException з повідомленням")
    void saveDisc_daoThrows_wraps() throws Exception {
        doThrow(new SQLException("save failed")).when(discDao).save(any());
        ServiceException ex = assertThrows(ServiceException.class,
                () -> discService.saveDisc(disc));
        assertTrue(ex.getMessage().contains("save failed"));
    }

    // =========================================================
    //  updateDisc
    // =========================================================

    @Test
    @DisplayName("updateDisc: диск з id > 0 — викликає dao.update")
    void updateDisc_valid_callsUpdate() throws Exception {
        discService.updateDisc(disc);
        verify(discDao, times(1)).update(disc);
    }

    @Test
    @DisplayName("updateDisc: null → ServiceException")
    void updateDisc_null_throws() {
        assertThrows(ServiceException.class, () -> discService.updateDisc(null));
        verifyNoInteractions(discDao);
    }

    @Test
    @DisplayName("updateDisc: id = 0 → ServiceException")
    void updateDisc_zeroId_throws() {
        Disc bad = new Disc(0, "Title");
        assertThrows(ServiceException.class, () -> discService.updateDisc(bad));
    }

    @Test
    @DisplayName("updateDisc: від'ємний id → ServiceException")
    void updateDisc_negativeId_throws() {
        Disc bad = new Disc(-5, "Title");
        assertThrows(ServiceException.class, () -> discService.updateDisc(bad));
    }

    @Test
    @DisplayName("updateDisc: SQLException у DAO → ServiceException")
    void updateDisc_daoThrows_wraps() throws Exception {
        doThrow(new SQLException("upd fail")).when(discDao).update(any());
        ServiceException ex = assertThrows(ServiceException.class,
                () -> discService.updateDisc(disc));
        assertTrue(ex.getMessage().contains("upd fail"));
    }

    // =========================================================
    //  deleteDisc
    // =========================================================

    @Test
    @DisplayName("deleteDisc: валідний id — викликає dao.delete")
    void deleteDisc_valid_callsDelete() throws Exception {
        discService.deleteDisc(1);
        verify(discDao, times(1)).delete(1);
    }

    @Test
    @DisplayName("deleteDisc: SQLException → ServiceException")
    void deleteDisc_daoThrows_wraps() throws Exception {
        doThrow(new SQLException("del fail")).when(discDao).delete(anyInt());
        ServiceException ex = assertThrows(ServiceException.class,
                () -> discService.deleteDisc(1));
        assertTrue(ex.getMessage().contains("del fail"));
    }

    // =========================================================
    //  getAllDiscs
    // =========================================================

    @Test
    @DisplayName("getAllDiscs: повертає список з DAO")
    void getAllDiscs_returnsDaoResult() throws Exception {
        List<Disc> expected = List.of(disc, new Disc(2, "Second Album"));
        when(discDao.findAll()).thenReturn(expected);

        List<Disc> result = discService.getAllDiscs();
        assertEquals(2, result.size());
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("getAllDiscs: SQLException → ServiceException")
    void getAllDiscs_daoThrows_wraps() throws Exception {
        when(discDao.findAll()).thenThrow(new SQLException("read fail"));
        assertThrows(ServiceException.class, () -> discService.getAllDiscs());
    }

    // =========================================================
    //  calculateTotalDuration
    // =========================================================

    @Test
    @DisplayName("calculateTotalDuration: сума тривалостей усіх треків")
    void calculateTotalDuration_sumOfTracks() {
        disc.addTrack(rock);     // 208
        disc.addTrack(pop);      // 233
        disc.addTrack(jazz);     // 300
        // 208 + 233 + 300 = 741
        assertEquals(741, discService.calculateTotalDuration(disc));
    }

    @Test
    @DisplayName("calculateTotalDuration: порожній диск → 0")
    void calculateTotalDuration_emptyDisc() {
        assertEquals(0, discService.calculateTotalDuration(disc));
    }

    @Test
    @DisplayName("calculateTotalDuration: один трек")
    void calculateTotalDuration_singleTrack() {
        disc.addTrack(classical); // 900 сек
        assertEquals(900, discService.calculateTotalDuration(disc));
    }

    // =========================================================
    //  formatTotalDuration
    // =========================================================

    @Test
    @DisplayName("formatTotalDuration: менше години — формат 'Xm Ys'")
    void formatTotalDuration_lessThanHour() {
        disc.addTrack(rock);  // 208 = 3m 28s
        disc.addTrack(pop);   // 233 = 3m 53s
        // 441 сек = 7m 21s
        String result = discService.formatTotalDuration(disc);
        assertEquals("7m 21s", result);
    }

    @Test
    @DisplayName("formatTotalDuration: рівно 60 хвилин — формат '1h 0m 0s'")
    void formatTotalDuration_exactlyOneHour() {
        RockTrack oneHour = new RockTrack(10, "Long Song", "Artist", 3600, "");
        disc.addTrack(oneHour);
        assertEquals("1h 0m 0s", discService.formatTotalDuration(disc));
    }

    @Test
    @DisplayName("formatTotalDuration: більше години — формат 'Xh Ym Zs'")
    void formatTotalDuration_moreThanHour() {
        // 3661 сек = 1h 1m 1s
        RockTrack longTrack = new RockTrack(10, "T", "A", 3661, "");
        disc.addTrack(longTrack);
        assertEquals("1h 1m 1s", discService.formatTotalDuration(disc));
    }

    @Test
    @DisplayName("formatTotalDuration: порожній диск → '0m 0s'")
    void formatTotalDuration_emptyDisc() {
        assertEquals("0m 0s", discService.formatTotalDuration(disc));
    }

    @Test
    @DisplayName("formatTotalDuration: 90 секунд → '1m 30s'")
    void formatTotalDuration_90sec() {
        disc.addTrack(new RockTrack(5, "T", "A", 90, ""));
        assertEquals("1m 30s", discService.formatTotalDuration(disc));
    }

    // =========================================================
    //  sortTracksByGenre
    // =========================================================

    @Test
    @DisplayName("sortTracksByGenre: сортує за назвою жанру алфавітно")
    void sortTracksByGenre_alphabeticalOrder() {
        // Classical < Jazz < Pop < Rock
        disc.addTrack(rock);
        disc.addTrack(pop);
        disc.addTrack(jazz);
        disc.addTrack(classical);

        List<MusicTrack> sorted = discService.sortTracksByGenre(disc);

        assertEquals("Classical", sorted.get(0).getGenre());
        assertEquals("Jazz",      sorted.get(1).getGenre());
        assertEquals("Pop",       sorted.get(2).getGenre());
        assertEquals("Rock",      sorted.get(3).getGenre());
    }

    @Test
    @DisplayName("sortTracksByGenre: не змінює оригінальний диск")
    void sortTracksByGenre_doesNotModifyOriginalDisc() {
        disc.addTrack(rock);
        disc.addTrack(classical);
        List<MusicTrack> originalOrder = new ArrayList<>(disc.getTracks());

        discService.sortTracksByGenre(disc);

        // оригінальний диск — без змін
        assertEquals(originalOrder.get(0).getGenre(), disc.getTracks().get(0).getGenre());
        assertEquals(originalOrder.get(1).getGenre(), disc.getTracks().get(1).getGenre());
    }

    @Test
    @DisplayName("sortTracksByGenre: вже відсортований — повертає той самий порядок")
    void sortTracksByGenre_alreadySorted_unchanged() {
        disc.addTrack(classical); // Classical
        disc.addTrack(jazz);      // Jazz

        List<MusicTrack> sorted = discService.sortTracksByGenre(disc);
        assertEquals("Classical", sorted.get(0).getGenre());
        assertEquals("Jazz",      sorted.get(1).getGenre());
    }

    @Test
    @DisplayName("sortTracksByGenre: один трек — список без змін")
    void sortTracksByGenre_singleTrack() {
        disc.addTrack(rock);
        List<MusicTrack> sorted = discService.sortTracksByGenre(disc);
        assertEquals(1, sorted.size());
        assertEquals(rock, sorted.get(0));
    }

    @Test
    @DisplayName("sortTracksByGenre: порожній диск — порожній список")
    void sortTracksByGenre_emptyDisc() {
        List<MusicTrack> sorted = discService.sortTracksByGenre(disc);
        assertTrue(sorted.isEmpty());
    }

    @Test
    @DisplayName("sortTracksByGenre: однаковий жанр — порядок між ними стабільний (вставки)")
    void sortTracksByGenre_sameGenre_stable() {
        RockTrack r1 = new RockTrack(1, "Song A", "Artist1", 200, "Punk");
        RockTrack r2 = new RockTrack(2, "Song B", "Artist2", 250, "Metal");
        disc.addTrack(r2);
        disc.addTrack(r1);

        List<MusicTrack> sorted = discService.sortTracksByGenre(disc);
        // обидва Rock, порядок між ними не змінюється (вставки стабільна)
        assertEquals(2, sorted.size());
        assertEquals("Rock", sorted.get(0).getGenre());
        assertEquals("Rock", sorted.get(1).getGenre());
    }

    @Test
    @DisplayName("sortTracksByGenre: зворотній порядок — повністю переставляє")
    void sortTracksByGenre_reverseOrder_sorted() {
        disc.addTrack(rock);      // Rock
        disc.addTrack(pop);       // Pop
        disc.addTrack(jazz);      // Jazz
        disc.addTrack(classical); // Classical

        List<MusicTrack> sorted = discService.sortTracksByGenre(disc);
        // очікуємо: Classical, Jazz, Pop, Rock
        assertEquals("Classical", sorted.get(0).getGenre());
        assertEquals("Jazz",      sorted.get(1).getGenre());
        assertEquals("Pop",       sorted.get(2).getGenre());
        assertEquals("Rock",      sorted.get(3).getGenre());
    }
}
