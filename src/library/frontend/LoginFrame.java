package library.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import library.DatabaseConnection;
import library.UserSession;

public class LoginFrame extends JFrame implements ActionListener {

    private JComboBox<String> userTypeDropdown;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton forgotButton;
    private JButton signUpButton;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("Smart Library - Login");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusLabel = new JLabel("Please select role and enter credentials.", SwingConstants.CENTER);
        statusLabel.setForeground(Color.DARK_GRAY);
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Select User Type:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        userTypeDropdown = new JComboBox<>(new String[]{"Student", "Librarian", "Admin"});
        inputPanel.add(userTypeDropdown, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0.0;
        inputPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        usernameField = new JTextField();
        inputPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0.0;
        inputPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        passwordField = new JPasswordField();
        passwordField.addActionListener(this);
        inputPanel.add(passwordField, gbc);

        add(inputPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 30));
        loginButton.addActionListener(this);

        forgotButton = new JButton("Forgot Password?");
        forgotButton.setBorderPainted(false);
        forgotButton.setOpaque(false);
        forgotButton.setBackground(UIManager.getColor("Label.background"));
        forgotButton.setForeground(Color.BLUE);
        forgotButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotButton.setFocusPainted(false);
        forgotButton.addActionListener(this);

        signUpButton = new JButton("New here? Sign up");
        signUpButton.setBorderPainted(false);
        signUpButton.setOpaque(false);
        signUpButton.setBackground(UIManager.getColor("Label.background"));
        signUpButton.setForeground(Color.BLUE);
        signUpButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpButton.setFocusPainted(false);
        signUpButton.addActionListener(this);

        bottomPanel.add(loginButton);
        bottomPanel.add(forgotButton);
        bottomPanel.add(signUpButton);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton || e.getSource() == passwordField) {
            attemptLogin();
        } else if (e.getSource() == forgotButton) {
            new ForgetPassword();
        } else if (e.getSource() == signUpButton) {
            new SignUpFrame(); // Open sign up window (make sure this class exists)
        }
    }

    private void attemptLogin() {
        String selectedRole = (String) userTypeDropdown.getSelectedItem();
        String username = usernameField.getText().trim();
        String plainPassword = new String(passwordField.getPassword());

        if (username.isEmpty() || plainPassword.isEmpty()) {
            showStatus("Username and Password cannot be empty.", true);
            return;
        }

        loginButton.setEnabled(false);
        showStatus("Authenticating...", false);

        String[] authResult = authenticateUser(username, plainPassword, selectedRole);
        loginButton.setEnabled(true);

        if (authResult != null) {
            String userId = authResult[0];
            String actualRole = authResult[1];
            try {
                UserSession.createInstance(username, actualRole, userId);
                openDashboard(actualRole);
                this.dispose();
            } catch (IllegalStateException | IllegalArgumentException sessionEx) {
                showStatus(sessionEx.getMessage(), true);
                UserSession.clearInstance();
            }
        } else {
            passwordField.setText("");
            usernameField.requestFocusInWindow();
        }
    }

    private String[] authenticateUser(String username, String plainPassword, String selectedRole) {
        String sql = "SELECT password, role, status FROM Users WHERE username = ?";
        String userId = username;
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                showStatus("Cannot connect to database.", true);
                return null;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String storedPasswordOrHash = rs.getString("password");
                        String actualRole = rs.getString("role");
                        String status = rs.getString("status");

                        if (!actualRole.equalsIgnoreCase(selectedRole)) {
                            showStatus("Role mismatch.", true);
                            return null;
                        }
                        boolean passwordMatch = plainPassword.equals(storedPasswordOrHash);
                        if (!passwordMatch) {
                            showStatus("Invalid password.", true);
                            return null;
                        }
                        if (!"Active".equalsIgnoreCase(status)) {
                            showStatus("Account inactive.", true);
                            return null;
                        }
                        userId = fetchSpecificUserId(conn, username, actualRole);
                        return new String[]{userId, actualRole};
                    } else {
                        showStatus("Username not found.", true);
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            showStatus("Database error.", true);
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            showStatus("Login error.", true);
            e.printStackTrace();
            return null;
        }
    }

    private String fetchSpecificUserId(Connection conn, String username, String role) throws SQLException {
        String specificId = username;
        String query = null;
        String idColumn = null;
        if ("Student".equalsIgnoreCase(role)) {
            query = "SELECT student_id FROM Students WHERE username = ?";
            idColumn = "student_id";
        } else if ("Librarian".equalsIgnoreCase(role)) {
            query = "SELECT librarian_id FROM Librarians WHERE username = ?";
            idColumn = "librarian_id";
        } else {
            return username;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    specificId = rs.getString(idColumn);
                } else {
                    System.err.println("Warning: Specific ID not found for " + role + " " + username);
                }
            }
        }
        return specificId;
    }

    private void openDashboard(String role) {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame dashboard = null;
                if ("Admin".equalsIgnoreCase(role)) {
                    dashboard = new AdminDashboard();
                } else if ("Librarian".equalsIgnoreCase(role)) {
                    dashboard = new LibrarianDashboard();
                } else if ("Student".equalsIgnoreCase(role)) {
                    dashboard = new StudentDashboard();
                } else {
                    showStatus("Unknown role: " + role, true);
                    return;
                }
                if (dashboard != null) {
                    dashboard.setVisible(true);
                }
            } catch (Exception ex) {
                showStatus("Error loading dashboard.", true);
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading dashboard.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showStatus(final String message, final boolean isError) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
            statusLabel.setForeground(isError ? Color.RED : new Color(0, 100, 0));
        });
    }
}
