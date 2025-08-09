package ToolBox;

import java.sql.*;

/**
 * Simple authentication service backed by a local SQLite database.
 * Provides registration and login capabilities and keeps track of
 * the currently authenticated user.
 */
public class AuthService {

    private static final String DB_URL = "jdbc:sqlite:users.db";
    private static AuthService instance;
    private String currentUser;

    private AuthService() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public boolean register(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (userExists(username)) {
            return false;
        }
        String sql = "INSERT INTO users(username) VALUES(?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.executeUpdate();
            currentUser = username.trim();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean login(String username) {
        if (userExists(username)) {
            currentUser = username;
            return true;
        }
        return false;
    }

    private boolean userExists(String username) {
        String sql = "SELECT username FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getCurrentUser() {
        return currentUser;
    }
}
