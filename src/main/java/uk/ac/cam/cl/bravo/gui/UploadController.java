package uk.ac.cam.cl.bravo.gui;

import java.io.File;
import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import kotlin.Pair;
import uk.ac.cam.cl.bravo.dataset.Bodypart;
import uk.ac.cam.cl.bravo.pipeline.MainPipeline;

/**
 *  Holds the upload panel for the app, where the user inputs an image to the pipeline
 *  Is currently loaded in the MainController, might in the future be loaded from AnalysisController
 */
public class UploadController {

    private File imgFile = null;
    private Bodypart bodypart = null;
    private MainController mainController;
    private Stage stage;

    @FXML
    private Button selectButton;
    @FXML
    private Text fileText;
    @FXML
    private ImageView imageView;
    @FXML
    private Button analyzeButton;
    @FXML
    private ComboBox bodypartChoice;
    @FXML
    private Label analyzeCheck;


    /**
     * Constructor: initialises non-FXML-dependant elements.
     * Call before launcher.
     * */
    public UploadController(Stage stage) {
        this.stage = stage;


    }

    /**
     * Launcher: initialises FXML-dependent elements.
     * Call after constructor.
     */
    public void launch() {
        ObservableList<Bodypart> items = FXCollections.observableArrayList(Bodypart.ELBOW, Bodypart.FINGER, Bodypart.FOREARM, Bodypart.HUMERUS, Bodypart.SHOULDER, Bodypart.HAND, Bodypart.WRIST);
        bodypartChoice.setItems(items);
    }

    /**
     * Set the main controller, important to get access to the pipeline.
     * @param main: the main controller
     */
    public void setMainController(MainController main) {
        mainController = main;
    }

    /**
     * Handle function for "Select" button .
     * Opens a FileChooser with options to select an image of type PNG of JPEG.
     * @param event
     * @throws IOException
     */
    @FXML
    protected void handleSelectButtonAction(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter[]{new ExtensionFilter("Image Files", new String[]{"*.png", "*.jpeg"})});
        fileChooser.setTitle("Open Image File");
        this.imgFile = fileChooser.showOpenDialog(this.stage);
        String fileName = this.imgFile.getName();
        this.fileText.setText(fileName);
        Image img = new Image(this.imgFile.toURI().toString());
        this.imageView.setImage(img);
    }

    /**
     * Handle function from the "Analyze" button.
     * Will start the pipeline and open up the analysis screen,
     * where elements will appear as soon as they are available from the pipeline.
     * @param event
     * @throws IOException
     */
    @FXML
    protected void handleAnalyzeButtonAction(ActionEvent event) throws IOException {
        MainPipeline mainPipeline = mainController.getMainPipeline();

        if (imgFile == null || bodypart == null){
            analyzeCheck.setText("Please choose an image and the corresponding body part to proceed.");
        } else {
            mainController.setInputImage(new Image(this.imgFile.toURI().toString()));

//            Pair<String, Bodypart> userInput = new Pair<>(imgFile.getAbsolutePath(), bodypart);

//            mainPipeline.getUserInput().onNext(userInput);
//            mainPipeline.getUserInput().onComplete();
//            mainPipeline.getUserInput().onError(new Throwable());

            mainController.loadAnalysis(imageView.getImage());
        }
    }

    /**
     * Handle function for the bodypart selector dropdown menu.
     * Gets the selected bodypart andd stores it in the private UploadController.bodypart field.
     * @param event
     * @throws IOException
     */
    @FXML
    protected void handleSelectBodypart(ActionEvent event) throws IOException {
        this.bodypart = (Bodypart) bodypartChoice.getSelectionModel().getSelectedItem();
    }


}