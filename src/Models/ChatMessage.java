package Models;

import java.io.Serializable;

/**
 * Represents a structured message exchanged over the network.
 */
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private String sender;
    private String receiver;
    private String content; // message text or chunk
    private String batchId;
    private int index;
    private int total;

    // Constructor for text messages
    public ChatMessage(String sender, String receiver, String content) {
        this.type = MessageType.TEXT;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    // Constructor for batched messages
    public ChatMessage(String batchId, String sender, String receiver, int index, int total, String content) {
        this.type = MessageType.BATCH;
        this.batchId = batchId;
        this.sender = sender;
        this.receiver = receiver;
        this.index = index;
        this.total = total;
        this.content = content;
    }

    public MessageType getType() {
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
