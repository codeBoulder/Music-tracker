package prog.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import prog.models.Disc;
import prog.models.MusicTrack;

import java.util.ArrayList;
import java.util.List;

public class DiscDialogController {

    @FXML private TextField discTitleField;
    @FXML private ListView<MusicTrack> availableListView;
    @FXML private ListView<MusicTrack> selectedListView;

    private final ObservableList<MusicTrack> availableItems = FXCollections.observableArrayList();
    private final ObservableList<MusicTrack> selectedItems  = FXCollections.observableArrayList();

    private boolean saved = false;
    private Disc result = null;
    private int editingId = 0;

    @FXML
    public void initialize() {
        availableListView.setItems(availableItems);
        selectedListView.setItems(selectedItems);

        availableListView.setCellFactory(lv -> new TrackCell());
        selectedListView.setCellFactory(lv -> new TrackCell());
    }

    public void setData(Disc disc, ObservableList<MusicTrack> allTracks) {
        if (disc != null) {
            editingId = disc.getId();
            discTitleField.setText(disc.getTitle());

            if (disc.getTracks() != null) {
                for (MusicTrack track : disc.getTracks()) {
                    selectedItems.add(track);
                }
            }
        }

        for (MusicTrack track : allTracks) {
            boolean alreadySelected = false;
            for (MusicTrack sel : selectedItems) {
                if (sel.getId() == track.getId()) {
                    alreadySelected = true;
                    break;
                }
            }
            if (!alreadySelected) {
                availableItems.add(track);
            }
        }
    }

    @FXML
    private void onAddToDisc() {
        MusicTrack selected = availableListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            availableItems.remove(selected);
            selectedItems.add(selected);
        }
    }

    @FXML
    private void onRemoveFromDisc() {
        MusicTrack selected = selectedListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedItems.remove(selected);
            availableItems.add(selected);
        }
    }

    @FXML
    private void onMoveUp() {
        int idx = selectedListView.getSelectionModel().getSelectedIndex();
        if (idx > 0) {
            MusicTrack track = selectedItems.remove(idx);
            selectedItems.add(idx - 1, track);
            selectedListView.getSelectionModel().select(idx - 1);
        }
    }

    @FXML
    private void onMoveDown() {
        int idx = selectedListView.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < selectedItems.size() - 1) {
            MusicTrack track = selectedItems.remove(idx);
            selectedItems.add(idx + 1, track);
            selectedListView.getSelectionModel().select(idx + 1);
        }
    }

    @FXML
    private void onSave() {
        String title = discTitleField.getText().trim();
        if (title.isEmpty()) {
            showWarning("Введіть назву диска");
            return;
        }
        if (selectedItems.isEmpty()) {
            showWarning("Додайте хоча б один трек до диска");
            return;
        }

        result = new Disc(editingId, title);
        List<MusicTrack> tracks = new ArrayList<>(selectedItems);
        result.setTracks(tracks);

        saved = true;
        closeDialog();
    }

    @FXML
    private void onCancel() {
        saved = false;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) discTitleField.getScene().getWindow();
        stage.close();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Помилка вводу");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isSaved()  { return saved; }
    public Disc getResult()   { return result; }

    private static class TrackCell extends ListCell<MusicTrack> {
        @Override
        protected void updateItem(MusicTrack track, boolean empty) {
            super.updateItem(track, empty);
            if (empty || track == null) {
                setText(null);
            } else {
                setText(track.getArtist() + " — " + track.getTitle()
                        + "  (" + track.getFormattedDuration() + ")  [" + track.getGenre() + "]");
            }
        }
    }
}
