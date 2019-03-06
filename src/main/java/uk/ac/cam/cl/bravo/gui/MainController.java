package uk.ac.cam.cl.bravo.gui;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import uk.ac.cam.cl.bravo.dataset.ImageSample;
import uk.ac.cam.cl.bravo.pipeline.MainPipeline;
import uk.ac.cam.cl.bravo.pipeline.Rated;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class MainController {

    private List<Rated<ImageSample>> normalList;

    private UploadController uploadController;
    private AnalysisController analysisController;
    private MainPipeline mainPipeline = new MainPipeline();
    private Stage stage;
    private Image inputImage;

    @FXML
    AnchorPane container;

    /**
     * Constructor: initialises non-FXML-dependant elements. Call before launcher.
     * */
    public MainController(Stage stage) {
        this.stage = stage;
    }

    /**
     * Get main pipeline, responsible for supplying all child componenets with the same mainPipeline object.
     * @return
     */
    public MainPipeline getMainPipeline() {
        return this.mainPipeline;
    }

    public void loadAnalysis (Image img) {
        if (!container.getChildren().isEmpty()) {
            container.getChildren().remove(0);
        }
        try {
            // Initialize controller
            FXMLLoader analysisLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/analysis.fxml"));
            analysisController = new AnalysisController(stage);
            analysisLoader.setController(analysisController);
            Parent analysisFXML = analysisLoader.load();

            ((Region) analysisFXML).prefWidthProperty().bind(container.widthProperty());
            ((Region) analysisFXML).prefHeightProperty().bind(container.heightProperty());
            container.getChildren().add(0, analysisFXML);

            // Child controller actions
            analysisController.setMainController(this);
            analysisController.setPaneImage(analysisController.pane1, img, View.INPUT);
            analysisController.showThirdExplorer(false);
            analysisController.launch();
            analysisController.setPaneImage(analysisController.pane1, img, View.INPUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: ANE - MOVE UPLOAD SCREEN TO SIDE OF ANALYSIS SCREEN
    public void loadUpload () {
        if (!container.getChildren().isEmpty()) {
            container.getChildren().remove(0);
        }
        try {
            // Initialize controller
            FXMLLoader uploadLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/upload.fxml"));
            uploadController = new UploadController(stage);
            uploadLoader.setController(uploadController);
            Parent uploadFXML = uploadLoader.load();

            ((Region) uploadFXML).prefWidthProperty().bind(container.widthProperty());
            ((Region) uploadFXML).prefHeightProperty().bind(container.heightProperty());
            container.getChildren().add(0, uploadFXML);

            // Child controller actions
            uploadController.setMainController(this);
            uploadController.launch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Launcher: initialises FXML-dependent elements. Call after constructor.
     */
    public void launch() {
        loadUpload();
    }

    /**
     * Getter for input image.
     * @return
     */
    public Image getInputImage() {
        return inputImage;
    }


    /**
     * Setter for input image.
     * @param image
     */
    public void setInputImage(Image image) {
        this.inputImage = image;
    }
}