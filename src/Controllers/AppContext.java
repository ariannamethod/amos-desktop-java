package Controllers;

import javafx.stage.Stage;

public class AppContext {
    private final Stage stage;
    private String userName;

    public AppContext(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
