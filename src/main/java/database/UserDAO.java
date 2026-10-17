package database;

import model.Admin;
import model.Faculty;
import model.Student;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public User login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE username = ?";
        Connection conn = Database.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                if (!PasswordUtil.matches(password, rs.getString("password_hash"))) {
                    return null;
                }
                int userId = rs.getInt("id");
                String role = rs.getString("role");
                String dbUsername = rs.getString("username");
                if ("ADMIN".equals(role)) {
                    return new Admin(userId, dbUsername);
                }
                if ("FACULTY".equals(role)) {
                    return loadFaculty(conn, userId, dbUsername);
                }
                if ("STUDENT".equals(role)) {
                    return loadStudent(conn, userId, dbUsername);
                }
                return null;
            }
        }
    }

    private Faculty loadFaculty(Connection conn, int userId, String username) throws SQLException {
        String sql = "SELECT id, name, department, email FROM faculty WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Faculty(userId, username, rs.getInt("id"), rs.getString("name"),
                        rs.getString("department"), rs.getString("email"));
            }
        }
    }

    private Student loadStudent(Connection conn, int userId, String username) throws SQLException {
        String sql = "SELECT id, roll_no, name, batch, email FROM students WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Student(userId, username, rs.getInt("id"), rs.getString("roll_no"),
                        rs.getString("name"), rs.getString("batch"), rs.getString("email"));
            }
        }
    }
}
