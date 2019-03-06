package uk.ac.cam.cl.bravo.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uk.ac.cam.cl.bravo.dataset.ImageSample;
import uk.ac.cam.cl.bravo.pipeline.Rated;
import uk.ac.cam.cl.bravo.pipeline.Uncertain;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The main window where all operations take place.
 * Is loaded in the MainController.
 */
public class AnalysisController {

    private static final int MIN_PIXELS = 10;

    private MainController mainController;
    private ImageExplorerController imageExplorerController;
    private MatchListController matchListController;
    private Stage stage;
    private InformationPanelController informationPanelController;
    private ImageExplorerController activeExplorerController;
    private DatasetUploaderController datasetUploaderController;
    private BufferedImage highlightedImage;
    private Image inputPreProcessed;

    @FXML
    private GridPane grid;
    @FXML
    public GridPane pane1;
    @FXML
    private GridPane linkBox1;
    @FXML
    public GridPane pane2;
    @FXML
    private GridPane linkBox2;
    @FXML
    public GridPane pane3;
    @FXML
    private GridPane addBox;
    @FXML
    private ComboBox pane1choice;
    @FXML
    private ComboBox pane2choice;
    @FXML
    private ComboBox pane3choice;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ProgressBar progressBar2;

    private List<Rated<ImageSample>> normalList;


    /**
     * Constructor: initialises non-FXML-dependant elements.
     * Call before launcher.
     * */
    public AnalysisController(Stage stage) {
        this.stage = stage;
    }

