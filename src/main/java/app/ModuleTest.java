package app;

import database.AttendanceDAO;
import database.Database;
import database.TimetableDAO;
import database.UserDAO;
import model.AttendanceSummary;
import model.Faculty;
import model.Student;
import model.User;

/**
 * Console checks for login, timetable conflicts, attendance percentage and shortage.
 * Run: mvn -q exec:java -Dexec.mainClass=app.ModuleTest
 */
public class ModuleTest {
    public static void main(String[] args) throws Exception {
        Database.initialize();
        UserDAO users = new UserDAO();
        AttendanceDAO attendance = new AttendanceDAO();
        TimetableDAO timetable = new TimetableDAO();
        int passed = 0;
        int failed = 0;

        User admin = users.login("admin", "admin123");
        if (admin != null && "ADMIN".equals(admin.getRole())) {
            System.out.println("PASS login admin");
            passed++;
        } else {
            System.out.println("FAIL login admin");
            failed++;
        }

        User bad = users.login("admin", "wrong");
        if (bad == null) {
            System.out.println("PASS reject wrong password");
            passed++;
        } else {
            System.out.println("FAIL reject wrong password");
            failed++;
        }

        User faculty = users.login("faculty1", "faculty123");
        if (faculty instanceof Faculty) {
            System.out.println("PASS login faculty: " + faculty.getWelcomeMessage());
            passed++;
        } else {
            System.out.println("FAIL login faculty");
            failed++;
        }

        User student = users.login("student1", "student123");
        if (student instanceof Student s) {
            System.out.println("PASS login student: " + s.getWelcomeMessage());
            passed++;
            AttendanceSummary cs101 = null;
            for (AttendanceSummary row : attendance.summariesForStudent(s.getStudentId())) {
                if ("CS101".equals(row.getSubjectCode())) {
                    cs101 = row;
                }
            }
            if (cs101 != null && cs101.getTotalCount() == 4 && Math.abs(cs101.getPercentage() - 50.0) < 0.01
                    && cs101.isShortage() && cs101.extraClassesFor75() == 4) {
                System.out.println("PASS Alice CS101 50% needs 4 extra classes");
                passed++;
            } else {
                System.out.println("FAIL Alice CS101 percentage/shortage");
                failed++;
            }
        } else {
            System.out.println("FAIL login student");
            failed++;
        }

        try {
            timetable.insert("Monday", 1, 1, "CS-A", 1);
            System.out.println("FAIL timetable conflict not detected");
            failed++;
        } catch (Exception e) {
            System.out.println("PASS timetable conflict blocked: " + e.getMessage());
            passed++;
        }

        System.out.println("Result: " + passed + " passed, " + failed + " failed");
        System.exit(failed > 0 ? 1 : 0);
    }
}
