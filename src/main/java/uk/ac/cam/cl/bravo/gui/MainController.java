package uk.ac.cam.cl.bravo.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    private UploadController uploadController;
    private AnalysisController analysisController;
    private PipelineObserver pipelineObserver;
    private Stage stage;
    private Image inputImage;
    private Image bestMatchNormal;
    private Image bestMatchAbnormal;

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
            analysisController.setPaneImage(analysisController.pane1, img);
            analysisController.setMainController(this);
            inputImage = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/bowtie.jpg"));
            bestMatchAbnormal = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/glasses.jpg"));
            bestMatchNormal = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/superthumb.jpg"));
            analysisController.setPaneImage(analysisController.pane2, bestMatchAbnormal);
            analysisController.setPaneImage(analysisController.pane3, bestMatchNormal);
            analysisController.showThirdExplorer(false);

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

    public Image getBestMatchNormal() {
        return bestMatchNormal;
    }

    public Image getBestMatchAbnormal() {
        return bestMatchAbnormal;
    }

    public Image getInputImage() {
        return inputImage; }

    public void launch() {

        loadUpload();
    }
}
