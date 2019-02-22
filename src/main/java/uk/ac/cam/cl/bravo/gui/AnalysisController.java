package uk.ac.cam.cl.bravo.gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class AnalysisController {

    private static final int MIN_PIXELS = 10;

    private MainController mainController;
    private ImageExplorerController imageExplorerController;
    private MatchListController matchListController;
    private Stage stage;
    private PipelineObserver pipelineObserver;

    @FXML
    private GridPane grid;
    @FXML
    private GridPane pane1;
    @FXML
    private GridPane linkBox1;
    @FXML
    private GridPane pane2;
    @FXML
    private GridPane linkBox2;
    @FXML
    private GridPane pane3;
    @FXML
    private GridPane addBox;

    public AnalysisController(Stage stage, PipelineObserver pipelineObserver) {
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

            grid.add(matchListFXML, 0, 2);

            // Child controller actions
            matchListController.launch();
            matchListController.setAnalysisController(this);
            pipelineObserver.addMatchListController(matchListController);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //  CREATE SCROLLABLE IMAGE PANE USER INPUT
    public void setPane1Image(Image imgFile) {
        try {
            // Initialize controller
            FXMLLoader imageExplorerLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/imageExplorer.fxml"));
            imageExplorerController = new ImageExplorerController(stage, pipelineObserver);
            imageExplorerLoader.setController(imageExplorerController);
            Parent imageExplorerFXML = imageExplorerLoader.load();

            if (pane1.getChildren().size() > 1) {
                pane1.getChildren().set(1, imageExplorerFXML);
            } else {
                pane1.add(imageExplorerFXML,0, 1 );
            }

            // Child controller actions
            imageExplorerController.setImage(imgFile);
            imageExplorerController.setAnalysisController(this);
        pipelineObserver.addImageExplorerController(imageExplorerController);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //  CREATE SCROLLABLE IMAGE PANE CLOSEST MATCH
    public void setPane2Image(Image imgFile) {
        try {
            // Initialize controller
            FXMLLoader imageExplorerLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/imageExplorer.fxml"));
            imageExplorerController = new ImageExplorerController(stage, pipelineObserver);
            imageExplorerLoader.setController(imageExplorerController);

            Parent imageExplorerFXML = imageExplorerLoader.load();

            if (pane2.getChildren().size() > 1) {
                pane2.getChildren().set(1, imageExplorerFXML);
            } else {
                pane2.add(imageExplorerFXML,0, 1 );            }

            // Child controller actions
            imageExplorerController.setImage(imgFile);
            imageExplorerController.setAnalysisController(this);
            pipelineObserver.addImageExplorerController(imageExplorerController);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //  CREATE SCROLLABLE IMAGE PANE
    public void setPane3Image(Image imgFile) {
        try {
            // Initialize controller
            FXMLLoader imageExplorerLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/imageExplorer.fxml"));
            imageExplorerController = new ImageExplorerController(stage, pipelineObserver);
            imageExplorerLoader.setController(imageExplorerController);
            Parent imageExplorerFXML = imageExplorerLoader.load();

            if (pane3.getChildren().size() > 1) {
                pane3.getChildren().set(1, imageExplorerFXML);
            } else {
                pane3.add(imageExplorerFXML,0, 1 );
            }

            // Child controller actions
            imageExplorerController.setImage(imgFile);
            imageExplorerController.setAnalysisController(this);
            pipelineObserver.addImageExplorerController(imageExplorerController);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showThirdExplorer(boolean bool) {
        addBox.setVisible(!bool);
        addBox.setManaged(!bool);
        linkBox2.setVisible(bool);
        linkBox2.setManaged(bool);
        pane3.setVisible(bool);
        pane3.setManaged(bool);
    }

    @FXML
    protected void handleAddExplorerButtonAction(ActionEvent event) throws IOException {
        showThirdExplorer(true);
        System.out.println("removing!!");
    }

}
