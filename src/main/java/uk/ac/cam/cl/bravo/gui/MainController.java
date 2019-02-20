package uk.ac.cam.cl.bravo.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController {

    private UploadController uploadController;
    private AnalysisController analysisController;
    private PipelineObserver pipelineObserver;
    private Stage stage;

    @FXML
    AnchorPane container;

    public MainController(Stage stage, PipelineObserver pipelineObserver) {
        this.stage = stage;
        this.pipelineObserver = pipelineObserver;

    }

    public void loadAnalysis (Image img) {
        if (!container.getChildren().isEmpty()) {
            container.getChildren().remove(0);
        }
        try {
            // Initialize controller
            FXMLLoader analysisLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/analysis.fxml"));
            analysisController = new AnalysisController(stage, pipelineObserver);
            analysisLoader.setController(analysisController);
            Parent analysisFXML = analysisLoader.load();

            ((Region) analysisFXML).prefWidthProperty().bind(container.widthProperty());
            ((Region) analysisFXML).prefHeightProperty().bind(container.heightProperty());
            container.getChildren().add(0, analysisFXML);

            // Child controller actions
            analysisController.setImage(img);
            analysisController.launch();
            pipelineObserver.addAnalysisController(analysisController);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadUpload () {
        if (!container.getChildren().isEmpty()) {
            container.getChildren().remove(0);
        }
        try {
            // Initialize controller
            FXMLLoader uploadLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/upload.fxml"));
            uploadController = new UploadController(stage, pipelineObserver);
            uploadLoader.setController(uploadController);
            Parent uploadFXML = uploadLoader.load();

            ((Region) uploadFXML).prefWidthProperty().bind(container.widthProperty());
            ((Region) uploadFXML).prefHeightProperty().bind(container.heightProperty());
            container.getChildren().add(0, uploadFXML);

            // Child controller actions
            uploadController.setMainController(this);
            pipelineObserver.addUploadController(uploadController);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStage(Stage newStage) {
        stage = newStage;
    }

    public void setPipelineObserver(PipelineObserver obs) {
        pipelineObserver = obs;
    }

    public void launch() {

        loadUpload();
    }
}
