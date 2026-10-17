package ui;

import database.AttendanceDAO;
import database.ExamDAO;
import database.FacultyDAO;
import database.StudentDAO;
import database.SubjectDAO;
import database.TimetableDAO;
import model.Admin;
import model.AttendanceSummary;
import model.Exam;
import model.Faculty;
import model.Student;
import model.Subject;
import model.TimetableEntry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;

public class AdminDashboard extends JFrame {
    private final StudentDAO studentDAO = new StudentDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final TimetableDAO timetableDAO = new TimetableDAO();
    private final ExamDAO examDAO = new ExamDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    private final DefaultTableModel studentModel = UiUtil.readOnlyModel(
            new String[]{"ID", "Roll No", "Name", "Batch", "Email", "Username"});
    private final JTextField stUser = new JTextField();
    private final JTextField stPass = new JTextField();
    private final JTextField stRoll = new JTextField();
    private final JTextField stName = new JTextField();
    private final JTextField stBatch = new JTextField();
    private final JTextField stEmail = new JTextField();
    private Integer selectedStudentId;

    private final DefaultTableModel facultyModel = UiUtil.readOnlyModel(
            new String[]{"ID", "Name", "Department", "Email", "Username"});
    private final JTextField fUser = new JTextField();
    private final JTextField fPass = new JTextField();
    private final JTextField fName = new JTextField();
    private final JTextField fDept = new JTextField();
    private final JTextField fEmail = new JTextField();
    private Integer selectedFacultyId;

    private final DefaultTableModel subjectModel = UiUtil.readOnlyModel(new String[]{"ID", "Code", "Name"});
    private final JTextField subCode = new JTextField();
    private final JTextField subName = new JTextField();
    private Integer selectedSubjectId;

