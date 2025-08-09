package Repository;

import Models.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {

    public static void initialize() {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS messages (id INTEGER PRIMARY KEY AUTOINCREMENT, sender TEXT, receiver TEXT, content TEXT, timestamp TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void save(Message message) {
        String sql = "INSERT INTO messages(sender, receiver, content, timestamp) VALUES(?,?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, message.getSender());
            ps.setString(2, message.getReceiver());
            ps.setString(3, message.getContent());
            ps.setString(4, message.getTimestamp());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Message> getMessagesBetween(String user1, String user2) {
        String sql = "SELECT sender, receiver, content, timestamp FROM messages WHERE (sender=? AND receiver=?) OR (sender=? AND receiver=?) ORDER BY id";
        List<Message> result = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user1);
            ps.setString(2, user2);
            ps.setString(3, user2);
            ps.setString(4, user1);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new Message(
                        rs.getString("sender"),
                        rs.getString("receiver"),
                        rs.getString("content"),
                        rs.getString("timestamp")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
