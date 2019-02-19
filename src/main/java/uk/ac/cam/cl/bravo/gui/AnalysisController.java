package uk.ac.cam.cl.bravo.gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AnalysisController implements Initializable {

    private static final int MIN_PIXELS = 10;

    public MainController mainController;
    private ImageExplorerController imageExplorerController;
    private Stage stage;

    private ScrollPane scrollPane = new ScrollPane();
    final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);

    @FXML
    private ListView matches;

    @FXML
    private VBox topBottom;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // ------ CREATE SCROLLABLE LIST VIEW --------

        Image img1 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/glasses.jpg"));
        Image img2 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/bowtie.jpg"));
        Image img3 = new Image(getClass().getResourceAsStream("/uk/ac/cam/cl/bravo/gui/superthumb.jpg"));

        ObservableList<String> items =FXCollections.observableArrayList (
                "RUBY", "APPLE", "VISTA");
        matches.setItems(items);
        matches.setCellFactory(param -> new ListCell<String>() {
            private ImageView matchView = new ImageView();


            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
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

    //  CREATE SCROLLABLE IMAGE PANE USER INPUT
    public void setImage(Image imgFile) {
        try {
            FXMLLoader imageExplorerLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/imageExplorer.fxml"));
            Parent imageExplorerFXML = imageExplorerLoader.load();
            imageExplorerController = imageExplorerLoader.getController();
            imageExplorerController.setImage(imgFile);
            topBottom.getChildren().add(0, imageExplorerFXML);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUp(MainController main, Stage newStage) {
        mainController = main;
        stage = newStage;
    }

    private HBox createButtons(double width, double height, ImageView imageView) {
        Button reset = new Button("Reset");
        reset.setOnAction(e -> reset(imageView, width / 2, height / 2));
        Button full = new Button("Full view");
        full.setOnAction(e -> reset(imageView, width, height));
        HBox buttons = new HBox(10, reset, full);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10));
        return buttons;
    }

    // reset to the top left:
    private void reset(ImageView imageView, double width, double height) {
        imageView.setViewport(new Rectangle2D(0, 0, width, height));
    }

    // shift the viewport of the imageView by the specified delta, clamping so
    // the viewport does not move off the actual image:
    private void shift(ImageView imageView, Point2D delta) {
        Rectangle2D viewport = imageView.getViewport();

        double width = imageView.getImage().getWidth() ;
        double height = imageView.getImage().getHeight() ;

        double maxX = width - viewport.getWidth();
        double maxY = height - viewport.getHeight();

        double minX = clamp(viewport.getMinX() - delta.getX(), 0, maxX);
        double minY = clamp(viewport.getMinY() - delta.getY(), 0, maxY);

        imageView.setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
    }

    private double clamp(double value, double min, double max) {

        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
        double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
        double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

        Rectangle2D viewport = imageView.getViewport();
        return new Point2D(
                viewport.getMinX() + xProportion * viewport.getWidth(),
                viewport.getMinY() + yProportion * viewport.getHeight());
    }

}
