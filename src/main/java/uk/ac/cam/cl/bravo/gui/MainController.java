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

public class MainController implements Initializable {

    private UploadController uploadController;
    private AnalysisController analysisController;
    private Stage stage;

    @FXML
    AnchorPane container;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        loadUpload();
    }

    public void loadAnalysis (Image img) {
        if (!container.getChildren().isEmpty()) {
            container.getChildren().remove(0);
        }
        try {
            FXMLLoader analysisLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/analysis.fxml"));
            Parent analysisFXML = analysisLoader.load();

            ((Region) analysisFXML).prefWidthProperty().bind(container.widthProperty());
            ((Region) analysisFXML).prefHeightProperty().bind(container.heightProperty());

            analysisController = analysisLoader.getController();
            analysisController.setUp(this, stage);
            analysisController.setImage(img);
            container.getChildren().add(0, analysisFXML);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadUpload () {
        if (!container.getChildren().isEmpty()) {
            container.getChildren().remove(0);
        }
        try {
            FXMLLoader uploadLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/upload.fxml"));
            Parent uploadFXML = uploadLoader.load();

            ((Region) uploadFXML).prefWidthProperty().bind(container.widthProperty());
            ((Region) uploadFXML).prefHeightProperty().bind(container.heightProperty());

            uploadController = uploadLoader.getController();
            uploadController.setUp(this, stage);
            container.getChildren().add(0, uploadFXML);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStage(Stage newStage) {
        stage = newStage;
    }

}
