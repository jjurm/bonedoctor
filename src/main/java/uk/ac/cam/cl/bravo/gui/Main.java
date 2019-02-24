package uk.ac.cam.cl.bravo.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.IOException;


public class Main extends Application {

    public static final String APP_NAME = "Bone Doctor";
    private Stage mainStage;
    private PipelineObserver pipelineObserver = new PipelineObserver();

    @Override
    public void start(Stage stage) throws IOException { // Stage is created by java during runtime.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/main.fxml"));
        MainController mainController = new MainController(mainStage, pipelineObserver);
        loader.setController(mainController);
        Parent mainWindow = loader.load();

        mainController.launch();
        pipelineObserver.addMainController(mainController);

        mainStage = new Stage();

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        mainStage.setWidth(screenBounds.getWidth() * 0.83);
        mainStage.setHeight(screenBounds.getHeight() * 0.74);

        Scene scene = new Scene(mainWindow);
        scene.getStylesheets().add("/uk/ac/cam/cl/bravo/gui/style.css");
        mainStage.setScene(scene);


        ((Region) mainWindow).prefWidthProperty().bind(mainStage.widthProperty());
        ((Region) mainWindow).prefHeightProperty().bind(mainStage.heightProperty());

        mainStage.setTitle(APP_NAME);
        mainStage.show();
    }

    public static void main() {
        Application.launch();
    }

}