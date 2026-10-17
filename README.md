# Academic Timetable & Attendance Management System

A Java Swing desktop application for a college project. It uses JDBC and SQLite. There is no web front end.

## Requirements

- Java 17 or later
- Maven 3.8 or later

Dependency (Maven downloads it):

- `org.xerial:sqlite-jdbc:3.47.1.0`

## How to run

From this folder:

```bash
mvn compile exec:java -Dexec.mainClass=app.Main
```

Or:

```bash
chmod +x run.sh
./run.sh
```

The first run creates `data/college.db` and loads sample records.

## Sample login credentials

| Role    | Username  | Password    |
|---------|-----------|-------------|
| Admin   | admin     | admin123    |
| Faculty | faculty1  | faculty123  |
| Faculty | faculty2  | faculty123  |
| Student | student1  | student123  |
| Student | student2  | student123  |
| Student | student3  | student123  |

Passwords are stored as SHA-256 hashes, not plain text.

## Project structure

```
academic-timetable-attendance/
  pom.xml
  run.sh
  README.md
  sql/schema.sql
  sql/sample_data.sql
  data/college.db          (created at runtime)
  src/main/java/
    app/Main.java
    app/ModuleTest.java
    model/                 Student, Faculty, Subject, ...
    database/              SQLite connection and JDBC DAOs
    ui/                    Login and dashboards
```

## Database tables

See `sql/schema.sql`. Tables: `users`, `students`, `faculty`, `subjects`, `timetable`, `attendance`, `exams`.

Attendance uniqueness: one row per student + subject + date + period.

Attendance percentage:

`(classes attended / total classes conducted) × 100`

Extra consecutive classes needed to reach 75%:

`x = 3 × total − 4 × present` (0 if already 75% or more)

## Basic test cases

1. Wrong password is rejected.
2. Admin, faculty and student each open their own dashboard.
3. Admin can add/update/delete students, faculty, subjects, timetable and exams.
4. Admin cannot assign the same faculty to two classes in the same day and period.
5. Faculty can mark Present/Absent and save; opening the same session again shows saved values.
6. Student `student1` (Alice) has CS101 at 50% and a below-75% warning; extra classes needed = 4.
7. Student `student2` (Bob) has CS101 at 100% (no shortage).
8. Subjects with no attendance show “No attendance recorded yet”.

Automated checks:

```bash
mvn -q compile exec:java -Dexec.mainClass=app.ModuleTest
```

## OOP used in this project

- **Encapsulation:** fields in model classes are private, with getters/setters.
- **Abstraction:** `User` is an abstract class; dashboards do not depend on extra unused details.
- **Inheritance:** `Admin`, `Faculty` and `Student` extend `User`.
- **Polymorphism:** login returns a `User`; `getWelcomeMessage()` and dashboard routing use the real type.