    /**
     * Launcher: initialises FXML-dependent elements.
     * Call after constructor.
     */
    public void launch() {
        try {
            // Initialize controller
            FXMLLoader informationPanelLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/informationPanel.fxml"));
            informationPanelController = new InformationPanelController(stage);
            informationPanelLoader.setController(informationPanelController);
            Parent informationPanelFXML = informationPanelLoader.load();

            grid.add(informationPanelFXML, 0, 2);

            // Child controller actions
            informationPanelController.setMainController(mainController);
            informationPanelController.setAnalysisController(this);
            informationPanelController.launch();


        ObservableList<View> items = FXCollections.observableArrayList(View.INPUT, View.NORMAL, View.NORMAL_OVER, View.HIGHLIGHT);

            pane1choice.setItems(items);
            pane2choice.setItems(items);
            pane3choice.setItems(items);

            subscribe();
            mainController.getMainPipeline().getFracturesHighlighted().subscribe(image -> highlightedImage = image);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the current "Active Explorer", i.e. which image pane the user is currently interacting with.
     * Important for the InformationPanel, as this will only display information about the active explorer.
     * Called by an ImageExplorerController when it is clicked on.
     * @param active
     */
    public void setActiveExplorer(ImageExplorerController active) {
        activeExplorerController = active;
        if (!(informationPanelController == null))
            informationPanelController.setActiveController(active);
    }

    /**
     * Creates a new ImageExplorer with updated state about pane, view and whether or not it is a preprocessed image.
     * Places it in the requested pane.
     * @param pane: which pane to insert the explorer into
     * @param imgFile: which image to insert
     * @param view: View.INPUT, View.NORMAL or View.NORMAL_OVER (overlay)
     * @param usePreProcessed: is this image using preprocessing?
     */
    public void setPaneImage(GridPane pane, ImageSample imgFile, View view, boolean usePreProcessed) {
        try {

            if (pane.getChildren().size()>1)
                pane.getChildren().remove(1);

            // Initialize controller
            FXMLLoader imageExplorerLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/imageExplorer.fxml"));
            imageExplorerController = new ImageExplorerController(stage, view, pane, usePreProcessed);
            imageExplorerLoader.setController(imageExplorerController);
            Parent imageExplorerFXML = imageExplorerLoader.load();

            pane.add(imageExplorerFXML, 0, 1);
            imageExplorerController.setAnalysisController(this);

            // Child controller actions
            Image img = null;
            if (usePreProcessed) {
                img = SwingFXUtils.toFXImage(imgFile.loadPreprocessedImage(), null);
            } else
                img = SwingFXUtils.toFXImage(imgFile.loadImage(), null);
            imageExplorerController.setImage(img);
            imageExplorerController.setImageSample(imgFile);
            imageExplorerController.setMainController(this.mainController);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new ImageExplorer with updated state about pane, view and whether or not it is a preprocessed image.
     * Places it in the requested pane.
     * Only for Input image, as it cannot be made into an ImageSample.
     * @param pane: which pane to insert the explorer into
     * @param imgFile: which image to insert
     * @param view: View.INPUT, View.NORMAL or View.NORMAL_OVER (overlay)
     */
    public void setPaneImage(GridPane pane, Image imgFile, View view, boolean usePreprocessed) {
        try {

            if (pane.getChildren().size()>1)
                pane.getChildren().remove(1);

            // Initialize controller
            FXMLLoader imageExplorerLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/imageExplorer.fxml"));
            imageExplorerController = new ImageExplorerController(stage, view, pane, usePreprocessed);
            imageExplorerLoader.setController(imageExplorerController);
            Parent imageExplorerFXML = imageExplorerLoader.load();

            pane.add(imageExplorerFXML, 0, 1);

            // Child controller actions
            Image img = null;
            if (usePreprocessed) {
                img = inputPreProcessed;
            } else
                img = imgFile;
            imageExplorerController.setAnalysisController(this);
            imageExplorerController.setImage(img);
            imageExplorerController.setMainController(this.mainController);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Opens a third image explorer if the uses clicks "Add explorer" button.
     * Can also be closed by setting the boolean to false.  // TODO: Add close button?
     * @param bool
     */
    public void showThirdExplorer(boolean bool) {
        if (bool) {
            grid.getColumnConstraints().get(0).setPercentWidth(33.3);
            grid.getColumnConstraints().get(1).setPercentWidth(0);
            grid.getColumnConstraints().get(2).setPercentWidth(33.3);
            grid.getColumnConstraints().get(3).setPercentWidth(0);
            grid.getColumnConstraints().get(4).setPercentWidth(100-33.3-33.3);
        }

        addBox.setVisible(!bool);
        addBox.setManaged(!bool);
        linkBox2.setVisible(bool);
        linkBox2.setManaged(bool);
        pane3.setVisible(bool);
        pane3.setManaged(bool);
    }

    /**
     * Handle function for "Add Explorer" button
     * @param event
     * @throws IOException
     */
    @FXML
    protected void handleAddExplorerButtonAction(ActionEvent event) throws IOException {
        showThirdExplorer(true);
        System.out.println("removing!!");
    }

    /**
     * Handle function for "Select View" dropdown menu for first panel/explorer.
     * @param e
     */
    @FXML
    protected void handleSelectViewPane1(ActionEvent e) {
        View choice = (View) pane1choice.getSelectionModel().getSelectedItem();
        Image img = null;
        ImageSample imgS = null;
        if (!(mainController.getInputImage() == null)) {
            img = mainController.getInputImage();
            setPaneImage(pane1, img, choice, false);
        } else if (choice.equals(View.NORMAL)) {
            if (!(getBestMatchNormal() == null)) {
                imgS = getBestMatchNormal().getValue();
                setPaneImage(pane1, imgS, choice, false);
            }
        }
        informationPanelController.setView(choice);
    }

    /**
     * Handle function for "Select View" dropdown menu for second panel/explorer.
     * @param e
     */
    @FXML
    protected void handleSelectViewPane2(ActionEvent e) {
        View choice = (View) pane2choice.getSelectionModel().getSelectedItem();
        Image img = null;
        ImageSample imgS = null;
        if (choice.equals(View.INPUT)) {
            if (!(mainController.getInputImage() == null)) {
                img = mainController.getInputImage();
                setPaneImage(pane2, img, choice, false);
            }
        } else if (choice.equals(View.NORMAL)) {
            if (!(getBestMatchNormal() == null)) {
                imgS = getBestMatchNormal().getValue();
                setPaneImage(pane2, imgS, choice, false);
            }
        } else if (choice.equals(View.NORMAL_OVER)) {
            if (!(getBestMatchNormal() == null)) {
                img = SwingFXUtils.toFXImage(getBestMatchNormal().getValue().loadImage(), null);
                setPaneImage(pane2, img, choice, false);
            }
        }

        informationPanelController.setView(choice);
    }

    /**
     * Handle function for "Select View" dropdown menu for third panel/explorer.
     * @param e
     */
    @FXML
    protected void handleSelectViewPane3(ActionEvent e) {
        View choice = (View) pane3choice.getSelectionModel().getSelectedItem();
        Image img = null;
        ImageSample imgS = null;
        if (choice == View.INPUT) {
            if (!(mainController.getInputImage() == null)) {
                img = mainController.getInputImage();
                setPaneImage(pane3, img, choice, false);
            }
        } else if (choice.equals(View.NORMAL)) {
            if (!(getBestMatchNormal() == null)) {
                imgS = getBestMatchNormal().getValue();
                setPaneImage(pane3, imgS, choice, false);
            }
        } else if (choice.equals(View.INPUT.HIGHLIGHT)){
            setPaneImage(pane3, SwingFXUtils.toFXImage(highlightedImage, null), choice, false);
            informationPanelController.setView(View.HIGHLIGHT);
        }
    }

    /**
     * Set the MainController. Important for accessing the mainPipeline.
     * Called from mainController after analysisController is loaded..
     * @param mainController
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Handle function for "Export" button, allows user to save image in imageexplorer.
     * @param e
     */
    public void handleSaveFile(ActionEvent e) {
        Image img = mainController.getInputImage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Image");
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(img,
                        null), "png", file);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    /**
     * Getter used by DatasetUploaderController
     * @return the informationPanelController.
     */
    public InformationPanelController getInformationPanelController() {
        return informationPanelController;
    }

    /**
     * Panel for uploading image to the dataset. Will appear instead of the informationPanel.
     * Launched when "Add to dataset" button is clicked.
     */
    public void startDatasetUpload() {
        try {
            // Initialize controller
            FXMLLoader datasetUploaderLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/uploadToDataset.fxml"));
            datasetUploaderController = new DatasetUploaderController(stage);
            datasetUploaderLoader.setController(datasetUploaderController);
            Parent datasetUploaderFXML = datasetUploaderLoader.load();

            informationPanelController.hide();
            grid.add(datasetUploaderFXML, 0, 2);

            // Child controller actions
            datasetUploaderController.setAnalysisController(this);
            datasetUploaderController.setMainController(this.mainController);
            datasetUploaderController.launch();

            // Set the height
            datasetUploaderController.setHeight(200.0);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Getter
     * @return best match from the match list
     */
    public Rated<ImageSample> getBestMatchNormal() {
        if (!(normalList == null))
            return normalList.get(0);
        else
            return null;
    }

    /**
     * Setter, used by the matchListController through our informationPanelController.
     * @param normals: the list of matches returned from the pipeline.
     */
    public void setNormalList(List<Rated<ImageSample>> normals) {
        this.normalList = normals;
        ImageSample img = getBestMatchNormal().getValue();
        setPaneImage(pane2, img, View.NORMAL, false);
    }

    /**
     * Show transformed image/image overlay
     */
    @FXML
    public void showTrans() {

//        BufferedImage buffImg = mainController.getMainPipeline().getImageToOverlay().onNext(activeExplorerController.getCurrentImage());
//        Image img = SwingFXUtils.toFXImage(buffImg, null);
//        setPaneImage(activeExplorerController.getCurrentPane(), img, View.NORMAL_OVER);
    }


    private void updateProgressBar(double progress) {
        progressBar.setProgress(progress);
        progressBar2.setProgress(progress);
    }

    /**
     * Threading function, makes sure UI calls don't interfere with mainPipeline.
     * @param progress
     */
    private void startUIChange(double progress) {
        Platform.runLater(() -> updateProgressBar(progress));
    }

    private void startUIChange(Uncertain<BufferedImage> pp) {
        Platform.runLater(() -> {this.inputPreProcessed = SwingFXUtils.toFXImage(pp.getValue(), null);});
    }

    private void subscribe() {
        mainController.getMainPipeline().getProgress().subscribe(item -> {startUIChange(item);});
        mainController.getMainPipeline().getPreprocessed().subscribe(pp -> startUIChange(pp));
    }


}