package dao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HexFormat;

public class UserDAO {

    public record LoginResult(String username, String fullName, String role, Integer employeeId) {}

    public LoginResult login(String username, String password) throws SQLException {
        String sql = "SELECT username, full_name, user_role, employee_id FROM USERS "
                   + "WHERE username = ? AND password_hash = ? AND status = 'ACTIVE'";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, sha256(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int emp = rs.getInt(4);
                    Integer employeeId = rs.wasNull() ? null : emp;
                    return new LoginResult(rs.getString(1), rs.getString(2), rs.getString(3), employeeId);
                }
            }
        }
        return null;
    }

    public boolean verifyPassword(String username, String password) throws SQLException {
        String sql = "SELECT 1 FROM USERS WHERE username = ? AND password_hash = ? AND status = 'ACTIVE'";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, sha256(password));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM USERS WHERE username = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean changeUsername(String currentUsername, String newUsername) throws SQLException {
        String sql = "UPDATE USERS SET username = ? WHERE username = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newUsername);
            ps.setString(2, currentUsername);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean changePassword(String username, String newPassword) throws SQLException {
        String sql = "UPDATE USERS SET password_hash = ? WHERE username = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sha256(newPassword));
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }

    public static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}