package Controllers;

import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LogInController implements Initializable, ContextAware {

    private AppContext context;
    @FXML
    private JFXTextField userNameTextField;

    @Override
    public void setContext(AppContext context) {
        this.context = context;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    void closeApp(MouseEvent event) {
        context.getStage().close();
    }

    @FXML
    void minimizeApp(MouseEvent event) {
        context.getStage().setIconified(true);
    }

    @FXML
    void signUp(ActionEvent event) {
        String enteredName = userNameTextField.getText();
        if (enteredName == null) {
            enteredName = "";
        }

        String trimmedName = enteredName.trim();
        if (trimmedName.isEmpty() || !enteredName.equals(trimmedName)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please enter a valid username.");
            alert.show();
            return;
        }

        try {
            context.setUserName(trimmedName);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../Views/home_view.fxml"));
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
            context.getStage().setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
