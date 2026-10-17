package database;

import model.Faculty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FacultyDAO {
    public List<Faculty> findAll() throws SQLException {
        String sql = """
                SELECT f.id, f.user_id, u.username, f.name, f.department, f.email
                FROM faculty f JOIN users u ON u.id = f.user_id
                ORDER BY f.name
                """;
        List<Faculty> list = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Faculty(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getString("email")
                ));
            }
        }
        return list;
    }

    public void insert(String username, String password, String name, String department, String email)
            throws SQLException {
        Connection conn = Database.getConnection();
        conn.setAutoCommit(false);
        try {
            int userId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users(username, password_hash, role) VALUES (?, ?, 'FACULTY')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username.trim());
                ps.setString(2, PasswordUtil.hash(password));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    userId = keys.getInt(1);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO faculty(user_id, name, department, email) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, userId);
                ps.setString(2, name.trim());
                ps.setString(3, department.trim());
                ps.setString(4, email == null ? "" : email.trim());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void update(int facultyId, String name, String department, String email) throws SQLException {
        String sql = "UPDATE faculty SET name = ?, department = ?, email = ? WHERE id = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, department.trim());
            ps.setString(3, email == null ? "" : email.trim());
            ps.setInt(4, facultyId);
            ps.executeUpdate();
        }
    }

    public void delete(int facultyId) throws SQLException {
        Connection conn = Database.getConnection();
        Integer userId = null;
        try (PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM faculty WHERE id = ?")) {
            ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM timetable WHERE faculty_id = ?")) {
            ps.setInt(1, facultyId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM faculty WHERE id = ?")) {
            ps.setInt(1, facultyId);
            ps.executeUpdate();
        }
        if (userId != null) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
        }
    }
}
