package prog.models;


public class PopTrack extends MusicTrack {

    private boolean hasVideoClip;

    public PopTrack(int id, String title, String artist, int durationSeconds, boolean hasVideoClip) {
        super(id, title, artist, durationSeconds);
        this.hasVideoClip = hasVideoClip;
    }

    @Override
    public String getGenre() {
        return "Pop";
    }

    public boolean isHasVideoClip() {
        return hasVideoClip;
    }

    public void setHasVideoClip(boolean hasVideoClip) {
        this.hasVideoClip = hasVideoClip;
    }

    @Override
    public String toString() {
        return super.toString() + " | Has video: " + (hasVideoClip ? "Yes" : "No");
    }
}
