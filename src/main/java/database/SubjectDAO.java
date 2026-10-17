package database;

import model.Subject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {
    public List<Subject> findAll() throws SQLException {
        List<Subject> list = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT id, code, name FROM subjects ORDER BY code");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Subject(rs.getInt("id"), rs.getString("code"), rs.getString("name")));
            }
        }
        return list;
    }

    public void insert(String code, String name) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "INSERT INTO subjects(code, name) VALUES (?, ?)")) {
            ps.setString(1, code.trim().toUpperCase());
            ps.setString(2, name.trim());
            ps.executeUpdate();
        }
    }

    public void update(int id, String code, String name) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "UPDATE subjects SET code = ?, name = ? WHERE id = ?")) {
            ps.setString(1, code.trim().toUpperCase());
            ps.setString(2, name.trim());
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "DELETE FROM exams WHERE subject_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "DELETE FROM attendance WHERE subject_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "DELETE FROM timetable WHERE subject_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "DELETE FROM subjects WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
