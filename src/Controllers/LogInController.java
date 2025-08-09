package Controllers;

import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import ToolBox.AuthService;

public class LogInController implements Initializable {

    @FXML
    private JFXTextField userNameTextField;

    private final AuthService authService = AuthService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    void closeApp(MouseEvent event) {
        Main.stage.close();
    }

    @FXML
    void minimizeApp(MouseEvent event) {
        Main.stage.setIconified(true);
    }

    @FXML
    void signUp(ActionEvent event) {
        try {
            String name = userNameTextField.getText();
            if (authService.register(name)) {
                Parent root = FXMLLoader.load(getClass().getResource("../Views/home_view.fxml"));
                Main.stage.setScene(new Scene(root));
            } else {
                System.out.println("Registration failed: user already exists or invalid name");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
