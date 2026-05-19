package prog.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Юніт-тести для класу Disc.
 */
@DisplayName("Disc model tests")
class DiscTest {

    private Disc disc;
    private RockTrack rock;
    private PopTrack  pop;
    private JazzTrack jazz;

    @BeforeEach
    void setUp() {
        disc = new Disc(1, "My Collection");
        rock = new RockTrack(1, "Highway to Hell", "AC/DC",       208, "Hard Rock");
        pop  = new PopTrack(2,  "Shape of You",   "Ed Sheeran",   233, true);
        jazz = new JazzTrack(3, "So What",         "Miles Davis",  565, "Modal");
    }

    // --- Конструктор і базові гетери ---

    @Test
    @DisplayName("Конструктор встановлює id та title")
    void constructor_setsIdAndTitle() {
        assertEquals(1,               disc.getId());
        assertEquals("My Collection", disc.getTitle());
    }

    @Test
    @DisplayName("Новий диск має порожній список треків")
    void newDisc_emptyTracks() {
        assertTrue(disc.getTracks().isEmpty());
    }

    // --- addTrack ---

    @Test
    @DisplayName("addTrack додає трек до диска")
    void addTrack_addsOneTrack() {
        disc.addTrack(rock);
        assertEquals(1, disc.getTracks().size());
        assertEquals(rock, disc.getTracks().get(0));
    }

    @Test
    @DisplayName("addTrack може додати декілька треків")
    void addTrack_addsMultipleTracks() {
        disc.addTrack(rock);
        disc.addTrack(pop);
        disc.addTrack(jazz);
        assertEquals(3, disc.getTracks().size());
    }

    @Test
    @DisplayName("addTrack зберігає порядок додавання")
    void addTrack_preservesOrder() {
        disc.addTrack(rock);
        disc.addTrack(pop);
        List<MusicTrack> tracks = disc.getTracks();
        assertEquals(rock, tracks.get(0));
        assertEquals(pop,  tracks.get(1));
    }

    // --- removeTrack ---

    @Test
    @DisplayName("removeTrack видаляє трек за валідним індексом")
    void removeTrack_validIndex() {
        disc.addTrack(rock);
        disc.addTrack(pop);
        disc.removeTrack(0); // видаляємо rock
        assertEquals(1, disc.getTracks().size());
        assertEquals(pop, disc.getTracks().get(0));
    }

    @Test
    @DisplayName("removeTrack ігнорує від'ємний індекс")
    void removeTrack_negativeIndex() {
        disc.addTrack(rock);
        disc.removeTrack(-1);
        assertEquals(1, disc.getTracks().size()); // нічого не видалено
    }

    @Test
    @DisplayName("removeTrack ігнорує індекс за межами списку")
    void removeTrack_outOfBoundIndex() {
        disc.addTrack(rock);
        disc.removeTrack(5);
        assertEquals(1, disc.getTracks().size()); // нічого не видалено
    }

    @Test
    @DisplayName("removeTrack на порожньому диску не кидає виняток")
    void removeTrack_emptyDisc() {
        assertDoesNotThrow(() -> disc.removeTrack(0));
    }

    // --- getTracks повертає копію ---

    @Test
    @DisplayName("getTracks повертає незалежну копію — зміна не впливає на диск")
    void getTracks_returnsDefensiveCopy() {
        disc.addTrack(rock);
        List<MusicTrack> copy = disc.getTracks();
        copy.add(pop); // модифікуємо зовнішній список
        // диск не змінився
        assertEquals(1, disc.getTracks().size());
    }

    // --- setTracks ---

    @Test
    @DisplayName("setTracks замінює весь список треків")
    void setTracks_replacesAllTracks() {
        disc.addTrack(rock);
        disc.setTracks(List.of(pop, jazz));
        List<MusicTrack> tracks = disc.getTracks();
        assertEquals(2, tracks.size());
        assertEquals(pop,  tracks.get(0));
        assertEquals(jazz, tracks.get(1));
    }

    @Test
    @DisplayName("setTracks зберігає порядок")
    void setTracks_preservesOrder() {
        disc.setTracks(List.of(jazz, rock, pop));
        List<MusicTrack> tracks = disc.getTracks();
        assertEquals(jazz, tracks.get(0));
        assertEquals(rock, tracks.get(1));
        assertEquals(pop,  tracks.get(2));
    }

    // --- Сетери базових полів ---

    @Test
    @DisplayName("setId та getId працюють")
    void setId_works() {
        disc.setId(99);
        assertEquals(99, disc.getId());
    }

    @Test
    @DisplayName("setTitle та getTitle працюють")
    void setTitle_works() {
        disc.setTitle("Best Hits");
        assertEquals("Best Hits", disc.getTitle());
    }

    // --- toString ---

    @Test
    @DisplayName("toString містить id, title та кількість треків")
    void toString_containsExpectedInfo() {
        disc.addTrack(rock);
        disc.addTrack(pop);
        String str = disc.toString();
        assertTrue(str.contains("1"));
        assertTrue(str.contains("My Collection"));
        assertTrue(str.contains("2"));
    }

    @Test
    @DisplayName("toString для порожнього диска — 0 треків")
    void toString_emptyDisc() {
        assertTrue(disc.toString().contains("0"));
    }
}
