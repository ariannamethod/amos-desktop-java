package ToolBox;

import java.io.Serializable;

public class ImageMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String sender;
    private final String receiver;
    private final byte[] imageBytes;

    public ImageMessage(String sender, String receiver, byte[] imageBytes) {
        this.sender = sender;
        this.receiver = receiver;
        this.imageBytes = imageBytes;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    @Override
    public String toString() {
        return "image>" + sender + ">" + receiver;
    }
}
