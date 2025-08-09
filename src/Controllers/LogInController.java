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

import java.util.regex.Pattern;
import ToolBox.AuthService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LogInController implements Initializable {

    public static String userName;
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,15}$");
    @FXML
    private JFXTextField userNameTextField;
    @FXML
    private JFXTextField phoneNumberTextField;


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
        String enteredName = userNameTextField.getText();
        if (enteredName == null) {
            enteredName = "";
        }

        String trimmedName = enteredName.trim();
        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please enter a valid username.");
            alert.show();
            return;
        }

        String phoneNumber = phoneNumberTextField.getText();
        if (phoneNumber == null) {
            phoneNumber = "";
        }
        if (!AuthService.authenticate(trimmedName, phoneNumber)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Authentication failed. Please check your credentials.");
            alert.show();
            return;
        }

        try {
            userName = trimmedName;
            Parent root = FXMLLoader.load(getClass().getResource("../Views/home_view.fxml"));
            Main.stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
