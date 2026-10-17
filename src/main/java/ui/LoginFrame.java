package ui;

import database.UserDAO;
import model.Admin;
import model.Faculty;
import model.Student;
import model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("Academic Timetable & Attendance System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 320);
        setLocationRelativeTo(null);

        JLabel heading = new JLabel("College Login", JLabel.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(Color.WHITE);
        heading.setOpaque(true);
        heading.setBackground(new Color(20, 55, 95));
        heading.setBorder(BorderFactory.createEmptyBorder(18, 10, 18, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Username"), gbc);
        gbc.gridx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Password"), gbc);
        gbc.gridx = 1;
        form.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());
        getRootPane().setDefaultButton(loginButton);

        gbc.gridx = 1;
        gbc.gridy = 2;
        form.add(loginButton, gbc);

        JLabel hint = new JLabel("Roles: Admin / Faculty / Student   |   Sample: admin / admin123", JLabel.CENTER);
        hint.setBorder(BorderFactory.createEmptyBorder(0, 10, 12, 10));

        add(heading, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(hint, BorderLayout.SOUTH);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if (username.isBlank() || password.isBlank()) {
            UiUtil.error(this, "Enter username and password.");
            return;
        }
        try {
            User user = userDAO.login(username, password);
            if (user == null) {
                UiUtil.error(this, "Invalid username or password.");
                return;
            }
            openDashboard(user);
            dispose();
        } catch (Exception ex) {
            UiUtil.error(this, "Login failed: " + ex.getMessage());
        }
    }

    // Polymorphism: User reference, different dashboard based on actual type
    private void openDashboard(User user) {
        if (user instanceof Admin admin) {
            new AdminDashboard(admin).setVisible(true);
        } else if (user instanceof Faculty faculty) {
            new FacultyDashboard(faculty).setVisible(true);
        } else if (user instanceof Student student) {
            new StudentDashboard(student).setVisible(true);
        }
    }
}
