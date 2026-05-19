package prog.models;

public class RockTrack extends MusicTrack {

    private String subgenre;

    public RockTrack(int id, String title, String artist, int durationSeconds, String subgenre) {
        super(id, title, artist, durationSeconds);
        this.subgenre = subgenre;
    }

    @Override
    public String getGenre() {
        return "Rock";
    }

    public String getSubgenre() {
        return subgenre;
    }

    public void setSubgenre(String subgenre) {
        this.subgenre = subgenre;
    }

    @Override
    public String toString() {
        return super.toString() + " | Subgenre: " + subgenre;
    }
}
