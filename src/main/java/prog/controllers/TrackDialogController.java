package prog.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import prog.models.*;


public class TrackDialogController {

    @FXML private TextField titleField;
    @FXML private TextField artistField;
    @FXML private TextField durationField;   // секунди
    @FXML private ComboBox<String> genreCombo;

    @FXML private Label     extraLabel;
    @FXML private TextField extraField;
    @FXML private CheckBox  hasVideoCheckBox; // тільки для Pop

    private boolean saved = false;
    private MusicTrack result = null;
    private int editingId = 0; // 0 = новий трек

    @FXML
    public void initialize() {
        genreCombo.getItems().addAll("Rock", "Pop", "Jazz", "Classical");
        genreCombo.getSelectionModel().selectFirst();
        updateExtraField("Rock");

        genreCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateExtraField(newVal);
            }
        });
    }

    public void setTrack(MusicTrack track) {
        if (track == null) return;

        editingId = track.getId();
        titleField.setText(track.getTitle());
        artistField.setText(track.getArtist());
        durationField.setText(String.valueOf(track.getDurationSeconds()));
        genreCombo.setValue(track.getGenre());

        if (track instanceof RockTrack rock) {
            extraField.setText(rock.getSubgenre());
        } else if (track instanceof PopTrack pop) {
            hasVideoCheckBox.setSelected(pop.isHasVideoClip());
        } else if (track instanceof JazzTrack jazz) {
            extraField.setText(jazz.getMood());
        } else if (track instanceof ClassicalTrack classical) {
            extraField.setText(classical.getComposerEra());
        }
    }


    private void updateExtraField(String genre) {
        extraField.setVisible(true);
        extraField.setManaged(true);
        hasVideoCheckBox.setVisible(false);
        hasVideoCheckBox.setManaged(false);

        switch (genre) {
            case "Rock"      -> { extraLabel.setText("Піджанр:"); extraField.setPromptText("Hard Rock, Punk..."); }
            case "Jazz"      -> { extraLabel.setText("Настрій:");  extraField.setPromptText("Smooth, Bebop..."); }
            case "Classical" -> { extraLabel.setText("Епоха:");    extraField.setPromptText("Baroque, Romantic..."); }
            case "Pop"       -> {
                extraLabel.setText("Є відеокліп:");
                extraField.setVisible(false);
                extraField.setManaged(false);
                hasVideoCheckBox.setVisible(true);
                hasVideoCheckBox.setManaged(true);
            }
        }
    }

    @FXML
    private void onSave() {
        String title  = titleField.getText().trim();
        String artist = artistField.getText().trim();
        String durStr = durationField.getText().trim();
        String genre  = genreCombo.getValue();

        if (title.isEmpty() || artist.isEmpty() || durStr.isEmpty()) {
            showWarning("Заповніть усі обов'язкові поля (назва, виконавець, тривалість)");
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durStr);
            if (duration <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showWarning("Тривалість має бути цілим позитивним числом (секунди)");
            return;
        }

        String extra = extraField.isVisible() ? extraField.getText().trim() : "";

        result = switch (genre) {
            case "Rock"      -> new RockTrack(editingId, title, artist, duration, extra);
            case "Pop"       -> new PopTrack(editingId, title, artist, duration, hasVideoCheckBox.isSelected());
            case "Jazz"      -> new JazzTrack(editingId, title, artist, duration, extra);
            case "Classical" -> new ClassicalTrack(editingId, title, artist, duration, extra);
            default          -> new RockTrack(editingId, title, artist, duration, extra);
        };

        saved = true;
        closeDialog();
    }

    @FXML
    private void onCancel() {
        saved = false;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Помилка вводу");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isSaved()       { return saved; }
    public MusicTrack getResult()  { return result; }
}
