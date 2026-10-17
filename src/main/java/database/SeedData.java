package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SeedData {
    public static void insertIfEmpty() throws SQLException {
        Connection conn = Database.getConnection();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        conn.setAutoCommit(false);
        try {
            int adminUser = insertUser(conn, "admin", "admin123", "ADMIN");
            int f1User = insertUser(conn, "faculty1", "faculty123", "FACULTY");
            int f2User = insertUser(conn, "faculty2", "faculty123", "FACULTY");
            int s1User = insertUser(conn, "student1", "student123", "STUDENT");
            int s2User = insertUser(conn, "student2", "student123", "STUDENT");
            int s3User = insertUser(conn, "student3", "student123", "STUDENT");

            // admin user is created for login; admin details live in the users table
            use(adminUser);

            int f1 = insertFaculty(conn, f1User, "Dr. Rao", "Computer Science", "rao@college.edu");
            int f2 = insertFaculty(conn, f2User, "Prof. Mehta", "Computer Science", "mehta@college.edu");

            int alice = insertStudent(conn, s1User, "CS001", "Alice Thomas", "CS-A", "alice@college.edu");
            int bob = insertStudent(conn, s2User, "CS002", "Bob Kumar", "CS-A", "bob@college.edu");
            int carol = insertStudent(conn, s3User, "CS003", "Carol Nair", "CS-B", "carol@college.edu");

            int cs101 = insertSubject(conn, "CS101", "Programming");
            int cs102 = insertSubject(conn, "CS102", "Database Systems");
            int cs103 = insertSubject(conn, "CS103", "Mathematics");

            insertTimetable(conn, "Monday", 1, cs101, "CS-A", f1);
            insertTimetable(conn, "Monday", 2, cs102, "CS-A", f2);
            insertTimetable(conn, "Monday", 1, cs103, "CS-B", f2);
            insertTimetable(conn, "Tuesday", 1, cs103, "CS-A", f1);
            insertTimetable(conn, "Tuesday", 1, cs102, "CS-B", f2);
            insertTimetable(conn, "Tuesday", 2, cs101, "CS-B", f1);
            insertTimetable(conn, "Wednesday", 1, cs102, "CS-A", f2);
            insertTimetable(conn, "Wednesday", 2, cs101, "CS-A", f1);
            insertTimetable(conn, "Thursday", 1, cs101, "CS-A", f1);
            insertTimetable(conn, "Thursday", 2, cs103, "CS-B", f2);
            insertTimetable(conn, "Friday", 1, cs102, "CS-B", f2);
            insertTimetable(conn, "Friday", 2, cs103, "CS-A", f1);

            insertExam(conn, cs101, "CS-A", "2026-11-10");
            insertExam(conn, cs102, "CS-A", "2026-11-12");
            insertExam(conn, cs103, "CS-B", "2026-11-14");
            insertExam(conn, cs101, "CS-B", "2026-11-11");

            // Alice CS101: 2 present / 4 total = 50% (shortage warning)
            insertAttendance(conn, alice, cs101, "2026-10-06", 1, "PRESENT");
            insertAttendance(conn, alice, cs101, "2026-10-08", 2, "ABSENT");
            insertAttendance(conn, alice, cs101, "2026-10-09", 1, "ABSENT");
            insertAttendance(conn, alice, cs101, "2026-10-13", 1, "PRESENT");

            insertAttendance(conn, bob, cs101, "2026-10-06", 1, "PRESENT");
            insertAttendance(conn, bob, cs101, "2026-10-08", 2, "PRESENT");
            insertAttendance(conn, bob, cs101, "2026-10-09", 1, "PRESENT");
            insertAttendance(conn, bob, cs101, "2026-10-13", 1, "PRESENT");

            insertAttendance(conn, carol, cs101, "2026-10-07", 2, "PRESENT");
            insertAttendance(conn, carol, cs101, "2026-10-10", 2, "PRESENT");

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static void use(int ignored) {
        // keeps the admin user insert so login works
    }

    private static int insertUser(Connection conn, String username, String password, String role)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users(username, password_hash, role) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            ps.setString(3, role);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private static int insertFaculty(Connection conn, int userId, String name, String dept, String email)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO faculty(user_id, name, department, email) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, dept);
            ps.setString(4, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private static int insertStudent(Connection conn, int userId, String roll, String name,
                                     String batch, String email) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO students(user_id, roll_no, name, batch, email) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, roll);
            ps.setString(3, name);
            ps.setString(4, batch);
            ps.setString(5, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private static int insertSubject(Connection conn, String code, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO subjects(code, name) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private static void insertTimetable(Connection conn, String day, int period, int subjectId,
                                        String batch, int facultyId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO timetable(day, period, subject_id, batch, faculty_id) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, day);
            ps.setInt(2, period);
            ps.setInt(3, subjectId);
            ps.setString(4, batch);
            ps.setInt(5, facultyId);
            ps.executeUpdate();
        }
    }

    private static void insertExam(Connection conn, int subjectId, String batch, String date)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO exams(subject_id, batch, exam_date) VALUES (?, ?, ?)")) {
            ps.setInt(1, subjectId);
            ps.setString(2, batch);
            ps.setString(3, date);
            ps.executeUpdate();
        }
    }

    private static void insertAttendance(Connection conn, int studentId, int subjectId,
                                         String date, int period, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO attendance(student_id, subject_id, session_date, period, status) VALUES (?, ?, ?, ?, ?)")) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setString(3, date);
            ps.setInt(4, period);
            ps.setString(5, status);
            ps.executeUpdate();
        }
    }
}
