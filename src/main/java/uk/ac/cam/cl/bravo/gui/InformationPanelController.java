package uk.ac.cam.cl.bravo.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.BoneCondition;
import uk.ac.cam.cl.bravo.dataset.ImageSample;
import uk.ac.cam.cl.bravo.pipeline.MainPipeline;
import uk.ac.cam.cl.bravo.pipeline.Uncertain;
import javafx.scene.control.CheckBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    FlowPane bestMatchFlow;
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
            matchListController.launch();
            setView(View.INPUT);
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
        matchListController.setAnalysisController(analysisController);
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
            matchListController.hide();
        } else {
            inputFlow.setVisible(false);
            inputFlow.setVisible(false);
            bestMatchFlow.setVisible(true);
            bestMatchFlow.setManaged(true);
            if (view == View.NORMAL ) {
                System.out.println("Normal");
                matchListController.show();
            }
        }
    }

    /**
     * Set bone condition label
     * @param boneCondition
     */
    public void setBoneCondition(Uncertain<BoneCondition> boneCondition) {
// TODO:       this.boneCondition.setText(boneCondition.getValue().getLabel());
//        this.boneConditionConfidence.setText(boneCondition.getConfidence().toString());
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
        mainPipeline.getBoneCondition().subscribe(item -> {reportBoneCondition(item);});
    }


    public void reportBoneCondition(@NotNull Uncertain<BoneCondition> boneCondition) {
        this.setBoneCondition(boneCondition);
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
        ImageSample currImage = activeController.getCurrentImage();
        GridPane pane = activeController.getCurrentPane();
        View view = activeController.getView();
        if (preprocessedCheckBox.isSelected()) {
            analysisController.setPaneImage(pane, currImage, view, true);
        } else {
            analysisController.setPaneImage(pane, currImage, view, false);
        }
    }
}
