package database;

import model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    public List<Student> findAll() throws SQLException {
        String sql = """
                SELECT s.id, s.user_id, u.username, s.roll_no, s.name, s.batch, s.email
                FROM students s JOIN users u ON u.id = s.user_id
                ORDER BY s.roll_no
                """;
        List<Student> list = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public List<Student> findByBatch(String batch) throws SQLException {
        String sql = """
                SELECT s.id, s.user_id, u.username, s.roll_no, s.name, s.batch, s.email
                FROM students s JOIN users u ON u.id = s.user_id
                WHERE s.batch = ?
                ORDER BY s.roll_no
                """;
        List<Student> list = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, batch);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public void insert(String username, String password, String rollNo, String name,
                       String batch, String email) throws SQLException {
        Connection conn = Database.getConnection();
        conn.setAutoCommit(false);
        try {
            int userId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users(username, password_hash, role) VALUES (?, ?, 'STUDENT')",
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
                    "INSERT INTO students(user_id, roll_no, name, batch, email) VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, userId);
                ps.setString(2, rollNo.trim());
                ps.setString(3, name.trim());
                ps.setString(4, batch.trim());
                ps.setString(5, email == null ? "" : email.trim());
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

    public void update(int studentId, String rollNo, String name, String batch, String email)
            throws SQLException {
        String sql = "UPDATE students SET roll_no = ?, name = ?, batch = ?, email = ? WHERE id = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, rollNo.trim());
            ps.setString(2, name.trim());
            ps.setString(3, batch.trim());
            ps.setString(4, email == null ? "" : email.trim());
            ps.setInt(5, studentId);
            ps.executeUpdate();
        }
    }

    public void delete(int studentId) throws SQLException {
        Connection conn = Database.getConnection();
        Integer userId = null;
        try (PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM students WHERE id = ?")) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM attendance WHERE student_id = ?")) {
            ps.setInt(1, studentId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE id = ?")) {
            ps.setInt(1, studentId);
            ps.executeUpdate();
        }
        if (userId != null) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
        }
    }

    private Student map(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getInt("id"),
                rs.getString("roll_no"),
                rs.getString("name"),
                rs.getString("batch"),
                rs.getString("email")
        );
    }
}
