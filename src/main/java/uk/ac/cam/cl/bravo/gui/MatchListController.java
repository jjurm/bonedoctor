package uk.ac.cam.cl.bravo.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MatchListController {

    @FXML
    private ListView matches;
    private Stage stage;
    private AnalysisController analysisController;
    private List<Image> imgList = new ArrayList<>();

    private View view;

    public MatchListController(Stage stage) {
        this.stage = stage;

        Image img1 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/img2.png"));
        Image img2 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/img3.png"));
        Image img3 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/img4.png"));
        Image img4 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/img5.png"));
        Image img5 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/img6.png"));
        Image img6 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/img5.png"));
        Image img7 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/img6.png"));
        Image img8 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/img5.png"));
        Image img9 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/img6.png"));
        imgList.add(img1);
        imgList.add(img2);
        imgList.add(img3);
        imgList.add(img4);
        imgList.add(img5);
        imgList.add(img6);
        imgList.add(img7);
        imgList.add(img8);
        imgList.add(img9);
    }

    public void setView(View view) {
        this.view= view;
        launch();
    }

    private void onNext() {}

    public void launch() {
        // ------ CREATE SCROLLABLE LIST VIEW --------

        ObservableList<String> items = FXCollections.observableArrayList("img1.png", "img2.png", "img3.png", "img4.png", "img5.png", "img6.png", "img7.png", "img8.png", "img9.png");
        matches.setItems(items);

        matches.setCellFactory(param -> new ListCell<String>() {
            private ImageView matchView = new ImageView();

            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);

                matchView.setFitHeight(180);
                matchView.setFitWidth(180);
                matchView.setPreserveRatio(true);

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
            analysisController.setPaneImage(analysisController.pane2, imgList.get(index), view);

        });

        return;
    }

    public void setAnalysisController(AnalysisController analysisController) {
        this.analysisController = analysisController;
    }

    public ListView getMatches() {
        return this.matches;
    }


}