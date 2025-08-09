package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    private static final String CONFIG_PATH = "src/resources/config.properties";

    @FXML
    private Button closeButton;
    @FXML
    private TextField ipField;
    @FXML
    private TextField portField;
    @FXML
    private TextField demoUsersField;

    private final Properties properties = new Properties();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try (InputStream input = new FileInputStream(CONFIG_PATH)) {
            properties.load(input);
        } catch (IOException e) {
            // ignore, use defaults
        }
        ipField.setText(properties.getProperty("ip", "127.0.0.1"));
        portField.setText(properties.getProperty("port", "55555"));
        demoUsersField.setText(properties.getProperty("demoUsers", ""));
    }

    @FXML
    void saveSettings(ActionEvent event) {
        properties.setProperty("ip", ipField.getText().trim());
        properties.setProperty("port", portField.getText().trim());
        properties.setProperty("demoUsers", demoUsersField.getText().trim());
        try (OutputStream output = new FileOutputStream(CONFIG_PATH)) {
            properties.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        closeSettings(event);
    }

    @FXML
    void closeSettings(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
