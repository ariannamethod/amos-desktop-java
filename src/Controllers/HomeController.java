package Controllers;

import Models.UserViewModel;
import Models.MessageViewModel;
import Models.Message;
import ToolBox.NetworkConnection;
import ToolBox.MessageBatcher;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static ToolBox.Utilities.getCurrentTime;

public class HomeController implements Initializable {

    @FXML
    private Label userNameLabel;
    @FXML
    private Label chatRoomNameLabel;
    @FXML
    private TextArea messageField;
    @FXML
    private ListView<UserViewModel> usersListView;
    @FXML
    private ListView<MessageViewModel> messagesListView;

    NetworkConnection connection;
    private ObservableList<UserViewModel> usersList = FXCollections.observableArrayList();
    UserViewModel currentlySelectedUser, localUser;
    Image userImage = new Image("resources/img/smile.png");

    private static final int MAX_MESSAGE_LENGTH = 100000;
    private static final int BATCH_SIZE = 4096;
    private final Map<String, String[]> pendingBatches = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String name = "Jetlight";
        usersList.add(new UserViewModel(name, "message ",
                getCurrentTime(), 0 + "", userImage));

        if (name.matches("Oussama")) {
            usersList.addAll(new UserViewModel("Oliver", "Hello", getCurrentTime(), 1 + "", userImage)
                    , new UserViewModel("Harry", "Did you receive my call?", getCurrentTime(), 1 + "", userImage)
                    , new UserViewModel("George", "How are you?", getCurrentTime(), 2 + "", userImage)
                    , new UserViewModel("Noah", "Yeah", getCurrentTime(), 0 + "", userImage)
                    , new UserViewModel("Jack", "No way!", getCurrentTime(), 0 + "", userImage));
        } else {
            usersList.addAll(new UserViewModel("Jacob", "Congratulations", getCurrentTime(), 1 + "", userImage)
                    , new UserViewModel("Leo", "Alright, thanks", getCurrentTime(), 0 + "", userImage)
                    , new UserViewModel("Oscar", "I agree, when?", getCurrentTime(), 2 + "", userImage));
        }

        localUser = new UserViewModel(LogInController.userName, "message", getCurrentTime(), 0 + "", userImage);
        userNameLabel.setText(localUser.getUserName());

        usersListView.setItems(usersList);
        usersListView.setCellFactory(param -> new UserCustomCellController() {
            {
                prefWidthProperty().bind(usersListView.widthProperty().subtract(0)); // 1
            }
        });
        messagesListView.setCellFactory(param -> new MessageCustomCellController() {
            {
                prefWidthProperty().bind(messagesListView.widthProperty().subtract(0)); // 1
            }
        });
        usersListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    currentlySelectedUser = usersListView.getSelectionModel().getSelectedItem();
                    messagesListView.setItems(currentlySelectedUser.messagesList);
                    chatRoomNameLabel.setText(currentlySelectedUser.userName);
                    messagesListView.scrollTo(currentlySelectedUser.messagesList.size());
                }
        );

        connection = new NetworkConnection(message -> Platform.runLater(() -> {
            if ("text".equals(message.getType())) {
                if (shouldReceive(message.getReceiver())) {
                    handleIncoming(message.getSender(), message.getContent(), null);
                }
            } else if ("batch".equals(message.getType())) {
                if (shouldReceive(message.getReceiver())) {
                    String key = message.getSender() + ">" + message.getReceiver() + ">" + message.getBatchId();
                    String[] parts = pendingBatches.computeIfAbsent(key, k -> new String[message.getTotal()]);
                    parts[message.getIndex()] = message.getContent();
                    boolean complete = true;
                    for (String part : parts) {
                        if (part == null) {
                            complete = false;
                            break;
                        }
                    }
                    if (complete) {
                        StringBuilder full = new StringBuilder();
                        for (String part : parts) {
                            full.append(part);
                        }
                        pendingBatches.remove(key);
                        handleIncoming(message.getSender(), full.toString(), null);
                    }
                }
            }
        }), "127.0.0.1", name.matches("Jetlight"), 55555);
        connection.openConnection();

        usersListView.getSelectionModel().select(0);
    }


    @FXML
    void sendMessage(ActionEvent event) {
        sendMessage();
    }

    private void sendMessage() {
        try {
            String text = messageField.getText();
            if (text.isEmpty()) return;
            if (text.length() > MAX_MESSAGE_LENGTH) {
                text = text.substring(0, MAX_MESSAGE_LENGTH);
            }
            currentlySelectedUser.messagesList.add(new MessageViewModel(text, getCurrentTime(), true, false, null));
            String receiver = (currentlySelectedUser.isBot() && localUser.isBot()) ? "bots" : currentlySelectedUser.getUserName();
            List<String> chunks = MessageBatcher.split(text, BATCH_SIZE);
            if (chunks.size() == 1) {
                Message message = new Message("text", localUser.getUserName(), receiver, text);
                connection.sendMessage(message);
            } else {
                String batchId = String.valueOf(System.currentTimeMillis());
                for (int i = 0; i < chunks.size(); i++) {
                    Message message = new Message("batch", localUser.getUserName(), receiver, chunks.get(i), batchId, i, chunks.size());
                    connection.sendMessage(message);
                }
            }
            messageField.clear();
            messagesListView.scrollTo(currentlySelectedUser.messagesList.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void messageFieldKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
            event.consume();
            sendMessage();
        }
    }

    @FXML
    void attachFile(MouseEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            File imageFile = fileChooser.showOpenDialog(new Stage());
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            currentlySelectedUser.messagesList.add(new MessageViewModel("", getCurrentTime(), false, true, image));
            messagesListView.scrollTo(currentlySelectedUser.messagesList.size());

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @FXML
    void searchChatRoom(MouseEvent event) {

    }

    @FXML
    void settingsButtonClicked(MouseEvent event) {

    }

    @FXML
    void slideMenuClicked(MouseEvent event) {

    }

    @FXML
    void smileyButtonClicked(MouseEvent event) {

    }

    @FXML
    void vocalMessageClicked(MouseEvent event) {

    }

    @FXML
    void closeApp(MouseEvent event) {
        try {
            connection.closeConnection();
            Main.stage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void minimizeApp(MouseEvent event) {
        Main.stage.setIconified(true);
    }

    int findUser(String userName) {
        for (int i = 0; i < usersList.size(); i++) {
            if (usersList.get(i).getUserName().matches(userName)) {
                return i;
            }
        }
        return -1;
    }

    private boolean shouldReceive(String receiver) {
        return receiver.matches(localUser.getUserName()) || (receiver.matches("bots") && localUser.isBot());
    }

    private void handleIncoming(String sender, String messageText, Image image) {
        int userSender = findUser(sender);
        usersList.get(userSender).time.setValue(getCurrentTime());
        if (messageText.matches("null")) {
            usersList.get(userSender).lastMessage.setValue(messageText);
        }
        usersList.get(userSender).messagesList.add(new MessageViewModel(messageText, getCurrentTime(), false, image != null, image));
        messagesListView.scrollTo(currentlySelectedUser.messagesList.size());
        usersList.get(userSender).notificationsNumber.setValue((Integer.valueOf(currentlySelectedUser.notificationsNumber.getValue()) + 1) + "");
        System.out.println("Sender: " + usersList.get(userSender).userName
                + "\n" + "Receiver: " + localUser.getUserName()
                + "\n" + "Image : " + image);
    }

}
