package model;

public class Exam {
    private int id;
    private int subjectId;
    private String subjectCode;
    private String subjectName;
    private String batch;
    private String examDate;

    public Exam(int id, int subjectId, String subjectCode, String subjectName,
                String batch, String examDate) {
        this.id = id;
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.batch = batch;
        this.examDate = examDate;
    }

    public int getId() {
        return id;
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

    public String getExamDate() {
        return examDate;
    }
}
