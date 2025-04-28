import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdminDashboard extends JFrame {

    // Constructor -> Clear
    public AdminDashboard() {
        setTitle("Admin Dashboard - Tabbed Version");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane adminTabs = new JTabbedPane();

        adminTabs.addTab("Manage Librarians", createManageLibrariansTab());
        adminTabs.addTab("View Reports", createReportsTab());
        adminTabs.addTab("Fine Management", createFineManagementTab());
        adminTabs.addTab("User Account Management", createUserAccountsTab());
        adminTabs.addTab("Logout", createLogoutTab());

        add(adminTabs);
        setVisible(true);
    }

    // Manage Librarians
    private JPanel createManageLibrariansTab() {
        JTabbedPane librarianTabs = new JTabbedPane();

        // Add Librarian Panel
        JPanel addLibrarianPanel = new JPanel(new GridBagLayout());
        addLibrarianPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField nameField = new JTextField(20);
        JTextField idField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField passwordField = new JTextField(20);
        JButton addButton = new JButton("Add Librarian");

        gbc.gridx = 0; gbc.gridy = 0;
        addLibrarianPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        addLibrarianPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        addLibrarianPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        addLibrarianPanel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        addLibrarianPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        addLibrarianPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        addLibrarianPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        addLibrarianPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        addLibrarianPanel.add(addButton, gbc);

        // View Librarians Panel
        JPanel viewLibrariansPanel = new JPanel(new BorderLayout());
        DefaultTableModel librarianModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Action"}, 0);
        JTable librarianTable = new JTable(librarianModel);
        JScrollPane scrollPane = new JScrollPane(librarianTable);
        JButton deleteButton = new JButton("Delete Selected Librarian");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(deleteButton);

        viewLibrariansPanel.add(scrollPane, BorderLayout.CENTER);
        viewLibrariansPanel.add(buttonPanel, BorderLayout.SOUTH);

        librarianTabs.addTab("Add Librarian", addLibrarianPanel);
        librarianTabs.addTab("View/Delete Librarians", viewLibrariansPanel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(librarianTabs, BorderLayout.CENTER);

        // Backend functionality for buttons
        addButton.addActionListener(e -> {
            String name = nameField.getText();
            String id = idField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            addLibrarian(name, id, email, password);
            loadLibrarians(librarianModel);
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = librarianTable.getSelectedRow();
            if (selectedRow != -1) {
                String librarianId = (String) librarianModel.getValueAt(selectedRow, 0);
                deleteLibrarian(librarianId);
                loadLibrarians(librarianModel);
            }
        });

        // Load librarians initially
        loadLibrarians(librarianModel);

        return panel;
    }

    // View Reports
    private JPanel createReportsTab() {
        JTabbedPane reportsTabs = new JTabbedPane();

        // Available Books
        JPanel availableBooksPanel = new JPanel(new BorderLayout());
        DefaultTableModel availableModel = new DefaultTableModel(new String[]{"Book ID", "Title", "Author"}, 0);
        JTable availableBooksTable = new JTable(availableModel);
        availableBooksPanel.add(new JScrollPane(availableBooksTable), BorderLayout.CENTER);

        // Borrowed Books
        JPanel borrowedBooksPanel = new JPanel(new BorderLayout());
        DefaultTableModel borrowedModel = new DefaultTableModel(new String[]{"Book ID", "Title", "Borrower"}, 0);
        JTable borrowedBooksTable = new JTable(borrowedModel);
        borrowedBooksPanel.add(new JScrollPane(borrowedBooksTable), BorderLayout.CENTER);

        // Fine Reports
        JTabbedPane fineReportsTabs = new JTabbedPane();

        JPanel individualFinePanel = new JPanel(new BorderLayout());
        DefaultTableModel individualFineModel = new DefaultTableModel(new String[]{"Student ID", "Name", "Total Fine"}, 0);
        JTable individualFineTable = new JTable(individualFineModel);
        individualFinePanel.add(new JScrollPane(individualFineTable), BorderLayout.CENTER);

        JPanel monthlyFinePanel = new JPanel(new BorderLayout());
        DefaultTableModel monthlyFineModel = new DefaultTableModel(new String[]{"Month", "Total Fine"}, 0);
        JTable monthlyFineTable = new JTable(monthlyFineModel);
        monthlyFinePanel.add(new JScrollPane(monthlyFineTable), BorderLayout.CENTER);

        fineReportsTabs.addTab("Individual Fine Report", individualFinePanel);
        fineReportsTabs.addTab("Monthly Fine Report", monthlyFinePanel);

        reportsTabs.addTab("Available Books", availableBooksPanel);
        reportsTabs.addTab("Borrowed Books", borrowedBooksPanel);
        reportsTabs.addTab("Fine Reports", fineReportsTabs);

        // Load data
        loadAvailableBooks(availableModel);
        loadBorrowedBooks(borrowedModel);
        loadIndividualFineReports(individualFineModel);
        loadMonthlyFineReports(monthlyFineModel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(reportsTabs, BorderLayout.CENTER);
        return panel;
    }

    // Fine Management
    private JPanel createFineManagementTab() {
        JPanel fineManagementPanel = new JPanel(new BorderLayout());

        DefaultTableModel fineModel = new DefaultTableModel(new String[]{"Student ID", "Book ID", "Fine Amount"}, 0);
        JTable fineTable = new JTable(fineModel);

        JButton editFineButton = new JButton("Edit Fine");
        JButton waiveFineButton = new JButton("Waive Off Fine");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editFineButton);
        buttonPanel.add(waiveFineButton);

        fineManagementPanel.add(new JScrollPane(fineTable), BorderLayout.CENTER);
        fineManagementPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Load data
        loadFines(fineModel);

        // Buttons
        editFineButton.addActionListener(e -> {
            int selectedRow = fineTable.getSelectedRow();
            if (selectedRow != -1) {
                String studentId = (String) fineModel.getValueAt(selectedRow, 0);
                String bookId = (String) fineModel.getValueAt(selectedRow, 1);
                String fineAmountStr = JOptionPane.showInputDialog(this, "Enter new fine amount:");
                if (fineAmountStr != null) {
                    double fineAmount = Double.parseDouble(fineAmountStr);
                    editFine(studentId, bookId, fineAmount);
                    loadFines(fineModel);
                }
            }
        });

        waiveFineButton.addActionListener(e -> {
            int selectedRow = fineTable.getSelectedRow();
            if (selectedRow != -1) {
                String studentId = (String) fineModel.getValueAt(selectedRow, 0);
                String bookId = (String) fineModel.getValueAt(selectedRow, 1);
                waiveFine(studentId, bookId);
                loadFines(fineModel);
            }
        });

        return fineManagementPanel;
    }

    //  User Account Management
    private JPanel createUserAccountsTab() {
        JPanel userAccountsPanel = new JPanel(new BorderLayout());

        DefaultTableModel userModel = new DefaultTableModel(new String[]{"User ID", "Username", "Role", "Status"}, 0);
        JTable userTable = new JTable(userModel);

        JButton activateButton = new JButton("Activate Account");
        JButton deactivateButton = new JButton("Deactivate Account");
        JButton resetPasswordButton = new JButton("Reset Password");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(activateButton);
        buttonPanel.add(deactivateButton);
        buttonPanel.add(resetPasswordButton);

        userAccountsPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        userAccountsPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Load data
        loadUsers(userModel);

        // Buttons
        activateButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow != -1) {
                String userId = (String) userModel.getValueAt(selectedRow, 0);
                toggleUserStatus(userId, true);
                loadUsers(userModel);
            }
        });

        deactivateButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow != -1) {
                String userId = (String) userModel.getValueAt(selectedRow, 0);
                toggleUserStatus(userId, false);
                loadUsers(userModel);
            }
        });

        resetPasswordButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow != -1) {
                String userId = (String) userModel.getValueAt(selectedRow, 0);
                String newPassword = JOptionPane.showInputDialog(this, "Enter new password:");
                if (newPassword != null) {
                    resetUserPassword(userId, newPassword);
                    JOptionPane.showMessageDialog(this, "Password reset successfully.");
                }
            }
        });

        return userAccountsPanel;
    }

    //  Logout
    private JPanel createLogoutTab() {
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 100));
        JButton logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(200, 50));
        logoutPanel.add(logoutButton);
        return logoutPanel;
    }




    // Backend Method Stubs

    private void addLibrarian(String name, String id, String email, String password) {
        // Code to insert librarian in database
    }

    private void loadLibrarians(DefaultTableModel model) {
        // Code to load librarians from database into table
    }

    private void deleteLibrarian(String librarianId) {
        // Code to delete librarian from database
    }

    private void loadAvailableBooks(DefaultTableModel model) {
        // Load available books
    }

    private void loadBorrowedBooks(DefaultTableModel model) {
        // Load borrowed books
    }

    private void loadIndividualFineReports(DefaultTableModel model) {
        // Load individual fine reports
    }

    private void loadMonthlyFineReports(DefaultTableModel model) {
        // Load monthly fine reports
    }

    private void loadFines(DefaultTableModel model) {
        // Load fines
    }

    private void editFine(String studentId, String bookId, double fineAmount) {
        // Edit fine amount
    }

    private void waiveFine(String studentId, String bookId) {
        // Waive off fine
    }

    private void loadUsers(DefaultTableModel model) {
        // Load user accounts
    }

    private void toggleUserStatus(String userId, boolean activate) {
        // Activate or deactivate user
    }

    private void resetUserPassword(String userId, String newPassword) {
        // Reset password
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminDashboard::new);
    }
}
