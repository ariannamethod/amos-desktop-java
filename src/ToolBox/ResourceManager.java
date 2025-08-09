package ToolBox;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceManager {
    private static Locale locale = Locale.getDefault();

    public static void setLocale(Locale newLocale) {
        locale = newLocale;
    }

    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle("bundles.messages", locale);
    }

    public static Parent loadFXML(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(ResourceManager.class.getResource(fxmlPath), getBundle());
        return loader.load();
    }

    public static void applyTheme(Scene scene, String cssPath) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(ResourceManager.class.getResource(cssPath).toExternalForm());
    }
}
