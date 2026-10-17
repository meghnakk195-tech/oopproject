package model;

public class AttendanceRecord {
    private int studentId;
    private String rollNo;
    private String studentName;
    private int subjectId;
    private String subjectCode;
    private String sessionDate;
    private int period;
    private String status;

    public AttendanceRecord(int studentId, String rollNo, String studentName, int subjectId,
                            String subjectCode, String sessionDate, int period, String status) {
        this.studentId = studentId;
        this.rollNo = rollNo;
        this.studentName = studentName;
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.sessionDate = sessionDate;
        this.period = period;
        this.status = status;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getRollNo() {
        return rollNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public int getPeriod() {
        return period;
    }

    public String getStatus() {
        return status;
    }
}
