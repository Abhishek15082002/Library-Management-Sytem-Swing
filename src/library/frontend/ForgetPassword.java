package library.frontend;

import javax.swing.*;
import java.awt.*;

public class ForgetPassword extends JFrame {

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
        questionDropdown = new JComboBox<>(new String[] {
                "What is your pet's name?",
                "What is your mother's maiden name?",
                "What is your favorite book?",
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
        mainPanel.add(verifyButton, gbc);

        gbc.gridy = 4;
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.DARK_GRAY);
        mainPanel.add(statusLabel, gbc);

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ForgetPassword::new);
    }
}
