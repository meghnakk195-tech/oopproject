package database;

import model.TimetableEntry;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TimetableDAO {
    private static final String SELECT = """
            SELECT t.id, t.day, t.period, t.subject_id, s.code AS subject_code, s.name AS subject_name,
                   t.batch, t.faculty_id, f.name AS faculty_name
            FROM timetable t
            JOIN subjects s ON s.id = t.subject_id
            JOIN faculty f ON f.id = t.faculty_id
            """;

    public List<TimetableEntry> findAll() throws SQLException {
        return query(SELECT + " ORDER BY CASE t.day "
                + "WHEN 'Monday' THEN 1 WHEN 'Tuesday' THEN 2 WHEN 'Wednesday' THEN 3 "
                + "WHEN 'Thursday' THEN 4 WHEN 'Friday' THEN 5 ELSE 6 END, t.period, t.batch");
    }

    public List<TimetableEntry> findByFaculty(int facultyId) throws SQLException {
        return query(SELECT + " WHERE t.faculty_id = ? ORDER BY t.day, t.period", facultyId);
    }

    public List<TimetableEntry> findByBatch(String batch) throws SQLException {
        String sql = SELECT + " WHERE t.batch = ? ORDER BY t.day, t.period";
        List<TimetableEntry> list = new ArrayList<>();
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

    public List<TimetableEntry> findByBatchAndDay(String batch, String day) throws SQLException {
        String sql = SELECT + " WHERE t.batch = ? AND t.day = ? ORDER BY t.period";
        List<TimetableEntry> list = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, batch);
            ps.setString(2, day);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public void insert(String day, int period, int subjectId, String batch, int facultyId) throws SQLException {
        checkConflicts(day, period, batch, facultyId, -1);
        String sql = "INSERT INTO timetable(day, period, subject_id, batch, faculty_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, day);
            ps.setInt(2, period);
            ps.setInt(3, subjectId);
            ps.setString(4, batch.trim());
            ps.setInt(5, facultyId);
            ps.executeUpdate();
        }
    }

    public void update(int id, String day, int period, int subjectId, String batch, int facultyId)
            throws SQLException {
        checkConflicts(day, period, batch, facultyId, id);
        String sql = "UPDATE timetable SET day = ?, period = ?, subject_id = ?, batch = ?, faculty_id = ? WHERE id = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, day);
            ps.setInt(2, period);
            ps.setInt(3, subjectId);
            ps.setString(4, batch.trim());
            ps.setInt(5, facultyId);
            ps.setInt(6, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "DELETE FROM timetable WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void checkConflicts(String day, int period, String batch, int facultyId, int ignoreId)
            throws SQLException {
        String facultySql = "SELECT COUNT(*) FROM timetable WHERE day = ? AND period = ? AND faculty_id = ? AND id <> ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(facultySql)) {
            ps.setString(1, day);
            ps.setInt(2, period);
            ps.setInt(3, facultyId);
            ps.setInt(4, ignoreId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("This faculty is already assigned in the same day and period.");
                }
            }
        }
        String batchSql = "SELECT COUNT(*) FROM timetable WHERE day = ? AND period = ? AND batch = ? AND id <> ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(batchSql)) {
            ps.setString(1, day);
            ps.setInt(2, period);
            ps.setString(3, batch.trim());
            ps.setInt(4, ignoreId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("This batch already has a class in the same day and period.");
                }
            }
        }
    }

    private List<TimetableEntry> query(String sql, Integer facultyId) throws SQLException {
        List<TimetableEntry> list = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            if (facultyId != null) {
                ps.setInt(1, facultyId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    private List<TimetableEntry> query(String sql) throws SQLException {
        return query(sql, null);
    }

    private TimetableEntry map(ResultSet rs) throws SQLException {
        return new TimetableEntry(
                rs.getInt("id"),
                rs.getString("day"),
                rs.getInt("period"),
                rs.getInt("subject_id"),
                rs.getString("subject_code"),
                rs.getString("subject_name"),
                rs.getString("batch"),
                rs.getInt("faculty_id"),
                rs.getString("faculty_name")
        );
    }
}
