package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class AnalysisController {

    private Stage stage;
    private Scene scene;
    private Parent parent;

    @FXML
    private Button selectButton;

    @FXML
    private ImageView imageView;

    public AnalysisController()  {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("analysis.fxml"));
        fxmlLoader.setController(this);
        try {
            parent = (Parent) fxmlLoader.load();
            // set height and width here for this login scene
            scene = new Scene(parent, 1000, 800);
        } catch (IOException ex) {
            System.out.println("Error displaying analysis window");
            throw new RuntimeException(ex);
        }

        FXMLLoader uploadLoader = new FXMLLoader(getClass().getResource("upload.fxml"));
        try {
            uploadLoader.load();
        } catch (IOException ex) {
            System.out.println("Error getting Upload Controller");
            throw new RuntimeException(ex);
        }

        UploadController upload = new ;
        File imgFile = upload.getImageFile();

        // Set image thumbnail
        Image img = new Image(imgFile.toURI().toString());
        imageView.setImage(img);

        return;
    }

    public void launchAnalysisScene(Stage stage) {
        this.stage = stage;
        stage.setScene(scene);


        stage.hide();
        stage.show();
    }


}
