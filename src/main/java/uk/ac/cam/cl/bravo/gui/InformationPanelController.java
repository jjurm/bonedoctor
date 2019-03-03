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

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setAnalysisController(AnalysisController analysisController) {
        this.analysisController = analysisController;
        matchListController.setAnalysisController(analysisController);
    }

    @FXML
    public void handleUpload() {
        analysisController.startDatasetUpload();
    }

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

    public MatchListController getMatchListController() {
        return matchListController;
    }

    public void setBoneCondition(Uncertain<BoneCondition> boneCondition) {
//        this.boneCondition.setText(boneCondition.getValue().getLabel());
//        this.boneConditionConfidence.setText(boneCondition.getConfidence().toString());
    }

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

    public void hide() {
        infoGrid.setVisible(false);
        infoGrid.setManaged(false);
    }

    public void showTrans() {
        analysisController.showTrans();
    }

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
