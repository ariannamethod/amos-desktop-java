package Controllers;

import Models.UserViewModel;
import Models.MessageViewModel;
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

        connection = new NetworkConnection(data -> Platform.runLater(() -> {
            Image image = null;
            String[] messageInfo = data.toString().split(">");
            String type = messageInfo[0];
            if (type.matches("image")) {
                image = new Image((InputStream) data);
            }

            if (type.matches("text")) {
                String sender = messageInfo[1];
                String receiver = messageInfo[2];
                String messageText = messageInfo[3];
                if (shouldReceive(receiver)) {
                    handleIncoming(sender, messageText, image);
                }
            } else if (type.matches("batch")) {
                String batchId = messageInfo[1];
                String sender = messageInfo[2];
                String receiver = messageInfo[3];
                int index = Integer.parseInt(messageInfo[4]);
                int total = Integer.parseInt(messageInfo[5]);
                String chunk = messageInfo[6];
                if (shouldReceive(receiver)) {
                    String key = sender + ">" + receiver + ">" + batchId;
                    String[] parts = pendingBatches.computeIfAbsent(key, k -> new String[total]);
                    parts[index] = chunk;
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
                        handleIncoming(sender, full.toString(), image);
                    }
                }
            }
        }), status -> {
            // Connection status updates can be handled here
        }, e -> e.printStackTrace(), "127.0.0.1", name.matches("Jetlight"), 55555);
        connection.openConnection();

        usersListView.getSelectionModel().select(0);
    }


    @FXML
    void sendMessage(ActionEvent event) {
        sendMessage();
    }

    private void sendMessage() {
        String text = messageField.getText();
        if (text.isEmpty()) return;
        if (text.length() > MAX_MESSAGE_LENGTH) {
            text = text.substring(0, MAX_MESSAGE_LENGTH);
        }
        currentlySelectedUser.messagesList.add(new MessageViewModel(text, getCurrentTime(), true, false, null));
        String receiver = (currentlySelectedUser.isBot() && localUser.isBot()) ? "bots" : currentlySelectedUser.getUserName();
        List<String> chunks = MessageBatcher.split(text, BATCH_SIZE);
        if (chunks.size() == 1) {
            connection.sendData("text>" + localUser.getUserName() + ">" + receiver + ">" + text);
        } else {
            String batchId = String.valueOf(System.currentTimeMillis());
            for (int i = 0; i < chunks.size(); i++) {
                connection.sendData("batch>" + batchId + ">" + localUser.getUserName() + ">" + receiver + ">" + i + ">" + chunks.size() + ">" + chunks.get(i));
            }
        }
        messageField.clear();
        messagesListView.scrollTo(currentlySelectedUser.messagesList.size());
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
