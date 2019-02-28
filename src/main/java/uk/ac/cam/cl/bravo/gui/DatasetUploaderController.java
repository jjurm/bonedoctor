package uk.ac.cam.cl.bravo.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import uk.ac.cam.cl.bravo.pipeline.MainPipeline;

public class DatasetUploaderController {

    public DatasetUploaderController(Stage stage, MainPipeline pipeline){

    }

    @FXML
    Button uploadImageButton;

    @FXML
    ComboBox<String> bodyPartListDropdown;

    @FXML
    ImageView uploadedImageView;

    @FXML
    private void handleUploadButton(){
    }
}
