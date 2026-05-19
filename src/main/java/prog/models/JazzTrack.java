package prog.models;

public class JazzTrack extends MusicTrack {

    private String mood; // наприклад: "Smooth", "Bebop", "Swing"

    public JazzTrack(int id, String title, String artist, int durationSeconds, String mood) {
        super(id, title, artist, durationSeconds);
        this.mood = mood;
    }

    @Override
    public String getGenre() {
        return "Jazz";
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    @Override
    public String toString() {
        return super.toString() + " | Mood: " + mood;
    }
}
