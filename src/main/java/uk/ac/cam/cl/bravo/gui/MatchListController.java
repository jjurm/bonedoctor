package uk.ac.cam.cl.bravo.gui;

import io.reactivex.Observer;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.ImageSample;
import uk.ac.cam.cl.bravo.pipeline.Rated;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Displays the list of matches. Loaded from InformationPanelController.
 * Responsible for loading list of matches from the pipeline.
 */
public class MatchListController {

    @FXML
    private ListView matches;
    private Stage stage;
    private AnalysisController analysisController;
    private List<Image> imgNormalList = new ArrayList<>();

    private View view;
    private MainController mainController;
    private ImageExplorerController activeController;

    /**
     * Constructor: initialises non-FXML-dependant elements.
     * Call before launcher.
     * */
    public MatchListController(Stage stage) {
        this.stage = stage;

    }

    /**
     * Resets the list of matches when a new view is selected. May not be needed after we removed Abnormal matches.
     * @param view
     */
    public void setView(View view) {
        this.view = view;
        launch();
    }

    /**
     * Launcher. Initializes FXML dependant elements.
     * Called after constructor.
     */
    public void launch() {
        // ------ CREATE SCROLLABLE LIST VIEW --------

        mainController.getMainPipeline().getSimilarNormal().subscribe(normals -> startUIChange(normals));
        return;
    }
    //TODO: Change bc of pipeline

    /**
     * Threading function, makes sure UI calls don't interfere with mainPipeline.
     * @param normals
     */
    private void startUIChange(List<Rated<ImageSample>> normals) {
        Platform.runLater(() -> createMatchList(normals));
    }
    //TODO: Change bc of pipeline

    /**
     * Creates the listview, populated by the list of normal images.
     * @param normals
     */
    private void createMatchList(List<Rated<ImageSample>> normals) {


        analysisController.setNormalList(normals);
        ObservableList<String> list = FXCollections.observableArrayList();
        for (Rated<ImageSample> r: normals){ // TODO: Change bc of pipeline
            double score = ((r.getScore() * -1.0) + 1)*100/2 ;
            String matchConf = new DecimalFormat("#.#").format(score);
            list.add("Patient: " + r.getValue().getPatient() + "    Match Confidence: " + matchConf + "%");
        }

        matches.setItems(list);

        matches.setCellFactory(param -> new ListCell<String>() {
            private ImageView matchView = new ImageView();

            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);

                matchView.setFitHeight(180);
                matchView.setFitWidth(180);
                matchView.setPreserveRatio(true);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
//                    matchView.setImage(normals.get(getIndex()));
                    //TODO: Change bc of pipeline
                    Image img = SwingFXUtils.toFXImage(normals.get(getIndex()).getValue().loadImage(), null);
                    matchView.setImage(img);
                    setText(name);
                    setGraphic(matchView);
                }
            }
        });

        // ----- ENABLE SELECT NEW MATCH -----

        matches.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                System.out.println("Clicked on " + matches.getSelectionModel().getSelectedIndex());
            }

            int index = matches.getSelectionModel().getSelectedIndex();

            ImageSample img = normals.get(index).getValue();
            analysisController.setPaneImage(activeController.getCurrentPane(), img, activeController.getView(), false);

        });
    }

    /**
     * Sets analysisController. Important for handing over the list of matches once it is loaded from the pipeline.
     * @param analysisController
     */
    public void setAnalysisController(AnalysisController analysisController) {
        this.analysisController = analysisController;
    }

    /**
     * Returns root component of the matchlist FXML.
     * @return
     */
    public ListView getMatches() {
        return this.matches;
    }

    /**
     * Hide list of matches
     */
    public void hide() {
        matches.setManaged(false);
        matches.setVisible(false);
    }

    /**
     * Show list of matches
     */
    public void show() {
        matches.setManaged(true);
        matches.setVisible(true);
    }

    /**
     * Sets main controller. Called from Information panel. Important for accessing the main pipeline.
     * @param mainController
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Set activeController. Called from information panel.
     * Important for knowing which panel to update once a different match is selected from the list.
     * @param activeController
     */
    public void setActiveController(ImageExplorerController activeController) {
        this.activeController = activeController;
    }
}