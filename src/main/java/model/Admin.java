package model;

public class Admin extends User {
    public Admin(int userId, String username) {
        super(userId, username, "ADMIN");
    }

    @Override
    public String getDisplayName() {
        return "Administrator";
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome Admin. Manage students, faculty, subjects, timetable and exams.";
    }
}
