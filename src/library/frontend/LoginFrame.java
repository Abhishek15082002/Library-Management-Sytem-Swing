package library.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;

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
    private JPanel inputPanel; // Declare inputPanel as a class member
    private static final int BORDER_RADIUS = 10;

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

    public LoginFrame() {
        setTitle("Smart Library - Login");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color = new Color(240, 240, 240); // Light gray
                g2d.setColor(color);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        add(mainPanel);

        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                try {
                    BufferedImage img = ImageIO.read(new File("src\\assets\\Books app icon.jpeg")); // Use your image path
                    // Keep original image size or scale as needed
                    g2d.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                } catch (IOException e) {
                    System.err.println("Error loading image: " + e.getMessage());
                    // Handle error, e.g., draw a placeholder
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(Color.BLACK);
                    g2d.drawString("Image Not Found", 10, 20);
                }
            }
        };
        imagePanel.setPreferredSize(new Dimension(400, getHeight())); // Half width
        mainPanel.add(imagePanel, BorderLayout.WEST);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel logoLabel = new JLabel();
        try {
            BufferedImage img = ImageIO.read(new File("src\\assets\\Books app icon.jpeg"));
            ImageIcon imageIcon = new ImageIcon(img.getScaledInstance(150, 100, Image.SCALE_SMOOTH)); // Adjust width and height as needed
            logoLabel.setIcon(imageIcon);
        } catch (IOException e) {
            System.err.println("Error loading logo image: " + e.getMessage());
            logoLabel.setText("Library Logo"); // Fallback text if image fails to load
            logoLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        }
        logoPanel.add(logoLabel);
        logoPanel.setOpaque(false);
        contentPanel.add(logoPanel, BorderLayout.NORTH);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusLabel = new JLabel("Please select role and enter credentials.", SwingConstants.CENTER);
        statusLabel.setForeground(Color.DARK_GRAY);
        statusPanel.setOpaque(false);
        add(statusPanel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        inputPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        JLabel userTypeLabel = new JLabel("User Type:"); // More descriptive labels
        userTypeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputPanel.add(userTypeLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        userTypeDropdown = new JComboBox<>(new String[]{"Student", "Librarian", "Admin"});
        userTypeDropdown.setPreferredSize(new Dimension(200, 30));
        userTypeDropdown.setBackground(Color.WHITE);
        inputPanel.add(userTypeDropdown, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputPanel.add(usernameLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(200, 30));
        usernameField.setBackground(Color.WHITE);
        inputPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputPanel.add(passwordLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 30));
        passwordField.setBackground(Color.WHITE);
        passwordField.addActionListener(this);
        inputPanel.add(passwordField, gbc);

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        bottomPanel.setOpaque(false);
        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(120, 35));
        loginButton.addActionListener(this);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 16));

        loginButton.setBackground(new Color(70, 130, 180)); // Light Blue
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(new RoundedBorder(BORDER_RADIUS));

        forgotButton = new JButton("Forgot Password?");
        forgotButton.setBorderPainted(false);
        forgotButton.setOpaque(false);
        forgotButton.setBackground(new Color(221, 160, 221)); // Plum
        forgotButton.setForeground(new Color(75, 0, 130));
        forgotButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotButton.setFocusPainted(false);
        forgotButton.addActionListener(this);
        forgotButton.setBorder(new RoundedBorder(10));
        forgotButton.setFont(new Font("SansSerif", Font.PLAIN, 12));

        signUpButton = new JButton("New here? Sign up");
        signUpButton.setBorderPainted(false);
        signUpButton.setOpaque(false);
        signUpButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpButton.setFocusPainted(false);
        signUpButton.addActionListener(this);
        signUpButton.setFont(new Font("SansSerif", Font.PLAIN, 12));

        signUpButton.setBorder(new RoundedBorder(10));
        signUpButton.setBackground(new Color(221, 160, 221)); // Plum
        signUpButton.setForeground(new Color(75, 0, 130));

        bottomPanel.add(loginButton);
        bottomPanel.add(forgotButton);
        bottomPanel.add(signUpButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

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
                        String storedPassword = rs.getString("password");
                        String actualRole = rs.getString("role");
                        String status = rs.getString("status");

                        if (!actualRole.equalsIgnoreCase(selectedRole)) {
                            showStatus("Role mismatch.", true);
                            return null;
                        }
                        boolean passwordMatch = plainPassword.equals(storedPassword);
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