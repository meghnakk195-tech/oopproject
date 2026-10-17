package model;

public class Faculty extends User {
    private int facultyId;
    private String name;
    private String department;
    private String email;

    public Faculty(int userId, String username, int facultyId, String name,
                   String department, String email) {
        super(userId, username, "FACULTY");
        this.facultyId = facultyId;
        this.name = name;
        this.department = department;
        this.email = email;
    }

    public int getFacultyId() {
        return facultyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome " + name + " (" + department + "). Mark attendance and view your timetable.";
    }

    @Override
    public String toString() {
        return name + " (" + department + ")";
    }
}
