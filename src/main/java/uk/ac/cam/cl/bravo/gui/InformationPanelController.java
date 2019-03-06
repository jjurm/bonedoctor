package uk.ac.cam.cl.bravo.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import uk.ac.cam.cl.bravo.dataset.BoneCondition;
import uk.ac.cam.cl.bravo.dataset.ImageSample;
import uk.ac.cam.cl.bravo.pipeline.MainPipeline;
import uk.ac.cam.cl.bravo.pipeline.Uncertain;
import javafx.scene.control.CheckBox;

import java.io.IOException;

/**
 * Contains information about the currently selected image panel.
 * Loaded in the AnalysisController
 */
public class InformationPanelController {

    private Stage stage;
    private MatchListController matchListController;
    private MainController mainController;
    private View view;
    private AnalysisController analysisController;
    private ImageExplorerController activeController;

    @FXML
    GridPane infoGrid;
    @FXML
    FlowPane inputFlow;
    @FXML
    FlowPane highlightFlow;
    @FXML
    FlowPane bestMatchFlow;
    @FXML
    GridPane highlightGrid;
    @FXML
    Button highlightButton;
    @FXML
    Slider highlightSlider;
    @FXML
    Slider searchSlider;
    @FXML
    Label boneCondition;
    @FXML
    Label boneConditionConfidence;
    @FXML
    Button addToDatasetButton;
    @FXML
    CheckBox transformCheckBox;
    @FXML
    CheckBox preprocessedCheckBox;

    /**
     * Constructor: initialises non-FXML-dependant elements.
     * Call before launcher.
     * */
    public InformationPanelController(Stage stage) {
        this.stage = stage;
    }

    /**
     * Launcher: initialises FXML-dependent elements. Call after constructor.
     */
    public void launch() {
        try {
            addToDatasetButton.setOnAction(event -> handleUpload());
            preprocessedCheckBox.setOnAction(event -> usePreprocessed());

            // Initialize controller
            FXMLLoader matchListLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/matchList.fxml"));
            matchListController = new MatchListController(stage);
            matchListLoader.setController(matchListController);
            Parent matchListFXML = matchListLoader.load();

            // TODO: Is this needed?
            matchListController.getMatches().setPrefHeight(200);

            infoGrid.add(matchListFXML, 0, 2);

            // Child controller actions
            matchListController.setMainController(mainController);
            matchListController.setAnalysisController(analysisController);
            matchListController.launch();
            setView(View.INPUT);
            subscribe();

            highlightButton.setOnMouseClicked(e -> {
                mainController.getMainPipeline().getHighlightGradient().onNext(highlightSlider.getValue());
                mainController.getMainPipeline().getHighlightAmount().onNext(searchSlider.getValue());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set mainController. Important for accessing mainPipeline.
     * @param mainController
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Set analysisController. Important for making changes to analysisController layout based on actions from this class.
     * Eg. when "Add to dataset" button is clicked.
     * @param analysisController
     */
    public void setAnalysisController(AnalysisController analysisController) {
        this.analysisController = analysisController;
    }

    /**
     * Handle function for "Add to dataset" button.
     * Will replace information panel with upload to dataset panel.
     */
    @FXML
    public void handleUpload() {
        analysisController.startDatasetUpload();
    }

    /**
     * Called when the used selects a different view from the dropdown menues in analysisController.
     * Will change between view for information panel to view for matches.
     * @param view
     */
    public void setView(View view) {
        this.view = view;
        matchListController.setView(view);
        matchListController.launch();

        if (view == View.INPUT) {
            inputFlow.setVisible(true);
            inputFlow.setManaged(true);
            bestMatchFlow.setVisible(false);
            bestMatchFlow.setManaged(false);
            highlightFlow.setManaged(false);
            highlightFlow.setVisible(false);
            matchListController.hide();
        } else if (view == View.NORMAL){
            inputFlow.setVisible(false);
            inputFlow.setVisible(false);
            bestMatchFlow.setVisible(true);
            bestMatchFlow.setManaged(true);
            highlightFlow.setManaged(false);
            highlightFlow.setVisible(false);
            addToDatasetButton.setVisible(false);
            addToDatasetButton.setManaged(false);
            matchListController.show();
        } else if (view == View.HIGHLIGHT){
            inputFlow.setVisible(false);
            inputFlow.setManaged(false);
            bestMatchFlow.setVisible(false);
            bestMatchFlow.setManaged(false);
            matchListController.hide();
            highlightFlow.setManaged(true);
            highlightFlow.setVisible(true);
            highlightGrid.setVisible(true);
        }

        if (!(activeController == null)) {
            if (activeController.isPreprocessed()) {
                preprocessedCheckBox.setSelected(true);
            } else {
                preprocessedCheckBox.setSelected(false);
            }
        }
    }

    /**
     * Threading function, makes sure UI calls don't interfere with mainPipeline.
     * @param boneCondition
     */
    private void startUIChange(Uncertain<BoneCondition> boneCondition) {
        Platform.runLater(() -> setBoneCondition(boneCondition));
    }

    /**
     * Set bone condition label
     * @param boneCondition
     */
    public void setBoneCondition(Uncertain<BoneCondition> boneCondition) {
        this.boneCondition.setText(boneCondition.getValue().getLabel());
        this.boneConditionConfidence.setText(boneCondition.getConfidence().toString());
    }

    /**
     * Called from analysisController on a change from a ImageExplorerController.
     * Will make sure the informationPanel shows information related to the active explorer.
     * @param active
     */
    public void setActiveController(ImageExplorerController active) {
        this.setView(active.getView());
        this.activeController = active;
        this.matchListController.setActiveController(active);
    }

    public void subscribe() {
        MainPipeline mainPipeline = mainController.getMainPipeline();
        mainPipeline.getBoneCondition().subscribe(item -> {startUIChange(item);});
    }


    /**
     * Hide whole panel
     */
    public void hide() {
        infoGrid.setVisible(false);
        infoGrid.setManaged(false);
    }

    /**
     * Show whole panel
     */
    public void unhide(){
        infoGrid.setVisible(true);
        infoGrid.setManaged(true);
    }

    /**
     * Handle button for "Enhance image" checkbox
     * Will set the active explorer to a preprocessed version of the image if selected.
     */
    @FXML
    protected void usePreprocessed() {
        ImageSample currImageSample = activeController.getCurrentImage();
        GridPane pane = activeController.getCurrentPane();
        View view = activeController.getView();
        if (pane.getChildren().size()>1)
            pane.getChildren().remove(1);
        if (preprocessedCheckBox.isSelected()) {
        if (this.activeController.getCurrentImage() == null) {
            Image img = activeController.getCurrentPlainImage();
            if (preprocessedCheckBox.isSelected()) {
                analysisController.setPaneImage(pane, img, view, true);
            } else {
                analysisController.setPaneImage(pane, img, view, false);
            }
        } else {
            if (preprocessedCheckBox.isSelected()) {
                analysisController.setPaneImage(pane, currImageSample, view, true);
            } else {
                analysisController.setPaneImage(pane, currImageSample, view, false);
            }
        }
    }
}
