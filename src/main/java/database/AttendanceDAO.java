package database;

import model.AttendanceRecord;
import model.AttendanceSummary;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {
    public void save(int studentId, int subjectId, String date, int period, String status) throws SQLException {
        String sql = """
                INSERT INTO attendance(student_id, subject_id, session_date, period, status)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(student_id, subject_id, session_date, period)
                DO UPDATE SET status = excluded.status
                """;
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setString(3, date);
            ps.setInt(4, period);
            ps.setString(5, status);
            ps.executeUpdate();
        }
    }

    public List<AttendanceRecord> findSession(int subjectId, String date, int period) throws SQLException {
        String sql = """
                SELECT a.student_id, s.roll_no, s.name, a.subject_id, sub.code, a.session_date, a.period, a.status
                FROM attendance a
                JOIN students s ON s.id = a.student_id
                JOIN subjects sub ON sub.id = a.subject_id
                WHERE a.subject_id = ? AND a.session_date = ? AND a.period = ?
                ORDER BY s.roll_no
                """;
        List<AttendanceRecord> list = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setString(2, date);
            ps.setInt(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AttendanceRecord(
                            rs.getInt("student_id"),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getInt("subject_id"),
                            rs.getString("code"),
                            rs.getString("session_date"),
                            rs.getInt("period"),
                            rs.getString("status")
                    ));
                }
            }
        }
        return list;
    }

    public List<AttendanceRecord> findByStudent(int studentId) throws SQLException {
        String sql = """
                SELECT a.student_id, s.roll_no, s.name, a.subject_id, sub.code, a.session_date, a.period, a.status
                FROM attendance a
                JOIN students s ON s.id = a.student_id
                JOIN subjects sub ON sub.id = a.subject_id
                WHERE a.student_id = ?
                ORDER BY a.session_date, a.period
                """;
        List<AttendanceRecord> list = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AttendanceRecord(
                            rs.getInt("student_id"),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getInt("subject_id"),
                            rs.getString("code"),
                            rs.getString("session_date"),
                            rs.getInt("period"),
                            rs.getString("status")
                    ));
                }
            }
        }
        return list;
    }

    public List<AttendanceSummary> summariesForStudent(int studentId) throws SQLException {
        String sql = """
                SELECT s.id AS student_id, s.roll_no, s.name AS student_name, s.batch,
                       sub.id AS subject_id, sub.code, sub.name AS subject_name,
                       COALESCE(SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END), 0) AS present_count,
                       COUNT(a.id) AS total_count
                FROM students s
                CROSS JOIN subjects sub
                LEFT JOIN attendance a ON a.student_id = s.id AND a.subject_id = sub.id
                WHERE s.id = ?
                GROUP BY s.id, sub.id
                ORDER BY sub.code
                """;
        return loadSummaries(sql, studentId);
    }

    public List<AttendanceSummary> summariesForSubject(int subjectId) throws SQLException {
        String sql = """
                SELECT s.id AS student_id, s.roll_no, s.name AS student_name, s.batch,
                       sub.id AS subject_id, sub.code, sub.name AS subject_name,
                       COALESCE(SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END), 0) AS present_count,
                       COUNT(a.id) AS total_count
                FROM students s
                JOIN subjects sub ON sub.id = ?
                LEFT JOIN attendance a ON a.student_id = s.id AND a.subject_id = sub.id
                GROUP BY s.id, sub.id
                HAVING total_count > 0
                ORDER BY s.roll_no
                """;
        return loadSummaries(sql, subjectId);
    }

    public List<AttendanceSummary> allStudentSummaries() throws SQLException {
        String sql = """
                SELECT s.id AS student_id, s.roll_no, s.name AS student_name, s.batch,
                       sub.id AS subject_id, sub.code, sub.name AS subject_name,
                       COALESCE(SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END), 0) AS present_count,
                       COUNT(a.id) AS total_count
                FROM students s
                JOIN attendance a ON a.student_id = s.id
                JOIN subjects sub ON sub.id = a.subject_id
                GROUP BY s.id, sub.id
                ORDER BY s.roll_no, sub.code
                """;
        return loadSummaries(sql, null);
    }

    public List<AttendanceSummary> shortageReport() throws SQLException {
        List<AttendanceSummary> all = allStudentSummaries();
        List<AttendanceSummary> shortage = new ArrayList<>();
        for (AttendanceSummary row : all) {
            if (row.isShortage()) {
                shortage.add(row);
            }
        }
        return shortage;
    }

    private List<AttendanceSummary> loadSummaries(String sql, Integer id) throws SQLException {
        List<AttendanceSummary> list = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            if (id != null) {
                ps.setInt(1, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AttendanceSummary(
                            rs.getInt("student_id"),
                            rs.getString("roll_no"),
                            rs.getString("student_name"),
                            rs.getString("batch"),
                            rs.getInt("subject_id"),
                            rs.getString("code"),
                            rs.getString("subject_name"),
                            rs.getInt("present_count"),
                            rs.getInt("total_count")
                    ));
                }
            }
        }
        return list;
    }
}
