package prog.models;

import java.util.ArrayList;
import java.util.List;

public class Disc {

    private int id;
    private String title;
    private List<MusicTrack> tracks;

    public Disc(int id, String title) {
        this.id = id;
        this.title = title;
        this.tracks = new ArrayList<>();
    }

    public void addTrack(MusicTrack track) {
        tracks.add(track);
    }


    public void removeTrack(int index) {
        if (index >= 0 && index < tracks.size()) {
            tracks.remove(index);
        }
    }


    public List<MusicTrack> getTracks() {
        return new ArrayList<>(tracks);
    }


    public void setTracks(List<MusicTrack> tracks) {
        this.tracks = new ArrayList<>(tracks);
    }

    @Override
    public String toString() {
        return String.format("Disc #%d: \"%s\" (%d tracks)", id, title, tracks.size());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
