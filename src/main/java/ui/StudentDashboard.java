package ui;

import database.AttendanceDAO;
import database.ExamDAO;
import database.TimetableDAO;
import model.AttendanceRecord;
import model.AttendanceSummary;
import model.Exam;
import model.Student;
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
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

public class StudentDashboard extends JFrame {
    private final Student student;
    private final TimetableDAO timetableDAO = new TimetableDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ExamDAO examDAO = new ExamDAO();

    private final DefaultTableModel weeklyModel = UiUtil.readOnlyModel(
            new String[]{"Day", "Period", "Subject", "Faculty"});
    private final DefaultTableModel dailyModel = UiUtil.readOnlyModel(
            new String[]{"Period", "Subject", "Faculty"});
    private final DefaultTableModel summaryModel = UiUtil.readOnlyModel(
            new String[]{"Subject", "Present", "Total", "%", "Extra classes for 75%", "Status"});
    private final DefaultTableModel detailModel = UiUtil.readOnlyModel(
            new String[]{"Date", "Period", "Subject", "Status"});
    private final DefaultTableModel examModel = UiUtil.readOnlyModel(
            new String[]{"Subject", "Batch", "Exam Date"});
    private final JComboBox<String> dayCombo = new JComboBox<>(
            new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"});
    private final JLabel warningLabel = new JLabel(" ");

    public StudentDashboard(Student student) {
        this.student = student;
        setTitle("Student Dashboard - " + student.getDisplayName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 640);
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(20, 55, 95));
        JLabel title = new JLabel("  " + student.getWelcomeMessage());
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
        tabs.addTab("Weekly Timetable", weeklyPanel());
        tabs.addTab("Daily Timetable", dailyPanel());
        tabs.addTab("Attendance", attendancePanel());
        tabs.addTab("Exams", examPanel());

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        loadWeekly();
        loadDaily();
        loadAttendance();
        loadExams();
    }

    private JPanel weeklyPanel() {
        JTable table = new JTable(weeklyModel);
        UiUtil.fitTable(table);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel dailyPanel() {
        JTable table = new JTable(dailyModel);
        UiUtil.fitTable(table);
        JButton view = new JButton("View day");
        view.addActionListener(e -> loadDaily());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Day"));
        top.add(dayCombo);
        top.add(view);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel attendancePanel() {
        warningLabel.setForeground(new Color(160, 30, 30));
        warningLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        JTable summary = new JTable(summaryModel);
        JTable detail = new JTable(detailModel);
        UiUtil.fitTable(summary);
        UiUtil.fitTable(detail);

        JPanel split = new JPanel(new GridTwo());
        split.add(wrap("Subject-wise attendance", summary));
        split.add(wrap("Saved attendance records", detail));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(warningLabel, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel examPanel() {
        JTable table = new JTable(examModel);
        UiUtil.fitTable(table);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel wrap(String title, JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadWeekly() {
        try {
            UiUtil.clearTable(weeklyModel);
            List<TimetableEntry> slots = timetableDAO.findByBatch(student.getBatch());
            for (TimetableEntry slot : slots) {
                weeklyModel.addRow(new Object[]{
                        slot.getDay(), slot.getPeriod(),
                        slot.getSubjectCode() + " - " + slot.getSubjectName(),
                        slot.getFacultyName()
                });
            }
        } catch (Exception e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void loadDaily() {
        try {
            UiUtil.clearTable(dailyModel);
            String day = (String) dayCombo.getSelectedItem();
            List<TimetableEntry> slots = timetableDAO.findByBatchAndDay(student.getBatch(), day);
            if (slots.isEmpty()) {
                dailyModel.addRow(new Object[]{"-", "No classes on " + day, "-"});
                return;
            }
            for (TimetableEntry slot : slots) {
                dailyModel.addRow(new Object[]{
                        slot.getPeriod(),
                        slot.getSubjectCode() + " - " + slot.getSubjectName(),
                        slot.getFacultyName()
                });
            }
        } catch (Exception e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void loadAttendance() {
        try {
            UiUtil.clearTable(summaryModel);
            UiUtil.clearTable(detailModel);
            List<AttendanceSummary> summaries = attendanceDAO.summariesForStudent(student.getStudentId());
            boolean anyShortage = false;
            boolean anyRecord = false;
            for (AttendanceSummary row : summaries) {
                String status;
                if (!row.hasRecords()) {
                    status = "No attendance recorded yet";
                } else if (row.isShortage()) {
                    status = "WARNING: below 75%";
                    anyShortage = true;
                    anyRecord = true;
                } else {
                    status = "OK";
                    anyRecord = true;
                }
                summaryModel.addRow(new Object[]{
                        row.getSubjectCode() + " - " + row.getSubjectName(),
                        row.getPresentCount(),
                        row.getTotalCount(),
                        row.hasRecords() ? String.format("%.1f", row.getPercentage()) : "-",
                        row.extraClassesFor75(),
                        status
                });
            }
            if (anyShortage) {
                warningLabel.setText("Warning: attendance is below 75% in one or more subjects. Extra consecutive classes needed are shown in the table.");
            } else if (!anyRecord) {
                warningLabel.setText("No attendance has been recorded yet.");
            } else {
                warningLabel.setText("Attendance is at or above 75% in recorded subjects.");
                warningLabel.setForeground(new Color(20, 110, 50));
            }

            for (AttendanceRecord rec : attendanceDAO.findByStudent(student.getStudentId())) {
                detailModel.addRow(new Object[]{
                        rec.getSessionDate(), rec.getPeriod(), rec.getSubjectCode(), rec.getStatus()
                });
            }
        } catch (Exception e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void loadExams() {
        try {
            UiUtil.clearTable(examModel);
            for (Exam exam : examDAO.findByBatch(student.getBatch())) {
                examModel.addRow(new Object[]{
                        exam.getSubjectCode() + " - " + exam.getSubjectName(),
                        exam.getBatch(),
                        exam.getExamDate()
                });
            }
        } catch (Exception e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private static class GridTwo extends java.awt.GridLayout {
        private GridTwo() {
            super(2, 1, 8, 8);
        }
    }
}
