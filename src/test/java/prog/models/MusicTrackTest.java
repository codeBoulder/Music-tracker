package prog.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("MusicTrack hierarchy tests")
class MusicTrackTest {


    @Test
    @DisplayName("RockTrack: getGenre() повертає 'Rock'")
    void rockTrack_getGenre() {
        RockTrack track = new RockTrack(1, "Back in Black", "AC/DC", 255, "Hard Rock");
        assertEquals("Rock", track.getGenre());
    }

    @Test
    @DisplayName("RockTrack: зберігає та повертає всі поля")
    void rockTrack_fields() {
        RockTrack track = new RockTrack(5, "Bohemian Rhapsody", "Queen", 354, "Classic Rock");
        assertEquals(5,                "id",       () -> String.valueOf(track.getId()));
        assertEquals("Bohemian Rhapsody", track.getTitle());
        assertEquals("Queen",             track.getArtist());
        assertEquals(354,                 track.getDurationSeconds());
        assertEquals("Classic Rock",      track.getSubgenre());
    }

    @Test
    @DisplayName("RockTrack: сетер subgenre працює")
    void rockTrack_setSubgenre() {
        RockTrack track = new RockTrack(1, "T", "A", 200, "Punk");
        track.setSubgenre("Grunge");
        assertEquals("Grunge", track.getSubgenre());
    }

    @Test
    @DisplayName("RockTrack: toString містить жанр та назву")
    void rockTrack_toString() {
        RockTrack track = new RockTrack(1, "Smells Like Teen Spirit", "Nirvana", 301, "Grunge");
        String str = track.toString();
        assertTrue(str.contains("Rock"));
        assertTrue(str.contains("Smells Like Teen Spirit"));
        assertTrue(str.contains("Nirvana"));
        assertTrue(str.contains("Grunge"));
    }


    @Test
    @DisplayName("PopTrack: getGenre() повертає 'Pop'")
    void popTrack_getGenre() {
        PopTrack track = new PopTrack(2, "Shape of You", "Ed Sheeran", 233, true);
        assertEquals("Pop", track.getGenre());
    }

    @Test
    @DisplayName("PopTrack: hasVideoClip = true зберігається")
    void popTrack_hasVideoClipTrue() {
        PopTrack track = new PopTrack(2, "T", "A", 200, true);
        assertTrue(track.isHasVideoClip());
    }

    @Test
    @DisplayName("PopTrack: hasVideoClip = false зберігається")
    void popTrack_hasVideoClipFalse() {
        PopTrack track = new PopTrack(3, "T", "A", 200, false);
        assertFalse(track.isHasVideoClip());
    }

    @Test
    @DisplayName("PopTrack: сетер hasVideoClip працює")
    void popTrack_setHasVideoClip() {
        PopTrack track = new PopTrack(1, "T", "A", 200, false);
        track.setHasVideoClip(true);
        assertTrue(track.isHasVideoClip());
    }

    @Test
    @DisplayName("PopTrack: toString містить 'Yes' коли є відеокліп")
    void popTrack_toStringWithVideo() {
        PopTrack track = new PopTrack(1, "Thriller", "Michael Jackson", 357, true);
        assertTrue(track.toString().contains("Yes"));
    }

    @Test
    @DisplayName("PopTrack: toString містить 'No' коли немає відеокліпа")
    void popTrack_toStringNoVideo() {
        PopTrack track = new PopTrack(1, "T", "A", 200, false);
        assertTrue(track.toString().contains("No"));
    }

    @Test
    @DisplayName("JazzTrack: getGenre() повертає 'Jazz'")
    void jazzTrack_getGenre() {
        JazzTrack track = new JazzTrack(3, "So What", "Miles Davis", 565, "Modal");
        assertEquals("Jazz", track.getGenre());
    }

    @Test
    @DisplayName("JazzTrack: зберігає mood")
    void jazzTrack_mood() {
        JazzTrack track = new JazzTrack(3, "T", "A", 300, "Smooth");
        assertEquals("Smooth", track.getMood());
    }

    @Test
    @DisplayName("JazzTrack: сетер mood працює")
    void jazzTrack_setMood() {
        JazzTrack track = new JazzTrack(1, "T", "A", 300, "Bebop");
        track.setMood("Swing");
        assertEquals("Swing", track.getMood());
    }

