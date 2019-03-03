package uk.ac.cam.cl.bravo.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uk.ac.cam.cl.bravo.dataset.BoneCondition;
import uk.ac.cam.cl.bravo.pipeline.Uncertain;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AnalysisController {

    private static final int MIN_PIXELS = 10;

    private MainController mainController;
    private ImageExplorerController imageExplorerController;
    private MatchListController matchListController;
    private Stage stage;
    private InformationPanelController informationPanelController;
    private ImageExplorerController activeExplorerController;


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

            ObservableList<View> items = FXCollections.observableArrayList(View.INPUT, View.NORMAL, View.ABNORMAL, View.NORMAL_OVER, View.ABNORMAL_OVER);

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
    public void setPaneImage(GridPane pane, Image imgFile) {
        try {
            // Initialize controller
            FXMLLoader imageExplorerLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/imageExplorer.fxml"));
            imageExplorerController = new ImageExplorerController(stage);
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
        String choiceText = pane1choice.getSelectionModel().getSelectedItem().toString();

        if (choiceText == View.INPUT.toString()) {
            setPaneImage(pane1, mainController.getInputImage());
            informationPanelController.setView(View.INPUT);
        } else if (choiceText == View.NORMAL.toString()) {
            setPaneImage(pane1, mainController.getBestMatchNormal());
            informationPanelController.setView(View.NORMAL);
        } else if (choiceText == View.ABNORMAL.toString()) {
            setPaneImage(pane1, mainController.getBestMatchAbnormal());
            informationPanelController.setView(View.ABNORMAL);
        } else {
            return;
        }
    }

    @FXML
    protected void handleSelectViewPane2(ActionEvent e) {
        String choiceText = pane2choice.getSelectionModel().getSelectedItem().toString();
        if (choiceText == View.INPUT.toString()) {
            setPaneImage(pane2, mainController.getInputImage());
            informationPanelController.setView(View.INPUT);
        } else if (choiceText == View.NORMAL.toString()) {
            setPaneImage(pane2, mainController.getBestMatchNormal());
            informationPanelController.setView(View.NORMAL);
        } else if (choiceText == View.ABNORMAL.toString()) {
            setPaneImage(pane2, mainController.getBestMatchAbnormal());
            informationPanelController.setView(View.ABNORMAL);
        } else {
            return;
        }
    }

    @FXML
    protected void handleSelectViewPane3(ActionEvent e) {
        String choiceText = pane3choice.getSelectionModel().getSelectedItem().toString();
        if (choiceText == View.INPUT.toString()) {
            setPaneImage(pane3, mainController.getInputImage());
            informationPanelController.setView(View.INPUT);
        } else if (choiceText == View.NORMAL.toString()) {
            setPaneImage(pane3, mainController.getBestMatchNormal());
            informationPanelController.setView(View.NORMAL);
        } else if (choiceText == View.ABNORMAL.toString()) {
            setPaneImage(pane3, mainController.getBestMatchAbnormal());
            informationPanelController.setView(View.ABNORMAL);
        } else {
            return;
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
}