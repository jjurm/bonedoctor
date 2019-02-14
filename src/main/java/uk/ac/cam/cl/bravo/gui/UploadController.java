package uk.ac.cam.cl.bravo.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class UploadController {

    private Parent parent;
    private Stage stage;
    private Scene scene;

    @FXML
    private Button selectButton;

    @FXML
    private Text fileText;

    @FXML
    private ImageView imageView;

    @FXML
    private Button analyzeButton;

    public File imgFile;


    public UploadController() {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("upload.fxml"));
        fxmlLoader.setController(this);
        try {
            parent = (Parent) fxmlLoader.load();
            // set height and width here for this login scene
            scene = new Scene(parent, 1000, 800);
        } catch (IOException ex) {
            System.out.println("Error displaying upload window");
            throw new RuntimeException(ex);
        }
    }

    public void displayUploadController(Stage stage) {
        this.stage = stage;
        stage.setScene(scene);

        // Must write
        stage.hide();
        stage.show();
    }


    @FXML
    protected void handleSelectButtonAction(ActionEvent event) throws IOException {
        // Make file chooser
        FileChooser fileChooser = new FileChooser();
        // Only image files should be selectable
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
        fileChooser.setTitle("Open Image File");
        imgFile = fileChooser.showOpenDialog(stage);

        // Set selected filename hint
        String fileName = imgFile.getName();
        fileText.setText(fileName);

        // Set image thumbnail
        Image img = new Image(imgFile.toURI().toString());
        imageView.setImage(img);


        return;
    }

    @FXML
    protected void handleAnalyzeButtonAction(ActionEvent event) throws IOException {
        AnalysisController ac = new AnalysisController();
        ac.launchAnalysisScene(stage);
    }

    public File getImageFile() {
        return imgFile;
    }
}
