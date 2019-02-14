package sample;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException { // Stage is created by java during runtime.
        new UploadController().displayUploadController(stage);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}