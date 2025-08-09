package Models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;


public class UserViewModel {
    private String userName;
    private SimpleStringProperty lastMessage;
    private SimpleStringProperty time;
    private SimpleStringProperty notificationsNumber;
    private Image avatarImage;
    private ObservableList<MessageViewModel> messagesList;
    private boolean isBot;

    public UserViewModel(String userName, String lastMessage, String time, String notificationsNumber, Image avatarImage) {
        this(userName, lastMessage, time, notificationsNumber, avatarImage, false);
    }

    public UserViewModel(String userName, String lastMessage, String time, String notificationsNumber, Image avatarImage, boolean isBot) {
        this.userName = userName;
        this.lastMessage = new SimpleStringProperty(lastMessage);
        this.time = new SimpleStringProperty(time);
        this.notificationsNumber = new SimpleStringProperty(notificationsNumber);
        this.avatarImage = avatarImage;
        this.isBot = isBot;
        messagesList = FXCollections.observableArrayList();
    }

    //region Getters & Setters

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLastMessage() {
        return lastMessage.get();
    }

    public SimpleStringProperty lastMessageProperty() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage.set(lastMessage);
    }

    public SimpleStringProperty timeProperty() {
        return time;
    }

    public String getTime() {
        return time.get();
    }

    public void setTime(String time) {
        this.time.set(time);
    }

    public String getNotificationsNumber() {
        return notificationsNumber.get();
    }

    public SimpleStringProperty notificationsNumberProperty() {
        return notificationsNumber;
    }

    public void setNotificationsNumber(String notificationsNumber) {
        this.notificationsNumber.set(notificationsNumber);
    }

    public Image getAvatarImage() {
        return avatarImage;
    }

    public void setAvatarImage(Image avatarImage) {
        this.avatarImage = avatarImage;
    }

    public boolean isBot() {
        return isBot;
    }

    public void setBot(boolean bot) {
        isBot = bot;
    }

    public ObservableList<MessageViewModel> getMessagesList() {
        return messagesList;
    }

    public void setMessagesList(ObservableList<MessageViewModel> messagesList) {
        this.messagesList = messagesList;
    }

    //endregion
}
