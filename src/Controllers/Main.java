package Controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    private AppContext context;

    @Override
    public void start(Stage primaryStage) throws Exception {
        context = new AppContext(primaryStage);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../Views/login_view.fxml"));
        loader.setControllerFactory(type -> {
            try {
                Object controller = type.getDeclaredConstructor().newInstance();
                if (controller instanceof ContextAware) {
                    ((ContextAware) controller).setContext(context);
                }
                return controller;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
