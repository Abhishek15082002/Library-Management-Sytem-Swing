package library.frontend;

import library.DatabaseConnection;

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class SignUpFrame extends JFrame implements ActionListener {

    private JTextField nameField, usernameField, emailField;
    private JPasswordField passwordField;
    private JComboBox<String> securityQuestionBox;
    private JTextField answerField;
    private JButton signUpButton;
    private JLabel statusLabel;

    private static class RoundedBorder implements javax.swing.border.Border {
        private int radius;

        RoundedBorder(int radius) {
            this.radius = radius;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius + 1, this.radius + 1, this.radius + 2, this.radius);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }
    public SignUpFrame() {
        setTitle("Smart Library - Sign Up");
        setSize(450, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) { // Added mainPanel for background
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(220, 230, 240);
                Color color2 = new Color(245, 245, 245);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        add(mainPanel);

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel logoLabel = new JLabel();
        logoPanel.add(logoLabel);
        mainPanel.add(logoPanel, BorderLayout.NORTH);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusLabel = new JLabel("Create a new account", SwingConstants.CENTER);
        statusLabel.setForeground(Color.DARK_GRAY); // Consistent color
        statusPanel.add(statusLabel);
        mainPanel.add(statusPanel, BorderLayout.NORTH);

        // Input panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40)); // Consistent padding
        formPanel.setOpaque(false); // Make form panel transparent
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;

        // Full Name
        gbc.gridx = 0; gbc.gridy = y;
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++;
        gbc.weightx = 1.0;
        nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(150, 30));
        nameField.setBackground(Color.WHITE);
        formPanel.add(nameField, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy = y;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        formPanel.add(usernameLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++;
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(150, 30));
        usernameField.setBackground(Color.WHITE);
        formPanel.add(usernameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = y;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        formPanel.add(emailLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++;
        emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(150, 30));
        emailField.setBackground(Color.WHITE);
        formPanel.add(emailField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = y;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++;
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(150, 30));
        passwordField.setBackground(Color.WHITE);
        formPanel.add(passwordField, gbc);

        // Security Question
        gbc.gridx = 0; gbc.gridy = y;
        JLabel questionLabel = new JLabel("Security Question:");
        questionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        formPanel.add(questionLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++;
        securityQuestionBox = new JComboBox<>(new String[] {
                "What is your pet's name?",
                "What is your mother's maiden name?",
                "What is your favorite book?",
                "What was the name of your first school?"
        });
        securityQuestionBox.setPreferredSize(new Dimension(150, 30));
        securityQuestionBox.setBackground(Color.WHITE);
        formPanel.add(securityQuestionBox, gbc);

        // Answer
        gbc.gridx = 0; gbc.gridy = y;
        JLabel answerLabel = new JLabel("Answer:");
        answerLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        formPanel.add(answerLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++;
        answerField = new JTextField();
        answerField.setPreferredSize(new Dimension(150, 30));
        answerField.setBackground(Color.WHITE);
        formPanel.add(answerField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Sign Up Button
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        signUpButton = new JButton("Sign Up");
        signUpButton.setPreferredSize(new Dimension(120, 35));
        signUpButton.addActionListener(this);
        signUpButton.setBackground(new Color(70, 130, 180));
        signUpButton.setForeground(Color.WHITE);
        signUpButton.setFocusPainted(false);
        signUpButton.setBorder(new RoundedBorder(10));
        bottomPanel.add(signUpButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

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