package library.frontend;

import library.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class ForgetPassword extends JFrame implements ActionListener {

    private JTextField usernameField;
    private JComboBox<String> questionDropdown;
    private JTextField answerField;
    private JButton verifyButton;
    private JLabel statusLabel;
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

    public ForgetPassword() {
        setTitle("Forgot Password - Security Verification");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) { // Added mainPanel for consistent design
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
        statusLabel = new JLabel("Enter details to verify your account", SwingConstants.CENTER);
        statusLabel.setForeground(Color.DARK_GRAY);  // Consistent color
        statusPanel.add(statusLabel);
        mainPanel.add(statusPanel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40)); // Consistent padding
        inputPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;

        gbc.gridx = 0; gbc.gridy = y;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputPanel.add(usernameLabel, gbc);
        gbc.gridx = 1;gbc.gridy = y++;gbc.weightx = 1.0;
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(150, 30));
        usernameField.setBackground(Color.WHITE);
        inputPanel.add(usernameField, gbc);

        gbc.gridx = 0;gbc.gridy = y;
        JLabel questionLabel = new JLabel("Security Question:");
        questionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputPanel.add(questionLabel, gbc);
        gbc.gridx = 1;gbc.gridy = y++;
        questionDropdown = new JComboBox<>(new String[]{
                "What is your pet's name?",
                "What is your mother's maiden name?",
                "What is your favorite book?",
                "What is your birthplace?"
        });
        questionDropdown.setPreferredSize(new Dimension(150, 30));
        questionDropdown.setBackground(Color.WHITE);
        inputPanel.add(questionDropdown, gbc);

        gbc.gridx = 0; gbc.gridy = y;
        JLabel answerLabel = new JLabel("Answer:");
        answerLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputPanel.add(answerLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++;
        answerField = new JTextField();
        answerField.setPreferredSize(new Dimension(150, 30));
        answerField.setBackground(Color.WHITE);
        inputPanel.add(answerField, gbc);

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5)); // Consistent layout
        bottomPanel.setOpaque(false);
        verifyButton = new JButton("Verify");
        verifyButton.setPreferredSize(new Dimension(100, 35));
        verifyButton.addActionListener(this);
        verifyButton.setBackground(new Color(70, 130, 180));
        verifyButton.setForeground(Color.WHITE);
        verifyButton.setFocusPainted(false);
        verifyButton.setBorder(new RoundedBorder(BORDER_RADIUS));
        bottomPanel.add(verifyButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String username = usernameField.getText().trim();
        String question = (String) questionDropdown.getSelectedItem();
        String answer = answerField.getText().trim();

        if (username.isEmpty() || answer.isEmpty()) {
            showStatus("Please fill all fields.", true);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM Users WHERE username = ? AND security_question = ? AND security_answer = ?";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, username);
                pst.setString(2, question);
                pst.setString(3, answer);

                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    showStatus("Verification successful. Set new password.", false);
                    showResetPasswordDialog(username);
                } else {
                    showStatus("Incorrect details. Try again.", true);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showStatus("Database error occurred.", true);
        }
    }

    private void showResetPasswordDialog(String username) {
        JPasswordField newPassField = new JPasswordField();
        int result = JOptionPane.showConfirmDialog(this, newPassField, "Enter new password (min 8 chars):", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String newPassword = new String(newPassField.getPassword());

            if (newPassword.length() < 8) {
                JOptionPane.showMessageDialog(this, "Password must be at least 8 characters.", "Invalid Password", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String updateQuery = "UPDATE Users SET password = ? WHERE username = ?";
                try (PreparedStatement pst = conn.prepareStatement(updateQuery)) {
                    pst.setString(1, newPassword);
                    pst.setString(2, username);
                    pst.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Password updated successfully.");
                    dispose(); // close the window
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to update password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Color.RED : new Color(0, 153, 0));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ForgetPassword::new);
    }
}