package prog.models;

public class ClassicalTrack extends MusicTrack {

    private String composerEra; // наприклад: "Baroque", "Romantic", "Modern"

    public ClassicalTrack(int id, String title, String artist, int durationSeconds, String composerEra) {
        super(id, title, artist, durationSeconds);
        this.composerEra = composerEra;
    }

    @Override
    public String getGenre() {
        return "Classical";
    }

    public String getComposerEra() {
        return composerEra;
    }

    public void setComposerEra(String composerEra) {
        this.composerEra = composerEra;
    }

    @Override
    public String toString() {
        return super.toString() + " | Era: " + composerEra;
    }
}
