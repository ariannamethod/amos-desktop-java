package Controllers;

import ToolBox.ResourceManager;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Locale;

public class Main extends Application {
    public static Stage stage;
    private static Scene scene;
    private static String currentView = "/Views/login_view.fxml";

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = ResourceManager.loadFXML(currentView);
        scene = new Scene(root, 1280, 720);
        ResourceManager.applyTheme(scene, "/resources/css/colors.css");
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        stage = primaryStage;
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        currentView = fxml;
        Parent root = ResourceManager.loadFXML(fxml);
        scene.setRoot(root);
    }

    public static void switchLanguage(Locale locale) throws IOException {
        ResourceManager.setLocale(locale);
        setRoot(currentView);
    }

    public static void switchTheme(String cssPath) {
        ResourceManager.applyTheme(scene, cssPath);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
