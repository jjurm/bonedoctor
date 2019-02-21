package uk.ac.cam.cl.bravo.gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MatchListController {

    @FXML
    private ListView matches;
    private Stage stage;
    private PipelineObserver pipelineObserver;
    private AnalysisController analysisController;


    public MatchListController(Stage stage, PipelineObserver pipelineObserver) {
        this.stage = stage;
        this.pipelineObserver = pipelineObserver;
    }

    public void launch() {
        // ------ CREATE SCROLLABLE LIST VIEW --------

        Image img1 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/glasses.jpg"));
        Image img2 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/bowtie.jpg"));
        Image img3 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/superthumb.jpg"));
        List<Image> imgList = new ArrayList<>();
        imgList.add(img1);
        imgList.add(img2);
        imgList.add(img3);

        ObservableList<String> items = FXCollections.observableArrayList (
                "RUBY", "APPLE", "VISTA");
        matches.setItems(items);
        matches.setCellFactory(param -> new ListCell<String>() {
            private ImageView matchView = new ImageView();

            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                matchView.fitHeightProperty().bind(matches.heightProperty());
                System.out.println("Matches: " + matches.heightProperty());
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    matchView.setImage(imgList.get(getIndex()));
                    setText(name);
                    setGraphic(matchView);
                }
            }

        });


        // ----- ENABLE SELECT NEW MATCH -----

        matches.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                System.out.println("Clicked on " + matches.getSelectionModel().getSelectedIndex());
            }

            int index = matches.getSelectionModel().getSelectedIndex();
            analysisController.setMatchImage(imgList.get(index));

        });

        return;
    }

    public void setAnalysisController(AnalysisController analysisController) {
        this.analysisController = analysisController;
    }
    
}