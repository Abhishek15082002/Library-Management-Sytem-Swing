package library.frontend;

import library.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ForgetPassword extends JFrame implements ActionListener {

    private JTextField usernameField;
    private JComboBox<String> questionDropdown;
    private JTextField answerField;
    private JButton verifyButton;
    private JLabel statusLabel;

    public ForgetPassword() {
        setTitle("Forgot Password - Security Verification");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField();
        mainPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Security Question:"), gbc);
        gbc.gridx = 1;
        questionDropdown = new JComboBox<>(new String[]{
                "What is your pet's name?",
                "What is your mother's maiden name?",
                "What is your favourite book?",
                "What is your birthplace?"
        });
        mainPanel.add(questionDropdown, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Answer:"), gbc);
        gbc.gridx = 1;
        answerField = new JTextField();
        mainPanel.add(answerField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        verifyButton = new JButton("Verify");
        verifyButton.addActionListener(this);
        mainPanel.add(verifyButton, gbc);

        gbc.gridy = 4;
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.DARK_GRAY);
        mainPanel.add(statusLabel, gbc);

        add(mainPanel, BorderLayout.CENTER);
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
