package library.frontend;

import javax.swing.*;
import java.awt.*; // Import AWT package for Layouts, Dimension, etc.
import java.awt.event.*; // Keep if needed for listeners

// Import other necessary classes
import library.UserSession;
/**
 * Placeholder for the Admin Dashboard GUI.
 */
public class AdminDashboard extends JFrame implements ActionListener { // Must be public

    // ... (rest of the placeholder code) ...

    private JTabbedPane adminTabs;
    private JButton logoutButton;
    private UserSession session;

     public AdminDashboard() {
        session = UserSession.getInstance(); // UserSession from library package
        if (session == null || !"Admin".equalsIgnoreCase(session.getRole())) {
            JOptionPane.showMessageDialog(null, "Access Denied.", "Session Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            SwingUtilities.invokeLater(LoginFrame::new); // LoginFrame from this package
            return;
        }
        // ... rest of constructor ...
         setTitle("Admin Dashboard - " + session.getUsername());
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { performLogout(); }
        });
        adminTabs = new JTabbedPane();
        adminTabs.addTab("Manage Librarians", createPlaceholderPanel("Librarian Management"));
        adminTabs.addTab("View Reports", createPlaceholderPanel("System Reports"));
        adminTabs.addTab("Fine Management", createPlaceholderPanel("Fine Adjustment"));
        adminTabs.addTab("User Accounts", createPlaceholderPanel("User Account Management"));
        adminTabs.addTab("Logout", createLogoutPanel());
        add(adminTabs);
    }

     private JPanel createPlaceholderPanel(String text) { /* ... as before ... */
         JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(text + " - Functionality to be implemented.");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label);
        return panel;
     }
     private JPanel createLogoutPanel() { /* ... as before ... */
         JPanel logoutPanel = new JPanel(new GridBagLayout());
        logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(150, 40));
        logoutButton.addActionListener(this);
        GridBagConstraints gbc = new GridBagConstraints();
        logoutPanel.add(logoutButton, gbc);
        return logoutPanel;
     }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == logoutButton) { performLogout(); }
    }

    private void performLogout() {
        int confirmation = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            UserSession.clearInstance(); // UserSession from library package
            this.dispose();
            SwingUtilities.invokeLater(LoginFrame::new); // LoginFrame from this package
        }
    }
}
