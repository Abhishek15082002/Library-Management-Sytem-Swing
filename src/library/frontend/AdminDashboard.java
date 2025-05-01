package library.frontend;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter; // For sorting tables
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import library.UserSession;
import library.backend.AdminService; // Import the backend service

/**
 * Admin Dashboard GUI. Provides administrative functions for managing users and librarians.
 */
public class AdminDashboard extends JFrame implements ActionListener {

    private final AdminService adminService; // Backend service
    private final UserSession session;

    // Main UI Components
    private JTabbedPane mainTabs;

    // User Management Tab Components
    private JPanel userManagementPanel;
    private JTable usersTable;
    private DefaultTableModel usersTableModel;
    private JButton activateUserButton;
    private JButton deactivateUserButton;
    private JScrollPane userScrollPane;
    private JTextField userSearchField; // Added search
    private JButton userSearchButton; // Added search

    // Librarian Management Tab Components
    private JPanel librarianManagementPanel;
    private JSplitPane librarianSplitPane; // Split add form and view table
    // Add Librarian Form
    private JPanel addLibrarianPanel;
    private JTextField libUsernameField;
    private JPasswordField libPasswordField; // Use JPasswordField
    private JTextField libNameField;
    private JTextField libEmailField;
    private JButton addLibrarianButton;
    // View Librarians Table
    private JPanel viewLibrariansPanel; // Renamed for clarity
    private JTable librariansTable;
    private DefaultTableModel librariansTableModel;
    private JButton deleteLibrarianButton;
    private JScrollPane librarianScrollPane;

    // Placeholder Panels
    private JPanel reportsPanel;
    private JPanel fineManagementPanel;

    // Logout Components
    private JButton logoutButton;


