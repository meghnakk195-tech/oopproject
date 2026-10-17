package database;

import model.Exam;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExamDAO {
    private static final String SELECT = """
            SELECT e.id, e.subject_id, s.code, s.name, e.batch, e.exam_date
            FROM exams e JOIN subjects s ON s.id = e.subject_id
            """;

    public List<Exam> findAll() throws SQLException {
        List<Exam> list = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                SELECT + " ORDER BY e.exam_date, s.code");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public List<Exam> findByBatch(String batch) throws SQLException {
        List<Exam> list = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                SELECT + " WHERE e.batch = ? ORDER BY e.exam_date")) {
            ps.setString(1, batch);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public void insert(int subjectId, String batch, String examDate) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "INSERT INTO exams(subject_id, batch, exam_date) VALUES (?, ?, ?)")) {
            ps.setInt(1, subjectId);
            ps.setString(2, batch.trim());
            ps.setString(3, examDate.trim());
            ps.executeUpdate();
        }
    }

    public void update(int id, int subjectId, String batch, String examDate) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "UPDATE exams SET subject_id = ?, batch = ?, exam_date = ? WHERE id = ?")) {
            ps.setInt(1, subjectId);
            ps.setString(2, batch.trim());
            ps.setString(3, examDate.trim());
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "DELETE FROM exams WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Exam map(ResultSet rs) throws SQLException {
        return new Exam(
                rs.getInt("id"),
                rs.getInt("subject_id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("batch"),
                rs.getString("exam_date")
        );
    }
}
