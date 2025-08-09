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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.sound.sampled.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;

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
    Image userImage = new Image(Objects.requireNonNull(getClass().getResource("/resources/img/smile.png")).toExternalForm());

    private static final int MAX_MESSAGE_LENGTH = 100000;
    private static final int BATCH_SIZE = 4096;
    private final Map<String, String[]> pendingBatches = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String name = "Jetlight";
        usersList.add(new UserViewModel(name, "message ",
                getCurrentTime(), 0 + "", userImage));

        if (name.equals("Oussama")) {
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
            if (type.equals("image")) {
                image = new Image((InputStream) data);
            }

            if (type.equals("text")) {
                String sender = messageInfo[1];
                String receiver = messageInfo[2];
                String messageText = messageInfo[3];
                if (shouldReceive(receiver)) {
                    handleIncoming(sender, messageText, image);
                }
            } else if (type.equals("batch")) {
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
        }), "127.0.0.1", name.equals("Jetlight"), 55555);
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
                connection.sendData("text>" + localUser.getUserName() + ">" + receiver + ">" + text);
            } else {
                String batchId = String.valueOf(System.currentTimeMillis());
                for (int i = 0; i < chunks.size(); i++) {
                    connection.sendData("batch>" + batchId + ">" + localUser.getUserName() + ">" + receiver + ">" + i + ">" + chunks.size() + ">" + chunks.get(i));
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
            if (imageFile == null) return;
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
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Search Messages");
        dialog.setHeaderText("Search in chat");
        dialog.setContentText("Enter text:");
        dialog.showAndWait().ifPresent(query -> {
            int index = -1;
            for (int i = 0; i < messagesListView.getItems().size(); i++) {
                MessageViewModel mv = messagesListView.getItems().get(i);
                String msg = mv.getMessage();
                if (msg != null && msg.toLowerCase().contains(query.toLowerCase())) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                messagesListView.scrollTo(index);
                messagesListView.getSelectionModel().select(index);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Search");
                alert.setHeaderText(null);
                alert.setContentText("No messages found for: " + query);
                alert.showAndWait();
            }
        });
    }

    @FXML
    void settingsButtonClicked(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Views/settings_view.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Settings");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to open settings.");
            alert.showAndWait();
        }
    }

    @FXML
    void slideMenuClicked(MouseEvent event) {
        ContextMenu menu = new ContextMenu();
        MenuItem newChat = new MenuItem("New Chat");
        MenuItem logout = new MenuItem("Logout");
        newChat.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Feature not available.");
            alert.showAndWait();
        });
        logout.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Feature not available.");
            alert.showAndWait();
        });
        menu.getItems().addAll(newChat, logout);
        menu.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
    }

    @FXML
    void smileyButtonClicked(MouseEvent event) {
        ContextMenu menu = new ContextMenu();
        String[] emojis = {"😀", "😂", "😍", "😢", "👍"};
        for (String e : emojis) {
            MenuItem item = new MenuItem(e);
            item.setOnAction(a -> messageField.appendText(e));
            menu.getItems().add(item);
        }
        menu.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
    }

    @FXML
    void vocalMessageClicked(MouseEvent event) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setHeaderText(null);
        info.setContentText("Recording for 5 seconds...");
        info.show();

        new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(16000, 16, 2, true, true);
                DataLine.Info dlInfo = new DataLine.Info(TargetDataLine.class, format);
                TargetDataLine line = (TargetDataLine) AudioSystem.getLine(dlInfo);
                line.open(format);
                line.start();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                long end = System.currentTimeMillis() + 5000;
                while (System.currentTimeMillis() < end) {
                    int count = line.read(buffer, 0, buffer.length);
                    if (count > 0) {
                        out.write(buffer, 0, count);
                    }
                }
                line.stop();
                line.close();
                byte[] audio = out.toByteArray();
                File file = File.createTempFile("voice_", ".wav");
                try (AudioInputStream ais = new AudioInputStream(
                        new ByteArrayInputStream(audio), format,
                        audio.length / format.getFrameSize())) {
                    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
                }
                Platform.runLater(() -> {
                    info.close();
                    currentlySelectedUser.messagesList.add(new MessageViewModel("[Voice message] " + file.getName(),
                            getCurrentTime(), true, false, null));
                    messagesListView.scrollTo(currentlySelectedUser.messagesList.size());
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    info.close();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to record audio: " + ex.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
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
            if (usersList.get(i).getUserName().equals(userName)) {
                return i;
            }
        }
        return -1;
    }

    private boolean shouldReceive(String receiver) {
        return receiver.equals(localUser.getUserName()) || (receiver.equals("bots") && localUser.isBot());
    }

    private void handleIncoming(String sender, String messageText, Image image) {
        int userSender = findUser(sender);
        usersList.get(userSender).time.setValue(getCurrentTime());
        if (messageText.equals("null")) {
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
