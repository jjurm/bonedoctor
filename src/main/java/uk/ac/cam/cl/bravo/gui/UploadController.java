package uk.ac.cam.cl.bravo.gui;

import java.io.File;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class UploadController {

    private File imgFile;
    private MainController mainController;
    private Stage stage;
    private PipelineObserver pipelineObserver;

    @FXML
    private Button selectButton;
    @FXML
    private Text fileText;
    @FXML
    private ImageView imageView;
    @FXML
    private Button analyzeButton;


    public UploadController(Stage stage, PipelineObserver pipelineObserver) {
        this.stage = stage;
        this.pipelineObserver = pipelineObserver;
    }


    public void setMainController(MainController main) {
        mainController = main;
    }

    @FXML
    protected void handleSelectButtonAction(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter[]{new ExtensionFilter("Image Files", new String[]{"*.png", "*.jpg"})});
        fileChooser.setTitle("Open Image File");
        this.imgFile = fileChooser.showOpenDialog(this.stage);
        String fileName = this.imgFile.getName();
        this.fileText.setText(fileName);
        Image img = new Image(this.imgFile.toURI().toString());
        this.imageView.setImage(img);
    }

    @FXML
    protected void handleAnalyzeButtonAction(ActionEvent event) throws IOException {
        mainController.loadAnalysis(imageView.getImage());
    }

}