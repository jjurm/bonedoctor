package uk.ac.cam.cl.bravo.gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class AnalysisController {

    private static final int MIN_PIXELS = 10;
    public static final ObservableList images = FXCollections.observableArrayList();


    private Stage stage;
    private Scene scene;
    private Parent parent;

    private ScrollPane scrollPane = new ScrollPane();
    final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);

    @FXML
    private ListView matches;

    @FXML
    private VBox topBottom;

    @FXML
    private ImageView imageView;

    @FXML
    private GridPane container;

    public AnalysisController(File imgFile)  {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/analysis.fxml"));
        fxmlLoader.setController(this);
        try {
            parent = (Parent) fxmlLoader.load();
            // set height and width here for this login scene
            scene = new Scene(parent, 1000, 800);
        } catch (IOException ex) {
            System.out.println("Error displaying analysis window");
            throw new RuntimeException(ex);
        }


        // ------- CREATE SCROLLABLE IMAGE PANE --------

        Image image = new Image(imgFile.toURI().toString());
        double width = image.getWidth();
        double height = image.getHeight();

        imageView.setImage(image);
        imageView.setPreserveRatio(true);
        reset(imageView, width, height);

        ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();

        imageView.setOnMousePressed(e -> {
            Point2D mousePress = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
            mouseDown.set(mousePress);
        });

        imageView.setOnMouseDragged(e -> {
            Point2D dragPoint = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
            shift(imageView, dragPoint.subtract(mouseDown.get()));
            mouseDown.set(imageViewToImage(imageView, new Point2D(e.getX(), e.getY())));
        });

        imageView.setOnScroll(e -> {
            double delta = e.getDeltaY();
            Rectangle2D viewport = imageView.getViewport();

            double scale = clamp(Math.pow(1.01, delta),

                    // don't scale so we're zoomed in to fewer than MIN_PIXELS in any direction:
                    Math.min(MIN_PIXELS / viewport.getWidth(), MIN_PIXELS / viewport.getHeight()),

                    // don't scale so that we're bigger than image dimensions:
                    Math.max(width / viewport.getWidth(), height / viewport.getHeight())

            );

            Point2D mouse = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));

            double newWidth = viewport.getWidth() * scale;
            double newHeight = viewport.getHeight() * scale;

            // To keep the visual point under the mouse from moving, we need
            // (x - newViewportMinX) / (x - currentViewportMinX) = scale
            // where x is the mouse X coordinate in the image

            // solving this for newViewportMinX gives

            // newViewportMinX = x - (x - currentViewportMinX) * scale

            // we then clamp this value so the image never scrolls out
            // of the imageview:

            double newMinX = clamp(mouse.getX() - (mouse.getX() - viewport.getMinX()) * scale,
                    0, width - newWidth);
            double newMinY = clamp(mouse.getY() - (mouse.getY() - viewport.getMinY()) * scale,
                    0, height - newHeight);

            imageView.setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
        });

        imageView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                reset(imageView, width, height);
            }
        });

        container.setPrefSize(800, 600);
        imageView.fitWidthProperty().bind(container.widthProperty());
        imageView.fitHeightProperty().bind(container.heightProperty());
        topBottom.setFillWidth(true);
        VBox.setVgrow(container, Priority.ALWAYS);



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
                    if(name.equals("RUBY"))
                        matchView.setImage(img1);
                    else if(name.equals("APPLE"))
                        matchView.setImage(img2);
                    else if(name.equals("VISTA"))
                        matchView.setImage(img3);
                    setText(name);
                    setGraphic(matchView);
                }
            }
        });

        return;
    }

    public void launchAnalysisScene(Stage stage) {
        this.stage = stage;
        stage.setScene(scene);

        stage.hide();
        stage.show();
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