    public AdminDashboard() {
        session = UserSession.getInstance();
        if (session == null || !"Admin".equalsIgnoreCase(session.getRole())) {
            JOptionPane.showMessageDialog(null, "Access Denied: Invalid session or role.", "Session Error", JOptionPane.ERROR_MESSAGE);
            dispose(); SwingUtilities.invokeLater(LoginFrame::new);
            this.adminService = null; // Satisfy final field check
            return;
        }

        this.adminService = new AdminService(); // Initialize service

        setTitle("Admin Dashboard - " + session.getUsername());
        setSize(1100, 750);
        setMinimumSize(new Dimension(850, 600)); // Adjusted minimum size
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { performLogout(); }
        });

        mainTabs = new JTabbedPane();

        // Create and add tabs
        createUserManagementTab();
        createLibrarianManagementTab();
        createReportsTab(); // Placeholder
        createFineManagementTab(); // Placeholder

        mainTabs.addTab("User Accounts", userManagementPanel);
        mainTabs.addTab("Manage Librarians", librarianManagementPanel);
        mainTabs.addTab("View Reports", reportsPanel);
        mainTabs.addTab("Fine Management", fineManagementPanel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Admin Dashboard - " + session.getUsername());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.WEST);

        logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(120, 35));
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.addActionListener(this);
        topPanel.add(logoutButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(mainTabs, BorderLayout.CENTER);

        // Load initial data for tables
        loadAllUsers();
        loadAllLibrarians();
    }

    // --- Tab Creation Methods ---

    private void createUserManagementTab() {
        userManagementPanel = new JPanel(new BorderLayout(10, 10));
        userManagementPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Search Panel (Top) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userSearchField = new JTextField(25);
        userSearchButton = new JButton("Search Users");
        userSearchButton.addActionListener(this);
        userSearchField.addActionListener(e -> userSearchButton.doClick()); // Search on Enter
        searchPanel.add(new JLabel("Search Username/Email:"));
        searchPanel.add(userSearchField);
        searchPanel.add(userSearchButton);
        userManagementPanel.add(searchPanel, BorderLayout.NORTH);


        // --- Table setup (Center) ---
        String[] userColumns = {"Username", "Role", "Status", "Email"};
        usersTableModel = new DefaultTableModel(userColumns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        usersTable = new JTable(usersTableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.setAutoCreateRowSorter(true); // Enable sorting
        userScrollPane = new JScrollPane(usersTable);
        userManagementPanel.add(userScrollPane, BorderLayout.CENTER);

        // --- Button Panel (Bottom) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        activateUserButton = new JButton("Activate Selected");
        deactivateUserButton = new JButton("Deactivate Selected");

        activateUserButton.setToolTipText("Set the selected user's status to Active.");
        deactivateUserButton.setToolTipText("Set the selected user's status to Inactive.");

        activateUserButton.addActionListener(this);
        deactivateUserButton.addActionListener(this);

        buttonPanel.add(activateUserButton);
        buttonPanel.add(deactivateUserButton);
        userManagementPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createLibrarianManagementTab() {
        librarianManagementPanel = new JPanel(new BorderLayout());

        // --- Add Librarian Form Panel ---
        addLibrarianPanel = new JPanel(new GridBagLayout());
        addLibrarianPanel.setBorder(BorderFactory.createTitledBorder("Add New Librarian"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Adjusted insets
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int gridY = 0; // Row counter

        // Username
        gbc.gridx = 0; gbc.gridy = gridY; gbc.weightx = 0.1; addLibrarianPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY++; gbc.weightx = 0.9; libUsernameField = new JTextField(15); addLibrarianPanel.add(libUsernameField, gbc);
        // Password
        gbc.gridx = 0; gbc.gridy = gridY; gbc.weightx = 0.1; addLibrarianPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY++; gbc.weightx = 0.9; libPasswordField = new JPasswordField(15); addLibrarianPanel.add(libPasswordField, gbc);
        // Name
        gbc.gridx = 0; gbc.gridy = gridY; gbc.weightx = 0.1; addLibrarianPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY++; gbc.weightx = 0.9; libNameField = new JTextField(15); addLibrarianPanel.add(libNameField, gbc);
        // Email
        gbc.gridx = 0; gbc.gridy = gridY; gbc.weightx = 0.1; addLibrarianPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY++; gbc.weightx = 0.9; libEmailField = new JTextField(15); addLibrarianPanel.add(libEmailField, gbc);
        // Add Button
        gbc.gridx = 0; gbc.gridy = gridY++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER; gbc.weightx = 0.0;
        addLibrarianButton = new JButton("Add Librarian");
        addLibrarianButton.addActionListener(this);
        addLibrarianPanel.add(addLibrarianButton, gbc);

        // Add padding at the bottom
        gbc.gridy = gridY; gbc.weighty = 1.0; addLibrarianPanel.add(new JLabel(), gbc);


        // --- View/Delete Librarians Panel ---
        viewLibrariansPanel = new JPanel(new BorderLayout(10, 10));
        viewLibrariansPanel.setBorder(BorderFactory.createTitledBorder("Current Librarians"));

        String[] libColumns = {"Librarian ID", "Name", "Username", "Email"};
        librariansTableModel = new DefaultTableModel(libColumns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        librariansTable = new JTable(librariansTableModel);
        librariansTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        librariansTable.setAutoCreateRowSorter(true);
        librarianScrollPane = new JScrollPane(librariansTable);
        viewLibrariansPanel.add(librarianScrollPane, BorderLayout.CENTER);

        // Delete Button Panel
        JPanel deleteButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        deleteLibrarianButton = new JButton("Delete Selected Librarian");
        deleteLibrarianButton.addActionListener(this);
        deleteButtonPanel.add(deleteLibrarianButton);
        viewLibrariansPanel.add(deleteButtonPanel, BorderLayout.SOUTH);

        // --- Split Pane ---
        librarianSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, addLibrarianPanel, viewLibrariansPanel);
        librarianSplitPane.setDividerLocation(380); // Adjust divider location
        librarianSplitPane.setResizeWeight(0.35); // Adjust resize weight

        librarianManagementPanel.add(librarianSplitPane, BorderLayout.CENTER);
    }

    // Placeholders for other tabs
    // 📁 View Reports
    private void createReportsTab() {
        reportsPanel = new JPanel(new BorderLayout()); // Store in instance variable

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

        reportsPanel.add(reportsTabs, BorderLayout.CENTER);
    }


    private void createFineManagementTab() {
        fineManagementPanel = new JPanel(new BorderLayout()); // Store in instance variable

        // Table for fines
        DefaultTableModel fineModel = new DefaultTableModel(new String[]{"Student ID", "Book ID", "Fine Amount"}, 0);
        JTable fineTable = new JTable(fineModel);

        // Buttons for editing and waiving fines
        JButton editFineButton = new JButton("Edit Fine");
        JButton waiveFineButton = new JButton("Waive Off Fine");

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editFineButton);
        buttonPanel.add(waiveFineButton);

        // Add components to the fineManagementPanel
        fineManagementPanel.add(new JScrollPane(fineTable), BorderLayout.CENTER);
        fineManagementPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Action listeners for buttons
        editFineButton.addActionListener(e -> {
            // Logic for editing fine
        });

        waiveFineButton.addActionListener(e -> {
            // Logic for waiving off fine
        });

        // Load data (uncomment and implement when needed)
        // loadFines(fineModel);
    }


//    private void createReportsTab() { reportsPanel = createPlaceholderPanel("System Reports - To be implemented"); }
//    private void createFineManagementTab() { fineManagementPanel = createPlaceholderPanel("Fine Management - To be implemented"); }


    private JPanel createPlaceholderPanel(String text) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(text); label.setFont(new Font("Arial", Font.ITALIC, 16));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label); return panel;
    }

    // --- Action Listener Implementation ---
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        try {
            if (source == activateUserButton) { handleSetUserStatus(true); }
            else if (source == deactivateUserButton) { handleSetUserStatus(false); }
            else if (source == userSearchButton) { loadAllUsers(); } // Reload users with search term
            else if (source == addLibrarianButton) { handleAddLibrarian(); }
            else if (source == deleteLibrarianButton) { handleDeleteLibrarian(); }
            else if (source == logoutButton) { performLogout(); }
        } catch (Exception ex) {
            showError("An unexpected UI error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // --- UI Action Handlers ---

    private void handleSetUserStatus(boolean activate) {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) { showWarning("Please select a user from the table."); return; }
        int modelRow = usersTable.convertRowIndexToModel(selectedRow);
        String username = (String) usersTableModel.getValueAt(modelRow, 0);
        String currentStatus = (String) usersTableModel.getValueAt(modelRow, 2);

        // Prevent redundant actions or self-deactivation
        if ((activate && "Active".equalsIgnoreCase(currentStatus)) || (!activate && "Inactive".equalsIgnoreCase(currentStatus))) {
            showInfo("User '" + username + "' already has status '" + currentStatus + "'."); return;
        }
        if (!activate && username.equals(session.getUsername())) {
            showWarning("You cannot deactivate your own admin account."); return;
        }

        String action = activate ? "Activate" : "Deactivate";
        int confirm = JOptionPane.showConfirmDialog(this, action + " user '" + username + "'?", "Confirm Action", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Disable buttons during operation
        activateUserButton.setEnabled(false);
        deactivateUserButton.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() throws Exception {
                return adminService.setUserStatus(username, activate);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        showSuccess("User '" + username + "' status updated successfully.");
                        loadAllUsers(); // Refresh table
                    } else { showError("Failed to update status for user '" + username + "'."); }
                } catch (Exception e) {
                    handleLoadingError(e, "updating user status");
                } finally {
                    activateUserButton.setEnabled(true); // Re-enable buttons
                    deactivateUserButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void handleAddLibrarian() {
        String username = libUsernameField.getText().trim();
        String password = new String(libPasswordField.getPassword());
        String name = libNameField.getText().trim();
        String email = libEmailField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || email.isEmpty()) {
            showWarning("Please fill in all librarian details."); return;
        }
        if (!isValidEmail(email)) { // Basic email format check
            showWarning("Please enter a valid email address."); return;
        }

        addLibrarianButton.setEnabled(false);
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() throws Exception {
                // REMINDER: Replace plain password with hashed password logic here or in service
                return adminService.addLibrarian(username, password, name, email);
            }
            @Override protected void done() {
                try {
                    String newLibrarianId = get();
                    showSuccess("Librarian '" + name + "' added (ID: " + newLibrarianId + ")");
                    libUsernameField.setText(""); libPasswordField.setText("");
                    libNameField.setText(""); libEmailField.setText("");
                    loadAllLibrarians(); // Refresh librarian table
                    loadAllUsers(); // Also refresh user table
                } catch (Exception e) {
                    handleLoadingError(e, "adding librarian");
                } finally {
                    addLibrarianButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void handleDeleteLibrarian() {
        int selectedRow = librariansTable.getSelectedRow();
        if (selectedRow == -1) { showWarning("Please select a librarian to delete."); return; }
        int modelRow = librariansTable.convertRowIndexToModel(selectedRow);
        String librarianId = (String) librariansTableModel.getValueAt(modelRow, 0);
        String librarianName = (String) librariansTableModel.getValueAt(modelRow, 1);
        String librarianUsername = (String) librariansTableModel.getValueAt(modelRow, 2);

        if (librarianUsername.equals(session.getUsername())) {
            showWarning("Cannot delete your own account record."); return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete librarian:\nID: " + librarianId + "\nName: " + librarianName + "\nUsername: " + librarianUsername + "\n\nThis also deletes their login.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        deleteLibrarianButton.setEnabled(false);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() throws Exception {
                return adminService.deleteLibrarian(librarianId);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        showSuccess("Librarian '" + librarianName + "' deleted.");
                        loadAllLibrarians(); loadAllUsers();
                    } else { showError("Failed to delete librarian '" + librarianName + "'."); }
                } catch (Exception e) { handleLoadingError(e, "deleting librarian"); }
                finally { deleteLibrarianButton.setEnabled(true); }
            }
        };
        worker.execute();
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            UserSession.clearInstance(); this.dispose(); SwingUtilities.invokeLater(LoginFrame::new);
        }
    }

    // --- Data Loading Methods ---

    private void loadAllUsers() {
        String searchTerm = userSearchField != null ? userSearchField.getText().trim() : ""; // Get search term if field exists
        usersTableModel.setRowCount(0);
        usersTableModel.addRow(new Object[]{"Loading...", "", "", ""});

        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                // TODO: Enhance AdminService.getAllUsers to accept a search term
                // For now, it loads all users. Filtering happens visually via sorter.
                // If implementing search in backend:
                // return adminService.searchUsers(searchTerm);
                return adminService.getAllUsers(); // Current implementation
            }
            @Override protected void done() {
                try {
                    List<Object[]> users = get();
                    usersTableModel.setRowCount(0);
                    if(users.isEmpty()){ System.out.println("No users found."); }
                    else { users.forEach(usersTableModel::addRow); }
                    // Apply filtering based on search term if not done in backend
                    applyUserFilter(searchTerm);
                } catch (Exception e) { handleLoadingError(e, "users"); usersTableModel.setRowCount(0); }
            }
        };
        worker.execute();
    }

    // Helper to apply filtering to the user table after loading all data
    // Ideally, filtering should be done in the SQL query for large datasets
    private void applyUserFilter(String searchTerm) {
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) usersTable.getRowSorter();
        if (sorter == null) { // Create sorter if it doesn't exist
            sorter = new TableRowSorter<>(usersTableModel);
            usersTable.setRowSorter(sorter);
        }
        if (searchTerm.isEmpty()) {
            sorter.setRowFilter(null); // Remove filter
        } else {
            // Filter based on username (col 0) or email (col 3), case-insensitive
            // Multiple column filter: RowFilter.orFilter(...)
            try {
                RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + searchTerm, 0, 3); // Case-insensitive regex on cols 0 and 3
                sorter.setRowFilter(rf);
            } catch (java.util.regex.PatternSyntaxException e) {
                System.err.println("Invalid regex pattern in search: " + searchTerm);
                sorter.setRowFilter(null); // Clear filter on invalid pattern
            }
        }
    }


    private void loadAllLibrarians() {
        librariansTableModel.setRowCount(0);
        librariansTableModel.addRow(new Object[]{"Loading...", "", "", ""});

        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                return adminService.getAllLibrarians();
            }
            @Override protected void done() {
                try {
                    List<Object[]> librarians = get();
                    librariansTableModel.setRowCount(0);
                    if(librarians.isEmpty()){ System.out.println("No librarians found."); }
                    else { librarians.forEach(librariansTableModel::addRow); }
                } catch (Exception e) { handleLoadingError(e, "librarians"); librariansTableModel.setRowCount(0); }
            }
        };
        worker.execute();
    }

    // --- UI Helper Methods ---
    private void showMessage(String message, String title, int messageType){ SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message, title, messageType)); }
    private void showError(String message) { showMessage(message, "Error", JOptionPane.ERROR_MESSAGE); }
    private void showWarning(String message) { showMessage(message, "Warning", JOptionPane.WARNING_MESSAGE); }
    private void showSuccess(String message) { showMessage(message, "Success", JOptionPane.INFORMATION_MESSAGE); }
    private void showInfo(String message) { showMessage(message, "Information", JOptionPane.INFORMATION_MESSAGE); }
    private void handleLoadingError(Exception e, String context) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        showError("Error loading " + context + ": " + cause.getMessage());
        cause.printStackTrace();
    }
    // Basic email validation helper
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        // Simple regex for basic format check - not foolproof
        String emailRegex = "^[a-zA-Z0-9_+&-]+(?:\\.[a-zA-Z0-9_+&-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

}