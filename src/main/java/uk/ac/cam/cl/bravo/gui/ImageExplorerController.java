package uk.ac.cam.cl.bravo.gui;

import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import uk.ac.cam.cl.bravo.dataset.ImageSample;

/**
 *  Controls the operation (zoom, pan) and contains state about an image the user is "exploring".
 *  Is loaded from AnalysisController.
 *
 *  A new ImageExplorerController is created when we change the image we want to display in one of the image panes.
 */
public class ImageExplorerController {

    private static final int MIN_PIXELS = 10;
    private final Stage stage;
    private AnalysisController analysisController;
    private MainController mainController;
    private View view;
    private ImageSample image;
    private GridPane pane;
    private boolean isPreprocessed;

    double width;
    double height;

    @FXML
    private GridPane explorerContainer;

    @FXML
    private ImageView imageView;
    private Image plainImage;

    /**
     * Constructor: initialises non-FXML-dependant elements.
     * Call before launcher.
     * */
    public ImageExplorerController(Stage stage, View view, GridPane pane, boolean usePreprocessed) {
        this.stage = stage;
        this.view = view;
        this.pane = pane;
        this.isPreprocessed = usePreprocessed;

    }


    public void setImageSample(ImageSample image) {
        this.image = image;
    }

    /**
     * Puts the input image into a viewframe, and defines the necessary scroll to zoom, drag to pan functions.
     * @param userInImg
     */
    public void setImage(Image userInImg) {

        analysisController.setActiveExplorer(this);
        if (!isPreprocessed)
            this.plainImage = userInImg;

        width = userInImg.getWidth();
        height = userInImg.getHeight();

        imageView.setImage(userInImg);
        reset(imageView, width, height);

        // ------- CREATE SCROLLABLE IMAGE PANE USER INPUT --------

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

        imageView.setOnMouseClicked(e -> {
            setActiveExplorer();
            explorerContainer.setStyle("-fx-background-color: #545454"); // test
        });



        GridPane.setVgrow(explorerContainer, Priority.ALWAYS);
        GridPane.setHgrow(explorerContainer, Priority.ALWAYS);
        ReadOnlyDoubleProperty gridWidth = (explorerContainer).widthProperty();
        ReadOnlyDoubleProperty gridHeight = (explorerContainer).heightProperty();
        imageView.fitWidthProperty().bind(gridWidth);
        imageView.fitHeightProperty().bind(gridHeight.add(-100));

        System.out.println("Height:" + explorerContainer.heightProperty());
        System.out.println("Width:" + explorerContainer.widthProperty());

    }


    /**
     * Reset image to full size, no zoom no panning.
     * @param imageView
     * @param width
     * @param height
     */
    private void reset(ImageView imageView, double width, double height) {
        imageView.setViewport(new Rectangle2D(0, 0, width, height));
    }

    /**
     * Shift the viewport of the imageView by the specified delta, clamping so
     * the viewport does not move off the actual image
     * @param imageView
     * @param delta
     */
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

    /**
     * Normal clamp function
     * @param value
     * @param min
     * @param max
     * @return
     */
    private double clamp(double value, double min, double max) {

        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    /**
     * Converts coordinates in imageView to coordinates in image
     * @param imageView
     * @param imageViewCoordinates
     * @return A Point2D
     */
    private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
        double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
        double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

        Rectangle2D viewport = imageView.getViewport();
        return new Point2D(
                viewport.getMinX() + xProportion * viewport.getWidth(),
                viewport.getMinY() + yProportion * viewport.getHeight());
    }

    /**
     * Set analysisController, used when ImageExplorer is loaded.
     * Important for setting the active explorer in analysisController.
     * @param analysisController
     */
    public void setAnalysisController(AnalysisController analysisController){
        this.analysisController = analysisController;
    }

    /**
     * Set activeExplorer in analysisController.
     * Used to determine which image the user wants to interact with,
     * and which information is shown in informationpanel.
     */
    private void setActiveExplorer() {
        analysisController.setActiveExplorer(this);
    }

    /**
     * Getter for view state
     * @return view state
     */
    public View getView() {
        return view;
    }

    /**
     * Set the main controller. Important to access the mainpipeline.
     * @param mainController
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Getter for image state
     * @return image state
     */
    public ImageSample getCurrentImage() {
        return this.image;
    }

    /**
     * Getter for pane state
     * @return pane state
     */
    public GridPane getCurrentPane() {
        return this.pane;
    }

    public boolean isPreprocessed() {
        if (this!=null)
            return this.isPreprocessed;
        else
            return false;
    }

    public Image getCurrentPlainImage() {
        return this.plainImage;
    }
}