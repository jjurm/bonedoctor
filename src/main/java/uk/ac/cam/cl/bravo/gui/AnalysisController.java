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
    private PipelineObserver pipelineObserver;

    private static String INPUT = "Input Image";
    private static String NORMAL = "Best Match, Normal";
    private static String ABNORMAL = "Best Match, Abormal";
    private static String ABNORMAL_OVER = "Overlay, Abormal";
    private static String NORMAL_OVER = "Overlay, Normal";


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

    public AnalysisController(Stage stage, PipelineObserver pipelineObserver) {
        this.stage = stage;
        this.pipelineObserver = pipelineObserver;
    }

    public void launch() {
        try {
            // Initialize controller
            FXMLLoader matchListLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/matchList.fxml"));
            matchListController = new MatchListController(stage, pipelineObserver);
            matchListLoader.setController(matchListController);
            Parent matchListFXML = matchListLoader.load();

            matchListController.getMatches().setPrefHeight(200);

            ObservableList<String> items = FXCollections.observableArrayList(INPUT, NORMAL, ABNORMAL, NORMAL_OVER, ABNORMAL_OVER);

            grid.add(matchListFXML, 0, 2);
            pane1choice.setItems(items);
            pane2choice.setItems(items);
            pane3choice.setItems(items);

            // Child controller actions
            matchListController.launch();
            matchListController.setAnalysisController(this);
            pipelineObserver.addMatchListController(matchListController);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //  CREATE SCROLLABLE IMAGE PANE
    public void setPaneImage(GridPane pane, Image imgFile) {
        try {
            // Initialize controller
            FXMLLoader imageExplorerLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/imageExplorer.fxml"));
            imageExplorerController = new ImageExplorerController(stage, pipelineObserver);
            imageExplorerLoader.setController(imageExplorerController);
            Parent imageExplorerFXML = imageExplorerLoader.load();

            pane.add(imageExplorerFXML, 0, 1);

            // Child controller actions
            imageExplorerController.setImage(imgFile);
            imageExplorerController.setAnalysisController(this);
            pipelineObserver.addImageExplorerController(imageExplorerController);
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
        System.out.println(choiceText);
        System.out.println(choiceText == INPUT);
        if (choiceText == INPUT) {
            setPaneImage(pane1, mainController.getInputImage());
        } else if (choiceText == NORMAL) {
            setPaneImage(pane1, mainController.getBestMatchNormal());
        } else if (choiceText == ABNORMAL) {
            setPaneImage(pane1, mainController.getBestMatchAbnormal());
        } else {
            return;
        }
    }

    @FXML
    protected void handleSelectViewPane2(ActionEvent e) {
        String choiceText = pane2choice.getSelectionModel().getSelectedItem().toString();
        if (choiceText == INPUT) {
            setPaneImage(pane2, mainController.getInputImage());
        } else if (choiceText == NORMAL) {
            setPaneImage(pane2, mainController.getBestMatchNormal());
        } else if (choiceText == ABNORMAL) {
            setPaneImage(pane2, mainController.getBestMatchAbnormal());
        } else {
            return;
        }
    }

    @FXML
    protected void handleSelectViewPane3(ActionEvent e) {
        String choiceText = pane3choice.getSelectionModel().getSelectedItem().toString();
        if (choiceText == INPUT) {
            setPaneImage(pane3, mainController.getInputImage());
        } else if (choiceText == NORMAL) {
            setPaneImage(pane3, mainController.getBestMatchNormal());
        } else if (choiceText == ABNORMAL) {
            setPaneImage(pane3, mainController.getBestMatchAbnormal());
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
}