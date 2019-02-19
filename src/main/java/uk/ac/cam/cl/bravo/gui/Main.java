package uk.ac.cam.cl.bravo.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {

    public static final String APP_NAME = "Bone Doctor";
    private Stage mainStage;

    @Override
    public void start(Stage stage) throws IOException { // Stage is created by java during runtime.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/uk/ac/cam/cl/bravo/gui/main.fxml"));
        Parent mainWindow = loader.load();
        mainStage = new Stage();

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        mainStage.setWidth(screenBounds.getWidth() * 0.83);
        mainStage.setHeight(screenBounds.getHeight() * 0.74);

        ((MainController) loader.getController()).setStage(mainStage);
        mainStage.setScene(new Scene(mainWindow));
        mainStage.setTitle(APP_NAME);
        mainStage.show();
    }

    public static void main() {
        Application.launch();
    }

}