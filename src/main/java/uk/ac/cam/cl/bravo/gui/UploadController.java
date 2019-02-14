package uk.ac.cam.cl.bravo.gui;

import java.io.File;
import java.io.IOException;
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
import javafx.stage.FileChooser.ExtensionFilter;

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
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/uk/ac/cam/cl/bravo/gui/upload.fxml"));
        fxmlLoader.setController(this);

        try {
            this.parent = (Parent)fxmlLoader.load();
            this.scene = new Scene(this.parent, 1000.0D, 800.0D);
        } catch (IOException var3) {
            System.out.println("Error displaying upload window");
            throw new RuntimeException(var3);
        }
    }

    public void displayUploadController(Stage stage) {
        this.stage = stage;
        stage.setScene(this.scene);
        stage.hide();
        stage.show();
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
        AnalysisController ac = new AnalysisController(this.imgFile);
        ac.launchAnalysisScene(this.stage);
    }
}