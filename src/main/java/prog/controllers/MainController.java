package prog.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prog.dao.DatabaseManager;
import prog.dao.DiscDao;
import prog.dao.TrackDao;
import prog.models.Disc;
import prog.models.MusicTrack;
import prog.services.DiscService;
import prog.services.EmailNotificationService;
import prog.exception.ServiceException;
import prog.services.TrackService;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML private TableView<MusicTrack> tracksTable;
    @FXML private TableColumn<MusicTrack, Integer> colTrackId;
    @FXML private TableColumn<MusicTrack, String>  colTrackTitle;
    @FXML private TableColumn<MusicTrack, String>  colTrackArtist;
    @FXML private TableColumn<MusicTrack, String>  colTrackGenre;
    @FXML private TableColumn<MusicTrack, String>  colTrackDuration;

    @FXML private TextField minDurField;
    @FXML private TextField maxDurField;

    @FXML private TableView<Disc> discsTable;
    @FXML private TableColumn<Disc, Integer> colDiscId;
    @FXML private TableColumn<Disc, String>  colDiscTitle;
    @FXML private TableColumn<Disc, String>  colDiscDuration;

    @FXML private TableView<MusicTrack> discTracksTable;
    @FXML private TableColumn<MusicTrack, String> colDtTitle;
    @FXML private TableColumn<MusicTrack, String> colDtArtist;
    @FXML private TableColumn<MusicTrack, String> colDtGenre;
    @FXML private TableColumn<MusicTrack, String> colDtDuration;

    @FXML private Label totalDurationLabel;

    private TrackService trackService;
    private DiscService discService;
    private EmailNotificationService emailService;

    private ObservableList<MusicTrack> tracksList = FXCollections.observableArrayList();
    private ObservableList<Disc>       discsList  = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        Connection conn = DatabaseManager.getInstance().getConnection();
        trackService = new TrackService(new TrackDao(conn));
        discService  = new DiscService(new DiscDao(conn));
        emailService = new EmailNotificationService();

        setupTrackTable();
        setupDiscTable();
        loadTracks();
        loadDiscs();
    }

    private void setupTrackTable() {
        colTrackId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTrackTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTrackArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colTrackGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colTrackDuration.setCellValueFactory(new PropertyValueFactory<>("formattedDuration"));
        tracksTable.setItems(tracksList);
    }

    private void setupDiscTable() {
        colDiscId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDiscTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        discsTable.setItems(discsList);

        colDtTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDtArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colDtGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colDtDuration.setCellValueFactory(new PropertyValueFactory<>("formattedDuration"));

        // Клік на диск — показуємо його треки
        discsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        showDiscTracks(newVal);
                    }
                }
        );
    }

    private void loadTracks() {
        try {
            List<MusicTrack> tracks = trackService.getAllTracks();
            tracksList.setAll(tracks);
        } catch (ServiceException e) {
            showError("Помилка завантаження треків", e.getMessage());
            emailService.sendException("loadTracks", e);
        }
    }

    private void loadDiscs() {
        try {
            List<Disc> discs = discService.getAllDiscs();
            discsList.setAll(discs);
        } catch (ServiceException e) {
            showError("Помилка завантаження дисків", e.getMessage());
            emailService.sendException("loadDiscs", e);
        }
    }

    private void showDiscTracks(Disc disc) {
        ObservableList<MusicTrack> tracks = FXCollections.observableArrayList(disc.getTracks());
        discTracksTable.setItems(tracks);
        totalDurationLabel.setText("Загальна тривалість: " + discService.formatTotalDuration(disc));
    }

    @FXML
    private void onAddTrack() {
        openTrackDialog(null);
    }

    @FXML
    private void onEditTrack() {
        MusicTrack selected = tracksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Оберіть трек для редагування");
            return;
        }
        openTrackDialog(selected);
    }

    @FXML
    private void onDeleteTrack() {
        MusicTrack selected = tracksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Оберіть трек для видалення");
            return;
        }
        if (confirmAction("Видалити трек '" + selected.getTitle() + "'?")) {
            try {
                trackService.deleteTrack(selected.getId());
                loadTracks();
            } catch (ServiceException e) {
                showError("Помилка видалення", e.getMessage());
                emailService.sendException("deleteTrack", e);
            }
        }
    }

    @FXML
    private void onSearchByDuration() {
        String minStr = minDurField.getText().trim();
        String maxStr = maxDurField.getText().trim();

        if (minStr.isEmpty() || maxStr.isEmpty()) {
            showWarning("Введіть мінімум та максимум тривалості (у секундах)");
            return;
        }

        try {
            int min = Integer.parseInt(minStr);
            int max = Integer.parseInt(maxStr);
            List<MusicTrack> result = trackService.findByDurationRange(min, max);
            tracksList.setAll(result);
            if (result.isEmpty()) {
                showInfo("Треки не знайдено у вказаному діапазоні");
            }
        } catch (NumberFormatException e) {
            showWarning("Введіть коректні числові значення");
        } catch (ServiceException e) {
            showError("Помилка пошуку", e.getMessage());
        }
    }

    @FXML
    private void onClearSearch() {
        minDurField.clear();
        maxDurField.clear();
        loadTracks();
    }

    @FXML
    private void onCreateDisc() {
        openDiscDialog(null);
    }

    @FXML
    private void onEditDisc() {
        Disc selected = discsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Оберіть диск для редагування");
            return;
        }
        openDiscDialog(selected);
    }

    @FXML
    private void onDeleteDisc() {
        Disc selected = discsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Оберіть диск для видалення");
            return;
        }
        if (confirmAction("Видалити диск '" + selected.getTitle() + "'?")) {
            try {
                discService.deleteDisc(selected.getId());
                loadDiscs();
                discTracksTable.setItems(FXCollections.emptyObservableList());
                totalDurationLabel.setText("Загальна тривалість: —");
            } catch (ServiceException e) {
                showError("Помилка видалення", e.getMessage());
                emailService.sendException("deleteDisc", e);
            }
        }
    }

    @FXML
    private void onSortByGenre() {
        Disc selected = discsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Оберіть диск для сортування");
            return;
        }
        List<MusicTrack> sorted = discService.sortTracksByGenre(selected);
        selected.setTracks(sorted);
        try {
            discService.updateDisc(selected);
            showDiscTracks(selected);
            showInfo("Треки відсортовано за жанром та збережено");
        } catch (ServiceException e) {
            showError("Помилка збереження", e.getMessage());
        }
    }

    private void openTrackDialog(MusicTrack trackToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/TrackDialog.fxml"));
            Stage dialog = new Stage();
            dialog.setTitle(trackToEdit == null ? "Додати трек" : "Редагувати трек");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(loader.load()));

            TrackDialogController controller = loader.getController();
            controller.setTrack(trackToEdit);
            dialog.showAndWait();

            if (controller.isSaved()) {
                MusicTrack track = controller.getResult();
                if (trackToEdit == null) {
                    trackService.addTrack(track);
                } else {
                    trackService.updateTrack(track);
                }
                loadTracks();
            }
        } catch (IOException | ServiceException e) {
            showError("Помилка діалогу треку", e.getMessage());
            logger.error("Track dialog error", e);
        }
    }

    private void openDiscDialog(Disc discToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DiscDialog.fxml"));
            Stage dialog = new Stage();
            dialog.setTitle(discToEdit == null ? "Створити диск" : "Редагувати диск");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(loader.load()));

            DiscDialogController controller = loader.getController();
            controller.setData(discToEdit, tracksList);
            dialog.showAndWait();

            if (controller.isSaved()) {
                Disc disc = controller.getResult();
                if (discToEdit == null) {
                    discService.saveDisc(disc);
                } else {
                    discService.updateDisc(disc);
                }
                loadDiscs();
            }
        } catch (IOException | ServiceException e) {
            showError("Помилка діалогу диска", e.getMessage());
            logger.error("Disc dialog error", e);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Попередження");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Інформація");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirmAction(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження");
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}