    private final DefaultTableModel ttModel = UiUtil.readOnlyModel(
            new String[]{"ID", "Day", "Period", "Subject", "Batch", "Faculty"});
    private final JComboBox<String> ttDay = new JComboBox<>(
            new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"});
    private final JComboBox<Integer> ttPeriod = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6});
    private final JComboBox<Subject> ttSubject = new JComboBox<>();
    private final JTextField ttBatch = new JTextField();
    private final JComboBox<Faculty> ttFaculty = new JComboBox<>();
    private Integer selectedTimetableId;

    private final DefaultTableModel examModel = UiUtil.readOnlyModel(
            new String[]{"ID", "Subject", "Batch", "Exam Date"});
    private final JComboBox<Subject> exSubject = new JComboBox<>();
    private final JTextField exBatch = new JTextField();
    private final JTextField exDate = new JTextField();
    private Integer selectedExamId;

    private final DefaultTableModel reportModel = UiUtil.readOnlyModel(
            new String[]{"Roll", "Student", "Batch", "Subject", "Present", "Total", "%", "Extra for 75%", "Warning"});

    public AdminDashboard(Admin admin) {
        setTitle("Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 640);
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(20, 55, 95));
        JLabel title = new JLabel("  " + admin.getWelcomeMessage());
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        header.add(title, BorderLayout.CENTER);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(logout);
        header.add(right, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Students", studentPanel());
        tabs.addTab("Faculty", facultyPanel());
        tabs.addTab("Subjects", subjectPanel());
        tabs.addTab("Timetable", timetablePanel());
        tabs.addTab("Exams", examPanel());
        tabs.addTab("Reports", reportPanel());

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        reloadAll();
    }

    private void reloadAll() {
        loadStudents();
        loadFaculty();
        loadSubjects();
        loadTimetable();
        loadExams();
        loadReports(false);
        fillSubjectCombo(ttSubject);
        fillSubjectCombo(exSubject);
        fillFacultyCombo();
    }

    private JPanel studentPanel() {
        JTable table = new JTable(studentModel);
        UiUtil.fitTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() < 0) {
                return;
            }
            int row = table.getSelectedRow();
            selectedStudentId = (Integer) studentModel.getValueAt(row, 0);
            stRoll.setText(String.valueOf(studentModel.getValueAt(row, 1)));
            stName.setText(String.valueOf(studentModel.getValueAt(row, 2)));
            stBatch.setText(String.valueOf(studentModel.getValueAt(row, 3)));
            stEmail.setText(String.valueOf(studentModel.getValueAt(row, 4)));
            stUser.setText(String.valueOf(studentModel.getValueAt(row, 5)));
        });

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Student form"));
        form.add(new JLabel("Username (for new login)"));
        form.add(stUser);
        form.add(new JLabel("Password (for new login)"));
        form.add(stPass);
        form.add(new JLabel("Roll No"));
        form.add(stRoll);
        form.add(new JLabel("Name"));
        form.add(stName);
        form.add(new JLabel("Batch"));
        form.add(stBatch);
        form.add(new JLabel("Email"));
        form.add(stEmail);

        JButton add = new JButton("Add");
        add.addActionListener(e -> addStudent());
        JButton update = new JButton("Update");
        update.addActionListener(e -> updateStudent());
        JButton delete = new JButton("Delete");
        delete.addActionListener(e -> deleteStudent());
        JButton refresh = new JButton("View");
        refresh.addActionListener(e -> loadStudents());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(refresh);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel facultyPanel() {
        JTable table = new JTable(facultyModel);
        UiUtil.fitTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() < 0) {
                return;
            }
            int row = table.getSelectedRow();
            selectedFacultyId = (Integer) facultyModel.getValueAt(row, 0);
            fName.setText(String.valueOf(facultyModel.getValueAt(row, 1)));
            fDept.setText(String.valueOf(facultyModel.getValueAt(row, 2)));
            fEmail.setText(String.valueOf(facultyModel.getValueAt(row, 3)));
            fUser.setText(String.valueOf(facultyModel.getValueAt(row, 4)));
        });

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Faculty form"));
        form.add(new JLabel("Username (for new login)"));
        form.add(fUser);
        form.add(new JLabel("Password (for new login)"));
        form.add(fPass);
        form.add(new JLabel("Name"));
        form.add(fName);
        form.add(new JLabel("Department"));
        form.add(fDept);
        form.add(new JLabel("Email"));
        form.add(fEmail);

        JButton add = new JButton("Add");
        add.addActionListener(e -> addFaculty());
        JButton update = new JButton("Update");
        update.addActionListener(e -> updateFaculty());
        JButton delete = new JButton("Delete");
        delete.addActionListener(e -> deleteFaculty());
        JButton refresh = new JButton("View");
        refresh.addActionListener(e -> loadFaculty());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(refresh);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel subjectPanel() {
        JTable table = new JTable(subjectModel);
        UiUtil.fitTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() < 0) {
                return;
            }
            int row = table.getSelectedRow();
            selectedSubjectId = (Integer) subjectModel.getValueAt(row, 0);
            subCode.setText(String.valueOf(subjectModel.getValueAt(row, 1)));
            subName.setText(String.valueOf(subjectModel.getValueAt(row, 2)));
        });

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Subject form"));
        form.add(new JLabel("Subject Code"));
        form.add(subCode);
        form.add(new JLabel("Subject Name"));
        form.add(subName);

        JButton add = new JButton("Add");
        add.addActionListener(e -> addSubject());
        JButton update = new JButton("Update");
        update.addActionListener(e -> updateSubject());
        JButton delete = new JButton("Delete");
        delete.addActionListener(e -> deleteSubject());
        JButton refresh = new JButton("View");
        refresh.addActionListener(e -> loadSubjects());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(refresh);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel timetablePanel() {
        JTable table = new JTable(ttModel);
        UiUtil.fitTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() < 0) {
                return;
            }
            int row = table.getSelectedRow();
            selectedTimetableId = (Integer) ttModel.getValueAt(row, 0);
            ttDay.setSelectedItem(String.valueOf(ttModel.getValueAt(row, 1)));
            ttPeriod.setSelectedItem((Integer) ttModel.getValueAt(row, 2));
            ttBatch.setText(String.valueOf(ttModel.getValueAt(row, 4)));
        });

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Create / update timetable"));
        form.add(new JLabel("Day"));
        form.add(ttDay);
        form.add(new JLabel("Period"));
        form.add(ttPeriod);
        form.add(new JLabel("Subject"));
        form.add(ttSubject);
        form.add(new JLabel("Batch"));
        form.add(ttBatch);
        form.add(new JLabel("Faculty"));
        form.add(ttFaculty);

        JButton add = new JButton("Add");
        add.addActionListener(e -> addTimetable());
        JButton update = new JButton("Update");
        update.addActionListener(e -> updateTimetable());
        JButton delete = new JButton("Delete");
        delete.addActionListener(e -> deleteTimetable());
        JButton refresh = new JButton("View");
        refresh.addActionListener(e -> loadTimetable());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(refresh);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel examPanel() {
        JTable table = new JTable(examModel);
        UiUtil.fitTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() < 0) {
                return;
            }
            int row = table.getSelectedRow();
            selectedExamId = (Integer) examModel.getValueAt(row, 0);
            exBatch.setText(String.valueOf(examModel.getValueAt(row, 2)));
            exDate.setText(String.valueOf(examModel.getValueAt(row, 3)));
        });

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Examination dates"));
        form.add(new JLabel("Subject"));
        form.add(exSubject);
        form.add(new JLabel("Batch"));
        form.add(exBatch);
        form.add(new JLabel("Exam Date (YYYY-MM-DD)"));
        form.add(exDate);

        JButton add = new JButton("Add");
        add.addActionListener(e -> addExam());
        JButton update = new JButton("Update");
        update.addActionListener(e -> updateExam());
        JButton delete = new JButton("Delete");
        delete.addActionListener(e -> deleteExam());
        JButton refresh = new JButton("View");
        refresh.addActionListener(e -> loadExams());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(refresh);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel reportPanel() {
        JTable table = new JTable(reportModel);
        UiUtil.fitTable(table);
        JButton all = new JButton("Student attendance summaries");
        all.addActionListener(e -> loadReports(false));
        JButton shortage = new JButton("Attendance shortage warnings");
        shortage.addActionListener(e -> loadReports(true));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(all);
        buttons.add(shortage);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(buttons, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadStudents() {
        try {
            UiUtil.clearTable(studentModel);
            for (Student s : studentDAO.findAll()) {
                studentModel.addRow(new Object[]{
                        s.getStudentId(), s.getRollNo(), s.getName(), s.getBatch(), s.getEmail(), s.getUsername()
                });
            }
        } catch (SQLException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void addStudent() {
        if (stUser.getText().isBlank() || stPass.getText().isBlank() || stRoll.getText().isBlank()
                || stName.getText().isBlank() || stBatch.getText().isBlank()) {
            UiUtil.error(this, "Username, password, roll no, name and batch are required.");
            return;
        }
        try {
            studentDAO.insert(stUser.getText(), stPass.getText(), stRoll.getText(),
                    stName.getText(), stBatch.getText(), stEmail.getText());
            UiUtil.info(this, "Student added.");
            loadStudents();
        } catch (SQLException e) {
            UiUtil.error(this, "Could not add student: " + e.getMessage());
        }
    }

    private void updateStudent() {
        if (selectedStudentId == null) {
            UiUtil.error(this, "Select a student from the table.");
            return;
        }
        try {
            studentDAO.update(selectedStudentId, stRoll.getText(), stName.getText(),
                    stBatch.getText(), stEmail.getText());
            UiUtil.info(this, "Student updated.");
            loadStudents();
        } catch (SQLException e) {
            UiUtil.error(this, "Could not update student: " + e.getMessage());
        }
    }

    private void deleteStudent() {
        if (selectedStudentId == null) {
            UiUtil.error(this, "Select a student from the table.");
            return;
        }
        if (!UiUtil.confirm(this, "Delete this student and related attendance?")) {
            return;
        }
        try {
            studentDAO.delete(selectedStudentId);
            selectedStudentId = null;
            UiUtil.info(this, "Student deleted.");
            loadStudents();
        } catch (SQLException e) {
            UiUtil.error(this, "Could not delete student: " + e.getMessage());
        }
    }

    private void loadFaculty() {
        try {
            UiUtil.clearTable(facultyModel);
            for (Faculty f : facultyDAO.findAll()) {
                facultyModel.addRow(new Object[]{
                        f.getFacultyId(), f.getName(), f.getDepartment(), f.getEmail(), f.getUsername()
                });
            }
            fillFacultyCombo();
        } catch (SQLException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void addFaculty() {
        if (fUser.getText().isBlank() || fPass.getText().isBlank() || fName.getText().isBlank()) {
            UiUtil.error(this, "Username, password and name are required.");
            return;
        }
        try {
            facultyDAO.insert(fUser.getText(), fPass.getText(), fName.getText(), fDept.getText(), fEmail.getText());
            UiUtil.info(this, "Faculty added.");
            loadFaculty();
        } catch (SQLException e) {
            UiUtil.error(this, "Could not add faculty: " + e.getMessage());
        }
    }

    private void updateFaculty() {
        if (selectedFacultyId == null) {
            UiUtil.error(this, "Select a faculty member from the table.");
            return;
        }
        try {
            facultyDAO.update(selectedFacultyId, fName.getText(), fDept.getText(), fEmail.getText());
            UiUtil.info(this, "Faculty updated.");
            loadFaculty();
        } catch (SQLException e) {
            UiUtil.error(this, "Could not update faculty: " + e.getMessage());
        }
    }

    private void deleteFaculty() {
        if (selectedFacultyId == null) {
            UiUtil.error(this, "Select a faculty member from the table.");
            return;
        }
        if (!UiUtil.confirm(this, "Delete this faculty member and their timetable entries?")) {
            return;
        }
        try {
            facultyDAO.delete(selectedFacultyId);
            selectedFacultyId = null;
            UiUtil.info(this, "Faculty deleted.");
            loadFaculty();
            loadTimetable();
        } catch (SQLException e) {
            UiUtil.error(this, "Could not delete faculty: " + e.getMessage());
        }
    }

    private void loadSubjects() {
        try {
            UiUtil.clearTable(subjectModel);
            for (Subject s : subjectDAO.findAll()) {
                subjectModel.addRow(new Object[]{s.getId(), s.getCode(), s.getName()});
            }
            fillSubjectCombo(ttSubject);
            fillSubjectCombo(exSubject);
        } catch (SQLException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void addSubject() {
        if (subCode.getText().isBlank() || subName.getText().isBlank()) {
            UiUtil.error(this, "Subject code and name are required.");
            return;
        }
        try {
            subjectDAO.insert(subCode.getText(), subName.getText());
            UiUtil.info(this, "Subject added.");
            loadSubjects();
        } catch (SQLException e) {
            UiUtil.error(this, "Could not add subject: " + e.getMessage());
        }
    }

    private void updateSubject() {
        if (selectedSubjectId == null) {
            UiUtil.error(this, "Select a subject from the table.");
            return;
        }
        try {
            subjectDAO.update(selectedSubjectId, subCode.getText(), subName.getText());
            UiUtil.info(this, "Subject updated.");
            loadSubjects();
        } catch (SQLException e) {
            UiUtil.error(this, "Could not update subject: " + e.getMessage());
        }
    }

    private void deleteSubject() {
        if (selectedSubjectId == null) {
            UiUtil.error(this, "Select a subject from the table.");
            return;
        }
        if (!UiUtil.confirm(this, "Delete this subject and related timetable/attendance/exams?")) {
            return;
        }
        try {
            subjectDAO.delete(selectedSubjectId);
            selectedSubjectId = null;
            UiUtil.info(this, "Subject deleted.");
            reloadAll();
        } catch (SQLException e) {
            UiUtil.error(this, "Could not delete subject: " + e.getMessage());
        }
    }

    private void loadTimetable() {
        try {
            UiUtil.clearTable(ttModel);
            for (TimetableEntry t : timetableDAO.findAll()) {
                ttModel.addRow(new Object[]{
                        t.getId(), t.getDay(), t.getPeriod(),
                        t.getSubjectCode() + " - " + t.getSubjectName(),
                        t.getBatch(), t.getFacultyName()
                });
            }
        } catch (SQLException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void addTimetable() {
        Subject subject = (Subject) ttSubject.getSelectedItem();
        Faculty faculty = (Faculty) ttFaculty.getSelectedItem();
        if (subject == null || faculty == null || ttBatch.getText().isBlank()) {
            UiUtil.error(this, "Select subject, faculty and enter batch.");
            return;
        }
        try {
            timetableDAO.insert((String) ttDay.getSelectedItem(), (Integer) ttPeriod.getSelectedItem(),
                    subject.getId(), ttBatch.getText(), faculty.getFacultyId());
            UiUtil.info(this, "Timetable entry added.");
            loadTimetable();
        } catch (SQLException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void updateTimetable() {
        if (selectedTimetableId == null) {
            UiUtil.error(this, "Select a timetable row.");
            return;
        }
        Subject subject = (Subject) ttSubject.getSelectedItem();
        Faculty faculty = (Faculty) ttFaculty.getSelectedItem();
        if (subject == null || faculty == null || ttBatch.getText().isBlank()) {
            UiUtil.error(this, "Select subject, faculty and enter batch.");
            return;
        }
        try {
            timetableDAO.update(selectedTimetableId, (String) ttDay.getSelectedItem(),
                    (Integer) ttPeriod.getSelectedItem(), subject.getId(), ttBatch.getText(),
                    faculty.getFacultyId());
            UiUtil.info(this, "Timetable entry updated.");
            loadTimetable();
        } catch (SQLException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void deleteTimetable() {
        if (selectedTimetableId == null) {
            UiUtil.error(this, "Select a timetable row.");
            return;
        }
        try {
            timetableDAO.delete(selectedTimetableId);
            selectedTimetableId = null;
            UiUtil.info(this, "Timetable entry deleted.");
            loadTimetable();
        } catch (SQLException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void loadExams() {
        try {
            UiUtil.clearTable(examModel);
            for (Exam exam : examDAO.findAll()) {
                examModel.addRow(new Object[]{
                        exam.getId(),
                        exam.getSubjectCode() + " - " + exam.getSubjectName(),
                        exam.getBatch(),
                        exam.getExamDate()
                });
            }
        } catch (SQLException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void addExam() {
        Subject subject = (Subject) exSubject.getSelectedItem();
        if (subject == null || exBatch.getText().isBlank() || exDate.getText().isBlank()) {
            UiUtil.error(this, "Subject, batch and exam date are required.");
            return;
        }
        try {
            examDAO.insert(subject.getId(), exBatch.getText(), exDate.getText());
            UiUtil.info(this, "Exam date saved.");
            loadExams();
        } catch (SQLException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void updateExam() {
        if (selectedExamId == null) {
            UiUtil.error(this, "Select an exam row.");
            return;
        }
        Subject subject = (Subject) exSubject.getSelectedItem();
        if (subject == null) {
            UiUtil.error(this, "Select a subject.");
            return;
        }
        try {
            examDAO.update(selectedExamId, subject.getId(), exBatch.getText(), exDate.getText());
            UiUtil.info(this, "Exam updated.");
            loadExams();
        } catch (SQLException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void deleteExam() {
        if (selectedExamId == null) {
            UiUtil.error(this, "Select an exam row.");
            return;
        }
        try {
            examDAO.delete(selectedExamId);
            selectedExamId = null;
            UiUtil.info(this, "Exam deleted.");
            loadExams();
        } catch (SQLException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void loadReports(boolean shortageOnly) {
        try {
            UiUtil.clearTable(reportModel);
            List<AttendanceSummary> rows = shortageOnly
                    ? attendanceDAO.shortageReport()
                    : attendanceDAO.allStudentSummaries();
            for (AttendanceSummary row : rows) {
                String warning = row.isShortage() ? "BELOW 75%" : "OK";
                if (!row.hasRecords()) {
                    warning = "No records";
                }
                reportModel.addRow(new Object[]{
                        row.getRollNo(),
                        row.getStudentName(),
                        row.getBatch(),
                        row.getSubjectCode() + " " + row.getSubjectName(),
                        row.getPresentCount(),
                        row.getTotalCount(),
                        String.format("%.1f", row.getPercentage()),
                        row.extraClassesFor75(),
                        warning
                });
            }
            if (rows.isEmpty()) {
                reportModel.addRow(new Object[]{"-", "No attendance recorded yet", "-", "-", "-", "-", "-", "-", "-"});
            }
        } catch (SQLException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void fillSubjectCombo(JComboBox<Subject> combo) {
        try {
            combo.removeAllItems();
            for (Subject s : subjectDAO.findAll()) {
                combo.addItem(s);
            }
        } catch (SQLException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void fillFacultyCombo() {
        try {
            ttFaculty.removeAllItems();
            for (Faculty f : facultyDAO.findAll()) {
                ttFaculty.addItem(f);
            }
        } catch (SQLException e) {
            UiUtil.error(this, e.getMessage());
        }
    }
}
