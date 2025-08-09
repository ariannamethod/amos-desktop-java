package Models;

import java.io.Serializable;

public class Message implements Serializable {
    private String type;
    private String sender;
    private String receiver;
    private String content;
    private String batchId;
    private int index;
    private int total;

    public Message() {}

    public Message(String type, String sender, String receiver, String content) {
        this(type, sender, receiver, content, null, 0, 0);
    }

    public Message(String type, String sender, String receiver, String content, String batchId, int index, int total) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.batchId = batchId;
        this.index = index;
        this.total = total;
    }

    public String getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public String getBatchId() {
        return batchId;
    }

    public int getIndex() {
        return index;
    }

    public int getTotal() {
        return total;
    }
}
