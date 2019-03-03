package uk.ac.cam.cl.bravo.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uk.ac.cam.cl.bravo.dataset.Bodypart;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatasetUploaderController {
    private File chosenImage;
    private Bodypart chosenBodyPart;
    protected Stage stage;
    private AnalysisController analysisController;
    private MainController mainController;

    @FXML
    AnchorPane datasetUploadPane;

    @FXML
    Button selectImageButton;

    @FXML
    Button backButton;

    @FXML
    Button addDataButton;

    @FXML
    ComboBox bodypartSelector;

    @FXML
    ImageView uploadedImageView;


    public DatasetUploaderController(Stage stage){
        this.stage = stage;
    }

    public void launch(){
        // Display drop down list
        ObservableList<Bodypart> items = FXCollections.observableArrayList(
                Bodypart.ELBOW,
                Bodypart.FINGER,
                Bodypart.FOREARM,
                Bodypart.HUMERUS,
                Bodypart.SHOULDER,
                Bodypart.HAND,
                Bodypart.WRIST);

        bodypartSelector.setItems(items);
    }


    public void setHeight(double height){
        datasetUploadPane.setMinHeight(height);
    }

    @FXML
    public void handleSelectBodyPart(ActionEvent event){
        chosenBodyPart = (Bodypart) bodypartSelector.getSelectionModel().getSelectedItem();
    }

    @FXML
    public void handleUploadButton(ActionEvent event){
        System.out.println("CLICKED UPLOAD BUTTON");
        // Build dialog for choosing file, filtering only png and jpeg files
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter[]{new FileChooser.ExtensionFilter("Image Files", new String[]{"*.png", "*.jpg"})});
        fileChooser.setTitle("Open Image File");

        // Set the image chosen
        chosenImage = fileChooser.showOpenDialog(stage);

        // Display the image chosen
        uploadedImageView.setImage(new Image(chosenImage.toURI().toString()));
    }

    @FXML
    public void handleAddDataButton(ActionEvent event){
        System.out.println("Pressed Add Data Button");

        // Store the image in the root directory
        File rootDirectory = new File("userData");

        if (!rootDirectory.exists()){
            rootDirectory.mkdirs();
        }
        // Checks for input nulls
        if (chosenBodyPart == null || chosenImage == null){
            System.out.println("Chosen body part or image is empty. Please input these fields.");
        }
        else{
            // Save the file and corresponding bodypart according to filename
            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String imageFilenameToSave = String.format("%s_%s.png", chosenBodyPart, dateFormat.format(new Date()));

                File fileToWrite = new File("userData/" + imageFilenameToSave);
                ImageIO.write(ImageIO.read(chosenImage), "png", fileToWrite);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Set invisible pane after uploading
        datasetUploadPane.setVisible(false);

        // Show info panel again
        analysisController.getInformationPanelController().unhide();
    }

    @FXML
    public void handleBackButton(ActionEvent event){
        // Hide the whole dataset upload panel
        datasetUploadPane.setVisible(false);

        // Show info panel again
        analysisController.getInformationPanelController().unhide();
    }

    public void setAnalysisController(AnalysisController analysisController) {
        this.analysisController = analysisController;
    }
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
