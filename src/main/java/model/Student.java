package model;

public class Student extends User {
    private int studentId;
    private String rollNo;
    private String name;
    private String batch;
    private String email;

    public Student(int userId, String username, int studentId, String rollNo,
                   String name, String batch, String email) {
        super(userId, username, "STUDENT");
        this.studentId = studentId;
        this.rollNo = rollNo;
        this.name = name;
        this.batch = batch;
        this.email = email;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
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
        return "Welcome " + name + " (" + rollNo + "), batch " + batch + ".";
    }
}
