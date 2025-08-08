package ToolBox;

import java.util.ArrayList;
import java.util.List;

public class MessageBatcher {
    public static List<String> split(String message, int size) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < message.length(); i += size) {
            parts.add(message.substring(i, Math.min(message.length(), i + size)));
        }
        return parts;
    }
}
