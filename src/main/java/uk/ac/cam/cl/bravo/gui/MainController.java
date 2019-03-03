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
import uk.ac.cam.cl.bravo.dataset.BoneCondition;
import uk.ac.cam.cl.bravo.dataset.ImageSample;
import uk.ac.cam.cl.bravo.pipeline.MainPipeline;
import uk.ac.cam.cl.bravo.pipeline.Rated;
import uk.ac.cam.cl.bravo.pipeline.Uncertain;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.channels.Pipe;
import java.util.List;

public class MainController {

    private List<Rated<ImageSample>> normalList;
    private List<Rated<ImageSample>> abnormalList;

    private UploadController uploadController;
    private AnalysisController analysisController;
    private MainPipeline mainPipeline = new MainPipeline();
    private Stage stage;
    private Image inputImage;
    private Image bestMatchNormal;
    private Image bestMatchAbnormal;

    @FXML
    AnchorPane container;


    public MainController(Stage stage) {
        this.stage = stage;
    }

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
            inputImage = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/img2.png"));
            bestMatchAbnormal = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/img3.png"));
            bestMatchNormal = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/img4.png"));
            analysisController.setPaneImage(analysisController.pane2, bestMatchAbnormal, View.ABNORMAL);
            analysisController.setPaneImage(analysisController.pane3, bestMatchNormal, View.NORMAL);
            analysisController.showThirdExplorer(false);

            analysisController.launch();
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

    public void setNormalList(List<Rated<ImageSample>> imageSamples) {
        normalList = imageSamples;
    }

    public void setAbnormalList(List<Rated<ImageSample>> imageSamples) {
        abnormalList = imageSamples;
    }

    public Image getBestMatchNormal() {
//        BufferedImage img = normalList.get(0).getValue().loadImage();
//        WritableImage writableImage = SwingFXUtils.toFXImage(img, null);

        return bestMatchNormal;
    }

    public Image getBestMatchAbnormal() {
//        BufferedImage img = abnormalList.get(0).getValue().loadImage();
//        WritableImage writableImage = SwingFXUtils.toFXImage(img, null);
        return bestMatchAbnormal;
    }

    public Image getInputImage() {
        return inputImage;
    }


    public void launch() {
        loadUpload();
    }


    public AnalysisController getAnalysisController() {
        return analysisController;
    }

    public UploadController getUploadController() {
        return uploadController;
    }

}