package uk.ac.cam.cl.bravo.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.BoneCondition;
import uk.ac.cam.cl.bravo.dataset.ImageSample;
import uk.ac.cam.cl.bravo.pipeline.MainPipeline;
import uk.ac.cam.cl.bravo.pipeline.Uncertain;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InformationPanelController {

    private Stage stage;
    private MatchListController matchListController;
    private MainController mainController;
    private View view;
    private AnalysisController analysisController;

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

    public InformationPanelController(Stage stage) {
        this.stage = stage;
    }

    public void launch() {
        try {
            addToDatasetButton.setOnAction(event -> handleUpload());

            // Initialize controller
            FXMLLoader matchListLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/matchList.fxml"));
            matchListController = new MatchListController(stage);
            matchListLoader.setController(matchListController);
            Parent matchListFXML = matchListLoader.load();

            matchListController.getMatches().setPrefHeight(200);

            infoGrid.add(matchListFXML, 0, 1);

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
}
