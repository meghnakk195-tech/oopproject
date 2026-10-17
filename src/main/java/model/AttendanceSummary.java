package model;

public class AttendanceSummary {
    private int studentId;
    private String rollNo;
    private String studentName;
    private String batch;
    private int subjectId;
    private String subjectCode;
    private String subjectName;
    private int presentCount;
    private int totalCount;

    public AttendanceSummary(int studentId, String rollNo, String studentName, String batch,
                             int subjectId, String subjectCode, String subjectName,
                             int presentCount, int totalCount) {
        this.studentId = studentId;
        this.rollNo = rollNo;
        this.studentName = studentName;
        this.batch = batch;
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.presentCount = presentCount;
        this.totalCount = totalCount;
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

    public String getBatch() {
        return batch;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public double getPercentage() {
        if (totalCount == 0) {
            return 0;
        }
        return (presentCount * 100.0) / totalCount;
    }

    public boolean hasRecords() {
        return totalCount > 0;
    }

    public boolean isShortage() {
        return hasRecords() && getPercentage() < 75.0;
    }

    /**
     * Extra consecutive classes needed so that (present + x) / (total + x) >= 75%.
     * Formula: x = max(0, 3 * total - 4 * present)
     */
    public int extraClassesFor75() {
        if (!hasRecords() || getPercentage() >= 75.0) {
            return 0;
        }
        int needed = 3 * totalCount - 4 * presentCount;
        return Math.max(0, needed);
    }
}
