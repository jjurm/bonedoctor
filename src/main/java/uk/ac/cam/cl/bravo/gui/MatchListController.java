package uk.ac.cam.cl.bravo.gui;

import io.reactivex.Observer;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.ImageSample;
import uk.ac.cam.cl.bravo.pipeline.Rated;

import java.util.*;

public class MatchListController {

    @FXML
    private ListView matches;
    private Stage stage;
    private AnalysisController analysisController;
    private List<Image> imgNormalList = new ArrayList<>();

    private View view;
    private MainController mainController;

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
        imgNormalList.add(img1);
        imgNormalList.add(img2);
        imgNormalList.add(img3);
        imgNormalList.add(img4);
        imgNormalList.add(img5);
        imgNormalList.add(img6);
        imgNormalList.add(img7);
        imgNormalList.add(img8);
        imgNormalList.add(img9);
    }

    public void setView(View view) {
        this.view = view;
        launch();
    }

    public void launch() {
        // ------ CREATE SCROLLABLE LIST VIEW --------

        mainController.getMainPipeline().getSimilarNormal().subscribe(normals -> startUIChange(normals));

        return;
    }

    private void startUIChange(List<Rated<ImageSample>> normals) {
        Platform.runLater(() -> createMatchList(normals));
    }

    private void createMatchList(List<Rated<ImageSample>> normals) {


        analysisController.setNormalList(normals);
        ObservableList<String> list = FXCollections.observableArrayList();
        for (Rated<ImageSample> r: normals){
            list.add(r.getValue().getPath());
        }

        matches.setItems(list);

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
//                    matchView.setImage(normals.get(getIndex()));
                    Image img = SwingFXUtils.toFXImage(normals.get(getIndex()).getValue().loadImage(), null);
                    matchView.setImage(img);
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

            analysisController.setPaneImage(analysisController.pane2, imgNormalList.get(index), View.NORMAL);

        });
    }

    public void setAnalysisController(AnalysisController analysisController) {
        this.analysisController = analysisController;
    }

    public ListView getMatches() {
        return this.matches;
    }


    public void hide() {
        matches.setManaged(false);
        matches.setVisible(false);
    }

    public void show() {
        matches.setManaged(true);
        matches.setVisible(true);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}