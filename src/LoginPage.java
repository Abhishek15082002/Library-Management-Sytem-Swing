import javax.swing.*;
import java.awt.*;
//import AdminDashbord;


public class LoginPage {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}

class LoginFrame {
    JFrame frame = new JFrame();
    private JComboBox<String> userTypeDropdown;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton forgotButton; // New button
    private JLabel statusLabel;

    public LoginFrame() {
        frame.setTitle("Multi-User Login");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // Panel setup
        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // Dropdown
        userTypeDropdown = new JComboBox<>(new String[]{"User", "Librarian", "Admin"});
        panel.add(new JLabel("Select User Type:"));
        panel.add(userTypeDropdown);

        // Username and Password fields
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        // Bottom Panel for login and forgot password
        JPanel bottomPanel = new JPanel(new FlowLayout());

        loginButton = new JButton("Login");

        loginButton.addActionListener(e -> {
            String userType = (String) userTypeDropdown.getSelectedItem(); // Get selected user type

            if ("User".equals(userType)) {

                new StudentDashboard(); // Open User page
                frame.dispose(); // Close Login page
            } else if ("Librarian".equals(userType)) {
                new LibrarianDashboard();
                frame.dispose();
            } else if ("Admin".equals(userType)) {
                new AdminDashboard();
//                System.out.print(1);
                frame.dispose();
            } else {
                statusLabel.setText("Please select a valid user type!");
            }
        });





        forgotButton = new JButton("Forgot Password?");
        forgotButton.setBorderPainted(false);
        forgotButton.setOpaque(false);
        forgotButton.setBackground(Color.WHITE);
        forgotButton.setForeground(Color.BLUE);
        forgotButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotButton.setFocusPainted(false);
        forgotButton.setContentAreaFilled(false);

        bottomPanel.add(loginButton);
        bottomPanel.add(forgotButton);

        statusLabel = new JLabel("", SwingConstants.CENTER);

        frame.add(panel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.add(statusLabel, BorderLayout.NORTH);

        frame.setVisible(true);
    }
}