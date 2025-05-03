package library.frontend;

import library.DatabaseConnection;

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SignUpFrame extends JFrame implements ActionListener {

    private JTextField nameField, usernameField, emailField;
    private JPasswordField passwordField;
    private JComboBox<String> securityQuestionBox;
    private JTextField answerField;
    private JButton signUpButton;
    private JLabel statusLabel;

    public SignUpFrame() {
        setTitle("Smart Library - Sign Up");
        setSize(450, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Status label
        statusLabel = new JLabel("Create a new account", SwingConstants.CENTER);
        statusLabel.setForeground(new Color(0, 102, 204));
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(statusLabel, BorderLayout.NORTH);

        // Input panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        // Full Name
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++;
        nameField = new JTextField();
        formPanel.add(nameField, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++;
        usernameField = new JTextField();
        formPanel.add(usernameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++;
        emailField = new JTextField();
        formPanel.add(emailField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++;
        passwordField = new JPasswordField();
        formPanel.add(passwordField, gbc);

        // Security Question
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Security Question:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++;
        securityQuestionBox = new JComboBox<>(new String[] {
                "What is your pet's name?",
                "What is your mother's maiden name?",
                "What is your favourite book?",
                "What was the name of your first school?"
        });
        formPanel.add(securityQuestionBox, gbc);

        // Answer
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Answer:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++;
        answerField = new JTextField();
        formPanel.add(answerField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Sign Up Button
        JPanel bottomPanel = new JPanel();
        signUpButton = new JButton("Sign Up");
        signUpButton.setPreferredSize(new Dimension(120, 35));
        signUpButton.addActionListener(this);
        bottomPanel.add(signUpButton);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == signUpButton) {
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String question = (String) securityQuestionBox.getSelectedItem();
            String answer = answerField.getText().trim();

            if (name.isEmpty() || username.isEmpty() || email.isEmpty() ||
                    password.isEmpty() || answer.isEmpty()) {
                showStatus("Please fill all fields.", true);
                return;
            }

            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            String passwordRegex = "^.{6,}$";

            if (!email.matches(emailRegex)) {
                showStatus("Invalid email format.", true);
                return;
            }

            if (!password.matches(passwordRegex)) {
                showStatus("Password must be at least 9 characters.", true);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String checkQuery = "SELECT * FROM Users WHERE username = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    showStatus("Username already exists.", true);
                    return;
                }

                String insertUser = "INSERT INTO Users (username, password, role, status, email, security_question, security_answer) VALUES (?, ?, ?, 'Active', ?, ?, ?)";
                PreparedStatement userStmt = conn.prepareStatement(insertUser);
                userStmt.setString(1, username);
                userStmt.setString(2, password);
                userStmt.setString(3, "student");
                userStmt.setString(4, email);
                userStmt.setString(5, question);
                userStmt.setString(6, answer);
                userStmt.executeUpdate();

                String newIdQuery = "SELECT COUNT(*) FROM Students";
                PreparedStatement countStmt = conn.prepareStatement(newIdQuery);
                ResultSet countRs = countStmt.executeQuery();
                countRs.next();
                int count = countRs.getInt(1) + 1;
                String studentId = String.format("S%03d", count);

                String insertStudent = "INSERT INTO Students (student_id, username, name) VALUES (?, ?, ?)";
                PreparedStatement studentStmt = conn.prepareStatement(insertStudent);
                studentStmt.setString(1, studentId);
                studentStmt.setString(2, username);
                studentStmt.setString(3, name);
                studentStmt.executeUpdate();

                showStatus("Account created successfully!", false);

            } catch (SQLException ex) {
                ex.printStackTrace();
                showStatus("Database error occurred.", true);
            }
        }

    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Color.RED : new Color(0, 153, 51));
    }
}
