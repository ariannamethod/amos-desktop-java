package Controllers;

import Models.UserViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class UserCustomCellController extends ListCell<UserViewModel> {

    @FXML
    private GridPane root;

    @FXML
    private ImageView avatarImage;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label lastMessageLabel;

    @FXML
    private Label messageTimeLabel;

    @FXML
    private Label nombreMessageLabel;

    @FXML
    private StackPane notificationPanel;

    private final FXMLLoader fxmlLoader;
    private final GridPane rootNode;

    public UserCustomCellController() {
        fxmlLoader = new FXMLLoader(getClass().getResource("/Views/user_custom_cell_view.fxml"));
        fxmlLoader.setController(this);
        try {
            rootNode = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(UserViewModel item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            userNameLabel.setText(String.valueOf(item.getUserName()));
            lastMessageLabel.setText(String.valueOf(item.getLastMessage()));
            messageTimeLabel.textProperty().bind(item.time);
            if (!item.getNotificationsNumber().equals("0")) {
                nombreMessageLabel.textProperty().bind(item.notificationsNumberProperty());
                if (!notificationPanel.isVisible()) notificationPanel.setVisible(true);
            } else {
                notificationPanel.setVisible(false);
            }
            setGraphic(rootNode);
        }
    }
}