    @Test
    @DisplayName("JazzTrack: toString містить mood")
    void jazzTrack_toString() {
        JazzTrack track = new JazzTrack(1, "Blue in Green", "Miles Davis", 337, "Smooth");
        assertTrue(track.toString().contains("Smooth"));
        assertTrue(track.toString().contains("Jazz"));
    }

    @Test
    @DisplayName("ClassicalTrack: getGenre() повертає 'Classical'")
    void classicalTrack_getGenre() {
        ClassicalTrack track = new ClassicalTrack(4, "Moonlight Sonata", "Beethoven", 900, "Romantic");
        assertEquals("Classical", track.getGenre());
    }

    @Test
    @DisplayName("ClassicalTrack: зберігає composerEra")
    void classicalTrack_era() {
        ClassicalTrack track = new ClassicalTrack(4, "T", "A", 400, "Baroque");
        assertEquals("Baroque", track.getComposerEra());
    }

    @Test
    @DisplayName("ClassicalTrack: сетер composerEra працює")
    void classicalTrack_setEra() {
        ClassicalTrack track = new ClassicalTrack(1, "T", "A", 400, "Baroque");
        track.setComposerEra("Modern");
        assertEquals("Modern", track.getComposerEra());
    }

    @Test
    @DisplayName("ClassicalTrack: toString містить era")
    void classicalTrack_toString() {
        ClassicalTrack track = new ClassicalTrack(1, "Symphony No.5", "Beethoven", 1980, "Romantic");
        assertTrue(track.toString().contains("Romantic"));
        assertTrue(track.toString().contains("Classical"));
    }

    @Test
    @DisplayName("getFormattedDuration: 0 секунд → '0:00'")
    void formattedDuration_zero() {
        RockTrack t = new RockTrack(1, "T", "A", 0, "");
        assertEquals("0:00", t.getFormattedDuration());
    }

    @Test
    @DisplayName("getFormattedDuration: 65 секунд → '1:05'")
    void formattedDuration_65sec() {
        RockTrack t = new RockTrack(1, "T", "A", 65, "");
        assertEquals("1:05", t.getFormattedDuration());
    }

    @Test
    @DisplayName("getFormattedDuration: 3600 секунд → '60:00'")
    void formattedDuration_3600sec() {
        RockTrack t = new RockTrack(1, "T", "A", 3600, "");
        assertEquals("60:00", t.getFormattedDuration());
    }

    @Test
    @DisplayName("getFormattedDuration: 213 секунд → '3:33'")
    void formattedDuration_213sec() {
        RockTrack t = new RockTrack(1, "T", "A", 213, "");
        assertEquals("3:33", t.getFormattedDuration());
    }

    @Test
    @DisplayName("MusicTrack: сетер id працює")
    void baseTrack_setId() {
        RockTrack t = new RockTrack(0, "T", "A", 100, "");
        t.setId(42);
        assertEquals(42, t.getId());
    }

    @Test
    @DisplayName("MusicTrack: сетер title працює")
    void baseTrack_setTitle() {
        RockTrack t = new RockTrack(1, "Old", "A", 100, "");
        t.setTitle("New Title");
        assertEquals("New Title", t.getTitle());
    }

    @Test
    @DisplayName("MusicTrack: сетер artist працює")
    void baseTrack_setArtist() {
        RockTrack t = new RockTrack(1, "T", "Old Artist", 100, "");
        t.setArtist("New Artist");
        assertEquals("New Artist", t.getArtist());
    }

    @Test
    @DisplayName("MusicTrack: сетер durationSeconds працює")
    void baseTrack_setDuration() {
        RockTrack t = new RockTrack(1, "T", "A", 100, "");
        t.setDurationSeconds(300);
        assertEquals(300, t.getDurationSeconds());
    }

    @Test
    @DisplayName("MusicTrack: toString базовий містить жанр, виконавця та тривалість")
    void baseTrack_toStringContainsBasicInfo() {
        PopTrack t = new PopTrack(1, "Song", "Artist", 125, false);
        String str = t.toString();
        assertTrue(str.contains("Pop"));
        assertTrue(str.contains("Song"));
        assertTrue(str.contains("Artist"));
        assertTrue(str.contains("2:05")); // 125 сек = 2:05
    }
}
