package uk.ac.cam.cl.bravo.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uk.ac.cam.cl.bravo.dataset.ImageSample;
import uk.ac.cam.cl.bravo.pipeline.Rated;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class AnalysisController {

    private static final int MIN_PIXELS = 10;

    private MainController mainController;
    private ImageExplorerController imageExplorerController;
    private MatchListController matchListController;
    private Stage stage;
    private InformationPanelController informationPanelController;
    private ImageExplorerController activeExplorerController;
    private DatasetUploaderController datasetUploaderController;

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
    private List<Rated<ImageSample>> normalList;


    public AnalysisController(Stage stage) {
        this.stage = stage;
    }

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
            informationPanelController.launch();
            informationPanelController.setAnalysisController(this);

            ObservableList<View> items = FXCollections.observableArrayList(View.INPUT, View.NORMAL, View.NORMAL_OVER);

            pane1choice.setItems(items);
            pane2choice.setItems(items);
            pane3choice.setItems(items);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setActiveExplorer(ImageExplorerController active) {
        activeExplorerController = active;
        informationPanelController.setActiveController(active);
    }

    //  CREATE SCROLLABLE IMAGE PANE
    public void setPaneImage(GridPane pane, ImageSample imgFile, View view) {
        try {
            // Initialize controller
            FXMLLoader imageExplorerLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/imageExplorer.fxml"));
            imageExplorerController = new ImageExplorerController(stage, view, pane);
            imageExplorerLoader.setController(imageExplorerController);
            Parent imageExplorerFXML = imageExplorerLoader.load();

            pane.add(imageExplorerFXML, 0, 1);

            // Child controller actions
            Image img = SwingFXUtils.toFXImage(imgFile.loadImage(), null);
            imageExplorerController.setImage(img);
            imageExplorerController.setImageSample(imgFile);
            imageExplorerController.setAnalysisController(this);
            imageExplorerController.setMainController(this.mainController);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //  CREATE SCROLLABLE IMAGE PANE
    public void setPaneImage(GridPane pane, Image imgFile, View view) {
        try {

            if (pane.getChildren().size()>1)
                pane.getChildren().remove(1);

            // Initialize controller
            FXMLLoader imageExplorerLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/imageExplorer.fxml"));
            imageExplorerController = new ImageExplorerController(stage, view, pane);
            imageExplorerLoader.setController(imageExplorerController);
            Parent imageExplorerFXML = imageExplorerLoader.load();

            pane.add(imageExplorerFXML, 0, 1);

            // Child controller actions
            imageExplorerController.setImage(imgFile);
            imageExplorerController.setAnalysisController(this);
            imageExplorerController.setMainController(this.mainController);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public void showThirdExplorer(boolean bool) {
        if (bool) {
            grid.getColumnConstraints().get(0).setPercentWidth(30);
            grid.getColumnConstraints().get(2).setPercentWidth(30);
            grid.getColumnConstraints().get(4).setPercentWidth(30);
        }

        addBox.setVisible(!bool);
        addBox.setManaged(!bool);
        linkBox2.setVisible(bool);
        linkBox2.setManaged(bool);
        pane3.setVisible(bool);
        pane3.setManaged(bool);
    }

    @FXML
    protected void handleAddExplorerButtonAction(ActionEvent event) throws IOException {
        showThirdExplorer(true);
        System.out.println("removing!!");
    }

    @FXML
    protected void handleSelectViewPane1(ActionEvent e) {
        View choice = (View) pane1choice.getSelectionModel().getSelectedItem();
        Image img = null;
        ImageSample imgS = null;
        if (!(mainController.getInputImage() == null)) {
            img = mainController.getInputImage();
            setPaneImage(pane1, img, choice);
        } else if (choice.equals(View.NORMAL)) {
            if (!(getBestMatchNormal() == null)) {
                imgS = getBestMatchNormal().getValue();
                setPaneImage(pane1, imgS, choice);
            }
        }
        informationPanelController.setView(choice);
    }

    @FXML
    protected void handleSelectViewPane2(ActionEvent e) {
        View choice = (View) pane2choice.getSelectionModel().getSelectedItem();
        Image img = null;
        ImageSample imgS = null;
        if (choice.equals(View.INPUT)) {
            if (!(mainController.getInputImage() == null)) {
                img = mainController.getInputImage();
                setPaneImage(pane2, img, choice);
            }
        } else if (choice.equals(View.NORMAL)) {
            if (!(getBestMatchNormal() == null)) {
                imgS = getBestMatchNormal().getValue();
                setPaneImage(pane2, imgS, choice);
            }
        } else if (choice.equals(View.NORMAL_OVER)) {
            if (!(getBestMatchNormal() == null)) {
                img = SwingFXUtils.toFXImage(getBestMatchNormal().getValue().loadImage(), null);
                setPaneImage(pane2, img, choice);
            }
        }

        informationPanelController.setView(choice);
    }

    @FXML
    protected void handleSelectViewPane3(ActionEvent e) {
        View choice = (View) pane3choice.getSelectionModel().getSelectedItem();
        Image img = null;
        ImageSample imgS = null;
        if (choice == View.INPUT) {
            if (!(mainController.getInputImage() == null)) {
                img = mainController.getInputImage();
                setPaneImage(pane3, img, choice);
            }
        } else if (choice.equals(View.NORMAL)) {
            if (!(getBestMatchNormal() == null)) {
                imgS = getBestMatchNormal().getValue();
                setPaneImage(pane2, imgS, choice);
            }
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }


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


    public InformationPanelController getInformationPanelController() {
        return informationPanelController;
    }

    public ImageExplorerController getImageExplorerController() {
        return imageExplorerController;
    }

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
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Rated<ImageSample> getBestMatchNormal() {
        if (!(normalList == null))
            return normalList.get(0);
        else
            return null;
    }

    public void setNormalList(List<Rated<ImageSample>> normals) {
        this.normalList = normals;
        ImageSample img = getBestMatchNormal().getValue();
        setPaneImage(pane2, img, View.NORMAL);
    }

    public void showTrans() {
        BufferedImage buffImg = mainController.getMainPipeline().getImageToOverlay().onNext(activeExplorerController.getCurrentImage());
        Image img = SwingFXUtils.toFXImage(buffImg, null);
        setPaneImage(activeExplorerController.getCurrentPane(), img, View.NORMAL_OVER);
    }
}