package database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static Connection connection;

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Path dataDir = Path.of("data");
                Files.createDirectories(dataDir);
                String url = "jdbc:sqlite:" + dataDir.resolve("college.db");
                connection = DriverManager.getConnection(url);
                try (Statement st = connection.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON");
                }
            } catch (Exception e) {
                throw new SQLException("Could not open database", e);
            }
        }
        return connection;
    }

    public static void initialize() throws SQLException {
        Connection conn = getConnection();
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password_hash TEXT NOT NULL,
                        role TEXT NOT NULL CHECK (role IN ('ADMIN', 'FACULTY', 'STUDENT'))
                    )
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS students (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL UNIQUE,
                        roll_no TEXT NOT NULL UNIQUE,
                        name TEXT NOT NULL,
                        batch TEXT NOT NULL,
                        email TEXT,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    )
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS faculty (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL UNIQUE,
                        name TEXT NOT NULL,
                        department TEXT,
                        email TEXT,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    )
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS subjects (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        code TEXT NOT NULL UNIQUE,
                        name TEXT NOT NULL
                    )
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS timetable (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        day TEXT NOT NULL,
                        period INTEGER NOT NULL,
                        subject_id INTEGER NOT NULL,
                        batch TEXT NOT NULL,
                        faculty_id INTEGER NOT NULL,
                        FOREIGN KEY (subject_id) REFERENCES subjects(id),
                        FOREIGN KEY (faculty_id) REFERENCES faculty(id),
                        UNIQUE (day, period, batch),
                        UNIQUE (day, period, faculty_id)
                    )
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS attendance (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        student_id INTEGER NOT NULL,
                        subject_id INTEGER NOT NULL,
                        session_date TEXT NOT NULL,
                        period INTEGER NOT NULL,
                        status TEXT NOT NULL CHECK (status IN ('PRESENT', 'ABSENT')),
                        FOREIGN KEY (student_id) REFERENCES students(id),
                        FOREIGN KEY (subject_id) REFERENCES subjects(id),
                        UNIQUE (student_id, subject_id, session_date, period)
                    )
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS exams (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        subject_id INTEGER NOT NULL,
                        batch TEXT NOT NULL,
                        exam_date TEXT NOT NULL,
                        FOREIGN KEY (subject_id) REFERENCES subjects(id)
                    )
                    """);
        }
        SeedData.insertIfEmpty();
    }
}
