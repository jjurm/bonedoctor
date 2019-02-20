package uk.ac.cam.cl.bravo.gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    private ScrollPane scrollPane = new ScrollPane();
    final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);

    @FXML
    private ListView matches;

    @FXML
    private VBox topBottom;

    @FXML
    private HBox imgExplorers;

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

            matchListFXML.maxHeight(topBottom.getMaxHeight()*0.75);
            topBottom.getChildren().add(2, matchListFXML);

            // Child controller actions
            matchListController.launch();
            pipelineObserver.addMatchListController(matchListController);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Box " + topBottom.heightProperty());
    }

    //  CREATE SCROLLABLE IMAGE PANE USER INPUT
    public void setUserImage(Image imgFile) {
        try {
            // Initialize controller
            FXMLLoader imageExplorerLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/imageExplorer.fxml"));
            imageExplorerController = new ImageExplorerController(stage, pipelineObserver);
            imageExplorerLoader.setController(imageExplorerController);
            Parent imageExplorerFXML = imageExplorerLoader.load();

            imageExplorerFXML.maxHeight(topBottom.getMaxHeight()*0.25);
            imgExplorers.getChildren().add(0, imageExplorerFXML);

            // Child controller actions
            imageExplorerController.setImage(imgFile);
            pipelineObserver.addImageExplorerController(imageExplorerController);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //  CREATE SCROLLABLE IMAGE PANE CLOSEST MATCH
    public void setMatchImage(Image imgFile) {
        try {
            // Initialize controller
            FXMLLoader imageExplorerLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/imageExplorer.fxml"));
            imageExplorerController = new ImageExplorerController(stage, pipelineObserver);
            imageExplorerLoader.setController(imageExplorerController);
            Parent imageExplorerFXML = imageExplorerLoader.load();

            imageExplorerFXML.maxHeight(topBottom.getMaxHeight()*0.25);
            imgExplorers.getChildren().add(1, imageExplorerFXML);

            // Child controller actions
            imageExplorerController.setImage(imgFile);
            pipelineObserver.addImageExplorerController(imageExplorerController);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
