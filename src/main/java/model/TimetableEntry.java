package model;

public class TimetableEntry {
    private int id;
    private String day;
    private int period;
    private int subjectId;
    private String subjectCode;
    private String subjectName;
    private String batch;
    private int facultyId;
    private String facultyName;

    public TimetableEntry(int id, String day, int period, int subjectId, String subjectCode,
                          String subjectName, String batch, int facultyId, String facultyName) {
        this.id = id;
        this.day = day;
        this.period = period;
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.batch = batch;
        this.facultyId = facultyId;
        this.facultyName = facultyName;
    }

    public int getId() {
        return id;
    }

    public String getDay() {
        return day;
    }

    public int getPeriod() {
        return period;
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

    public String getBatch() {
        return batch;
    }

    public int getFacultyId() {
        return facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }

    @Override
    public String toString() {
        return day + " P" + period + " | " + subjectCode + " | " + batch;
    }
}
