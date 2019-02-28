package uk.ac.cam.cl.bravo.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InformationPanelController {

    private Stage stage;
    private PipelineObserver pipelineObserver;
    private MatchListController matchListController;
    private AnalysisController analysisController;

    private static String INPUT = "Input Image";
    private static String NORMAL = "Best Match, Normal";
    private static String ABNORMAL = "Best Match, Abormal";
    private static String ABNORMAL_OVER = "Overlay, Abormal";
    private static String NORMAL_OVER = "Overlay, Normal";

    @FXML
    GridPane infoGrid;
    @FXML
    FlowPane inputFlow;
    @FXML
    FlowPane bestMatchFlow;

    public InformationPanelController(Stage stage, PipelineObserver pipelineObserver) {
        this.stage = stage;
        this.pipelineObserver = pipelineObserver;
    }

    public void launch() {
        try {
            // Initialize controller
            FXMLLoader matchListLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/matchList.fxml"));
            matchListController = new MatchListController(stage, pipelineObserver);
            matchListLoader.setController(matchListController);
            Parent matchListFXML = matchListLoader.load();

            matchListController.getMatches().setPrefHeight(200);

            infoGrid.add(matchListFXML, 0, 1);

            // Child controller actions
            matchListController.launch();
            pipelineObserver.addMatchListController(matchListController);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAnalysisController(AnalysisController analysisController) {
        this.analysisController = analysisController;
        matchListController.setAnalysisController(analysisController);
    }

    public void setView(String choiceText) {
        matchListController.setView(choiceText);
        matchListController.launch();
        if (choiceText == INPUT) {
            inputFlow.setVisible(true);
            inputFlow.setManaged(true);
            bestMatchFlow.setVisible(false);
            bestMatchFlow.setManaged(false);
        } else {
            inputFlow.setVisible(false);
            inputFlow.setVisible(false);
            bestMatchFlow.setVisible(true);
            bestMatchFlow.setManaged(true);
        }
    }
}
