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
import java.util.ResourceBundle;

public class MatchListController {

    @FXML
    private ListView matches;
    private Stage stage;
    private PipelineObserver pipelineObserver;

    public MatchListController(Stage stage, PipelineObserver pipelineObserver) {
        this.stage = stage;
        this.pipelineObserver = pipelineObserver;
    }

    public void launch() {
        // ------ CREATE SCROLLABLE LIST VIEW --------

        Image img1 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/glasses.jpg"));
        Image img2 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/bowtie.jpg"));
        Image img3 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/superthumb.jpg"));

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
                    if (name.equals("RUBY"))
                        matchView.setImage(img1);
                    else if (name.equals("APPLE"))
                        matchView.setImage(img2);
                    else if (name.equals("VISTA"))
                        matchView.setImage(img3);
                    setText(name);
                    setGraphic(matchView);
                }
            }

        });


        // ----- ENABLE SELECT NEW MATCH -----

        matches.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                System.out.println("Clicked on " + matches.getSelectionModel().getSelectedItem());
            }
        });

        return;
    }
    
}