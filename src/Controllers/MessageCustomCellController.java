package Controllers;

import Models.MessageViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.io.IOException;

public class MessageCustomCellController extends ListCell<MessageViewModel> {

    private final MessageCell incomingMessage;
    private final MessageCell outgoingMessage;
    private final ImageCell incomingImage;
    private final ImageCell outgoingImage;

    public MessageCustomCellController() {
        incomingMessage = loadMessageCell("../Views/incoming_message_custom_cell_view.fxml");
        outgoingMessage = loadMessageCell("../Views/outgoing_message_custom_cell_view.fxml");
        incomingImage = loadImageCell("../Views/incoming_image_custom_cell_view.fxml");
        outgoingImage = loadImageCell("../Views/outgoing_image_custom_cell_view.fxml");
    }

    private MessageCell loadMessageCell(String resource) {
        MessageCell cell = new MessageCell();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
        loader.setController(cell);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return cell;
    }

    private ImageCell loadImageCell(String resource) {
        ImageCell cell = new ImageCell();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
        loader.setController(cell);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return cell;
    }

    private static class MessageCell {
        @FXML
        GridPane root;
        @FXML
        Label messageLabel;
        @FXML
        Label messageTimeLabel;
    }

    private static class ImageCell {
        @FXML
        GridPane root;
        @FXML
        ImageView imageView;
        @FXML
        Label messageTimeLabel;
    }

    @Override
    protected void updateItem(MessageViewModel item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else if (item.isOutgoing) {
            if (item.isImage) {
                outgoingImage.messageTimeLabel.setText(item.getTime());
                outgoingImage.imageView.setImage(item.getImage());
                setGraphic(outgoingImage.root);
            } else {
                outgoingMessage.messageTimeLabel.setText(item.getTime());
                outgoingMessage.messageLabel.setText(item.getMessage());
                setGraphic(outgoingMessage.root);
            }
        } else {
            if (item.isImage) {
                incomingImage.messageTimeLabel.setText(item.getTime());
                incomingImage.imageView.setImage(item.getImage());
                setGraphic(incomingImage.root);
            } else {
                incomingMessage.messageTimeLabel.setText(item.getTime());
                incomingMessage.messageLabel.setText(item.getMessage());
                setGraphic(incomingMessage.root);
            }
        }
    }
}

