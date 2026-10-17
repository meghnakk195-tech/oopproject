package ui;

import database.AttendanceDAO;
import database.StudentDAO;
import database.SubjectDAO;
import database.TimetableDAO;
import model.AttendanceRecord;
import model.AttendanceSummary;
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
import java.time.LocalDate;
import java.util.List;

public class FacultyDashboard extends JFrame {
    private final Faculty faculty;
    private final TimetableDAO timetableDAO = new TimetableDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    private final DefaultTableModel timetableModel = UiUtil.readOnlyModel(
            new String[]{"Day", "Period", "Subject", "Batch"});
    private final DefaultTableModel studentListModel = UiUtil.readOnlyModel(
            new String[]{"Roll No", "Name", "Batch", "Email"});
    private final DefaultTableModel markModel = new DefaultTableModel(
            new String[]{"Student ID", "Roll No", "Name", "Present"}, 0) {
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 3 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 3;
        }
    };
    private final DefaultTableModel savedModel = UiUtil.readOnlyModel(
            new String[]{"Roll No", "Name", "Date", "Period", "Status"});
    private final DefaultTableModel summaryModel = UiUtil.readOnlyModel(
            new String[]{"Roll", "Student", "Present", "Total", "%", "Extra for 75%", "Warning"});

    private final JComboBox<String> batchFilter = new JComboBox<>();
    private final JComboBox<TimetableEntry> classCombo = new JComboBox<>();
    private final JTextField dateField = new JTextField(LocalDate.now().toString(), 10);
    private final JComboBox<Subject> summarySubject = new JComboBox<>();

    public FacultyDashboard(Faculty faculty) {
        this.faculty = faculty;
        setTitle("Faculty Dashboard - " + faculty.getDisplayName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 640);
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(20, 55, 95));
        JLabel title = new JLabel("  " + faculty.getWelcomeMessage());
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
        tabs.addTab("My Timetable", timetablePanel());
        tabs.addTab("Class Students", studentPanel());
        tabs.addTab("Mark Attendance", markPanel());
        tabs.addTab("Saved Attendance", savedPanel());
        tabs.addTab("Subject Summary", summaryPanel());

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        loadFacultyData();
    }

    private JPanel timetablePanel() {
        JTable table = new JTable(timetableModel);
        UiUtil.fitTable(table);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel studentPanel() {
        JTable table = new JTable(studentListModel);
        UiUtil.fitTable(table);
        JButton load = new JButton("View students");
        load.addActionListener(e -> loadClassStudents());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Batch"));
        top.add(batchFilter);
        top.add(load);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel markPanel() {
        JTable table = new JTable(markModel);
        UiUtil.fitTable(table);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        JButton load = new JButton("Load students");
        load.addActionListener(e -> loadMarkTable());
        JButton save = new JButton("Save attendance");
        save.addActionListener(e -> saveAttendance());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Class"));
        top.add(classCombo);
        top.add(new JLabel("Date (YYYY-MM-DD)"));
        top.add(dateField);
        top.add(load);
        top.add(save);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel savedPanel() {
        JTable table = new JTable(savedModel);
        UiUtil.fitTable(table);
        JButton view = new JButton("View saved records");
        view.addActionListener(e -> loadSaved());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Uses the class and date from Mark Attendance"));
        top.add(view);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel summaryPanel() {
        JTable table = new JTable(summaryModel);
        UiUtil.fitTable(table);
        JButton view = new JButton("View subject-wise summary");
        view.addActionListener(e -> loadSummary());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Subject"));
        top.add(summarySubject);
        top.add(view);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadFacultyData() {
        try {
            UiUtil.clearTable(timetableModel);
            classCombo.removeAllItems();
            batchFilter.removeAllItems();
            summarySubject.removeAllItems();
            List<TimetableEntry> slots = timetableDAO.findByFaculty(faculty.getFacultyId());
            for (TimetableEntry slot : slots) {
                timetableModel.addRow(new Object[]{
                        slot.getDay(), slot.getPeriod(),
                        slot.getSubjectCode() + " - " + slot.getSubjectName(), slot.getBatch()
                });
                classCombo.addItem(slot);
                boolean exists = false;
                for (int i = 0; i < batchFilter.getItemCount(); i++) {
                    if (batchFilter.getItemAt(i).equals(slot.getBatch())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    batchFilter.addItem(slot.getBatch());
                }
            }
            for (Subject subject : subjectDAO.findAll()) {
                boolean assigned = false;
                for (TimetableEntry slot : slots) {
                    if (slot.getSubjectId() == subject.getId()) {
                        assigned = true;
                        break;
                    }
                }
                if (assigned) {
                    summarySubject.addItem(subject);
                }
            }
        } catch (Exception e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void loadClassStudents() {
        String batch = (String) batchFilter.getSelectedItem();
        if (batch == null) {
            UiUtil.error(this, "No batch assigned.");
            return;
        }
        try {
            UiUtil.clearTable(studentListModel);
            for (Student s : studentDAO.findByBatch(batch)) {
                studentListModel.addRow(new Object[]{s.getRollNo(), s.getName(), s.getBatch(), s.getEmail()});
            }
        } catch (Exception e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void loadMarkTable() {
        TimetableEntry slot = (TimetableEntry) classCombo.getSelectedItem();
        if (slot == null) {
            UiUtil.error(this, "No assigned class found.");
            return;
        }
        try {
            markModel.setRowCount(0);
            List<Student> students = studentDAO.findByBatch(slot.getBatch());
            List<AttendanceRecord> existing = attendanceDAO.findSession(
                    slot.getSubjectId(), dateField.getText().trim(), slot.getPeriod());
            for (Student s : students) {
                boolean present = true;
                for (AttendanceRecord rec : existing) {
                    if (rec.getStudentId() == s.getStudentId()) {
                        present = "PRESENT".equals(rec.getStatus());
                    }
                }
                markModel.addRow(new Object[]{s.getStudentId(), s.getRollNo(), s.getName(), present});
            }
            if (students.isEmpty()) {
                UiUtil.info(this, "No students found for batch " + slot.getBatch());
            }
        } catch (Exception e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void saveAttendance() {
        TimetableEntry slot = (TimetableEntry) classCombo.getSelectedItem();
        if (slot == null) {
            UiUtil.error(this, "Select a class.");
            return;
        }
        String date = dateField.getText().trim();
        if (date.isBlank()) {
            UiUtil.error(this, "Enter a date.");
            return;
        }
        try {
            for (int i = 0; i < markModel.getRowCount(); i++) {
                int studentId = (Integer) markModel.getValueAt(i, 0);
                boolean present = Boolean.TRUE.equals(markModel.getValueAt(i, 3));
                attendanceDAO.save(studentId, slot.getSubjectId(), date, slot.getPeriod(),
                        present ? "PRESENT" : "ABSENT");
            }
            UiUtil.info(this, "Attendance saved.");
            loadSaved();
        } catch (Exception e) {
            UiUtil.error(this, "Could not save attendance: " + e.getMessage());
        }
    }

    private void loadSaved() {
        TimetableEntry slot = (TimetableEntry) classCombo.getSelectedItem();
        if (slot == null) {
            return;
        }
        try {
            UiUtil.clearTable(savedModel);
            List<AttendanceRecord> rows = attendanceDAO.findSession(
                    slot.getSubjectId(), dateField.getText().trim(), slot.getPeriod());
            if (rows.isEmpty()) {
                savedModel.addRow(new Object[]{"-", "No attendance recorded yet", dateField.getText(), slot.getPeriod(), "-"});
                return;
            }
            for (AttendanceRecord rec : rows) {
                savedModel.addRow(new Object[]{
                        rec.getRollNo(), rec.getStudentName(), rec.getSessionDate(), rec.getPeriod(), rec.getStatus()
                });
            }
        } catch (Exception e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void loadSummary() {
        Subject subject = (Subject) summarySubject.getSelectedItem();
        if (subject == null) {
            UiUtil.error(this, "No assigned subject.");
            return;
        }
        try {
            UiUtil.clearTable(summaryModel);
            List<AttendanceSummary> rows = attendanceDAO.summariesForSubject(subject.getId());
            if (rows.isEmpty()) {
                summaryModel.addRow(new Object[]{"-", "No attendance recorded yet", 0, 0, "0.0", 0, "No records"});
                return;
            }
            for (AttendanceSummary row : rows) {
                summaryModel.addRow(new Object[]{
                        row.getRollNo(),
                        row.getStudentName(),
                        row.getPresentCount(),
                        row.getTotalCount(),
                        String.format("%.1f", row.getPercentage()),
                        row.extraClassesFor75(),
                        row.isShortage() ? "BELOW 75%" : "OK"
                });
            }
        } catch (Exception e) {
            UiUtil.error(this, e.getMessage());
        }
    }
}
