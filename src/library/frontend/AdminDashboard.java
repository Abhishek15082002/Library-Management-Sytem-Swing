package library.frontend;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException; // Keep for catch blocks
import java.time.LocalDate; // For month/year selection
import java.time.Month; // For month names
import java.util.ArrayList;
import java.util.Calendar; // For year selection
import java.util.List;
import java.util.Vector; // For ComboBox model

import library.UserSession;
import library.backend.AdminService;

/**
 * Admin Dashboard GUI. Provides administrative functions for managing users,
 * librarians, fines, and viewing reports.
 */
public class AdminDashboard extends JFrame implements ActionListener {

    private final AdminService adminService;
    private final UserSession session;

    // --- Main UI Components ---
    private JTabbedPane mainTabs;

    // --- User Management Tab Components ---
    private JPanel userManagementPanel;
    private JTable usersTable;
    private DefaultTableModel usersTableModel;
    private JButton activateUserButton;
    private JButton deactivateUserButton;
    private JTextField userSearchField;
    private JButton userSearchButton;

    // --- Librarian Management Tab Components ---
    private JPanel librarianManagementPanel;
    private JSplitPane librarianSplitPane;
    // Add Form
    private JPanel addLibrarianPanel;
    private JTextField libUsernameField;
    private JPasswordField libPasswordField;
    private JTextField libNameField;
    private JTextField libEmailField;
    private JButton addLibrarianButton;
    // View Table
    private JPanel viewLibrariansPanel;
    private JTable librariansTable;
    private DefaultTableModel librariansTableModel;
    private JButton deleteLibrarianButton;

    // --- Fine Management Tab Components ---
    private JPanel fineManagementPanel;
    private JTable finesTable;
    private DefaultTableModel finesTableModel;
    private JButton waiveFineButton;
    private JTextField fineSearchStudentIdField;
    private JButton fineSearchButton;

    // --- Reports Tab Components ---
    private JPanel reportsPanel; // Main panel for the reports tab
    private JTabbedPane reportsSubTabs; // Sub-tabs for different report types
    // Book Reports
    private JPanel availableBooksReportPanel;
    private JTable availableBooksReportTable;
    private DefaultTableModel availableBooksReportModel;
    private JButton refreshAvailButton; // Added refresh button
    private JPanel borrowedBooksReportPanel;
    private JTable borrowedBooksReportTable;
    private DefaultTableModel borrowedBooksReportModel;
    private JButton refreshBorrowedButton; // Added refresh button
    // Fine Reports
    private JPanel fineReportsPanel; // Panel to hold fine report sub-tabs
    private JTabbedPane fineReportsSubTabs;
    // Individual Fine Report
    private JPanel individualFineReportPanel;
    private JTextField studentIdFineReportField;
    private JButton searchStudentFineReportButton;
    private JTable individualFineReportTable;
    private DefaultTableModel individualFineReportModel;
    // Monthly Fine Report
    private JPanel monthlyFineReportPanel;
    private JComboBox<Integer> yearSelector;
    private JComboBox<Month> monthSelector;
    private JButton searchMonthlyFineReportButton;
    private JLabel monthlyTotalLabel; // To display the total

    // --- Logout Components ---
    private JButton logoutButton;

    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);   // Blue
    private static final Color SECONDARY_COLOR = new Color(236, 240, 241); // Light Gray
    private static final Color ACCENT_COLOR = new Color(255, 107, 107);    // Red
    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final int BORDER_RADIUS = 8;

    private static class RoundedBorder implements javax.swing.border.Border {
        private int radius;
        RoundedBorder(int radius) { this.radius = radius; }
        public Insets getBorderInsets(Component c) { return new Insets(this.radius, this.radius, this.radius, this.radius); }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(PRIMARY_COLOR);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }
    }


    public AdminDashboard() {
        session = UserSession.getInstance();
        if (session == null || !"Admin".equalsIgnoreCase(session.getRole())) {
            JOptionPane.showMessageDialog(null, "Access Denied.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose(); SwingUtilities.invokeLater(LoginFrame::new);
            // Need to initialize final fields before returning, even if null
            this.adminService = null;
            return;
        }
        // Initialize service only if session is valid
        this.adminService = new AdminService();

        setTitle("Admin Dashboard - " + session.getUsername());
        setSize(1200, 800); // Increased size slightly for reports
        setMinimumSize(new Dimension(950, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { performLogout(); }
        });

        mainTabs = new JTabbedPane();
        mainTabs.setFont(MAIN_FONT);
        mainTabs.setBackground(SECONDARY_COLOR);
        // Create and add tabs
        createUserManagementTab();
        createLibrarianManagementTab();
        createFineManagementTab();
        createReportsTab(); // Create the actual reports tab now

        mainTabs.addTab("User Accounts", userManagementPanel);
        mainTabs.addTab("Manage Librarians", librarianManagementPanel);
        mainTabs.addTab("Fine Management", fineManagementPanel);
        mainTabs.addTab("View Reports", reportsPanel); // Add reports tab

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(PRIMARY_COLOR);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Admin Dashboard - " + session.getUsername());
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.WEST);

        logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(140, 40));
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoutButton.addActionListener(this);

        logoutButton.setBackground(ACCENT_COLOR);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(new RoundedBorder(BORDER_RADIUS));

        topPanel.add(logoutButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(mainTabs, BorderLayout.CENTER);

        // Load initial data
        loadAllUsers();
        loadAllLibrarians();
        loadUnpaidFines();
        // Load initial reports (optional, or load on tab selection/button click)
        loadAvailableBooksReport();
        loadBorrowedBooksReport();
    }

    // --- Tab Creation Methods ---

    private void createUserManagementTab() {
        userManagementPanel = new JPanel(new BorderLayout(15, 15));
        userManagementPanel.setBackground(SECONDARY_COLOR);
        userManagementPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(SECONDARY_COLOR);
        userSearchField = new JTextField(25); userSearchButton = new JButton("Search Users");
        userSearchField.setFont(MAIN_FONT);
        userSearchButton.setFont(MAIN_FONT);

        userSearchButton.setBackground(PRIMARY_COLOR);
        userSearchButton.setForeground(Color.WHITE);
        userSearchButton.setFocusPainted(false);
        userSearchButton.setBorder(new RoundedBorder(BORDER_RADIUS));
        userSearchButton.addActionListener(this);
        userSearchButton.addActionListener(this); userSearchField.addActionListener(e -> userSearchButton.doClick());
        searchPanel.add(new JLabel("Search Username/Email:")); searchPanel.add(userSearchField); searchPanel.add(userSearchButton);
        userManagementPanel.add(searchPanel, BorderLayout.NORTH);
        String[] userColumns = {"Username", "Role", "Status", "Email"};
        usersTableModel = new DefaultTableModel(userColumns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; }};
        usersTable = new JTable(usersTableModel); usersTable.setFont(MAIN_FONT);
        usersTable.getTableHeader().setFont(MAIN_FONT); usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); usersTable.setAutoCreateRowSorter(true);
        JScrollPane userScrollPane = new JScrollPane(usersTable);
        userScrollPane.getViewport().setBackground(Color.WHITE); userManagementPanel.add(userScrollPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(SECONDARY_COLOR);
        activateUserButton = new JButton("Activate Selected"); activateUserButton.setFont(MAIN_FONT);
        deactivateUserButton = new JButton("Deactivate Selected");  deactivateUserButton.setFont(MAIN_FONT);
        activateUserButton.setToolTipText("Set the selected user's status to Active."); deactivateUserButton.setToolTipText("Set the selected user's status to Inactive.");
        activateUserButton.addActionListener(this); deactivateUserButton.addActionListener(this);
        activateUserButton.setBackground(new Color(46, 204, 113)); // Green
        activateUserButton.setForeground(Color.WHITE);
        activateUserButton.setFocusPainted(false);
        activateUserButton.setBorder(new RoundedBorder(BORDER_RADIUS));
        deactivateUserButton.setBackground(ACCENT_COLOR);
        deactivateUserButton.setForeground(Color.WHITE);
        deactivateUserButton.setFocusPainted(false);
        deactivateUserButton.setBorder(new RoundedBorder(BORDER_RADIUS));
        buttonPanel.add(activateUserButton); buttonPanel.add(deactivateUserButton);
        userManagementPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createLibrarianManagementTab() {
        librarianManagementPanel = new JPanel(new BorderLayout());
        librarianManagementPanel.setBackground(SECONDARY_COLOR);
        librarianManagementPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        addLibrarianPanel = new JPanel(new GridBagLayout()); addLibrarianPanel.setBorder(BorderFactory.createTitledBorder("Add New Librarian"));
        addLibrarianPanel.setBackground(SECONDARY_COLOR);
        addLibrarianPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(5, 10, 5, 10); gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        int gridY = 0;
        gbc.gridx = 0; gbc.gridy = gridY; gbc.weightx = 0.1; addLibrarianPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY++; gbc.weightx = 0.9; libUsernameField = new JTextField(15); addLibrarianPanel.add(libUsernameField, gbc);
        gbc.gridx = 0; gbc.gridy = gridY; gbc.weightx = 0.1; addLibrarianPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY++; gbc.weightx = 0.9; libPasswordField = new JPasswordField(15); addLibrarianPanel.add(libPasswordField, gbc);
        gbc.gridx = 0; gbc.gridy = gridY; gbc.weightx = 0.1; addLibrarianPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY++; gbc.weightx = 0.9; libNameField = new JTextField(15); addLibrarianPanel.add(libNameField, gbc);
        gbc.gridx = 0; gbc.gridy = gridY; gbc.weightx = 0.1; addLibrarianPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridY++; gbc.weightx = 0.9; libEmailField = new JTextField(15); addLibrarianPanel.add(libEmailField, gbc);
        gbc.gridx = 0; gbc.gridy = gridY++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER; gbc.weightx = 0.0;
        addLibrarianButton = new JButton("Add Librarian"); addLibrarianButton.addActionListener(this); addLibrarianPanel.add(addLibrarianButton, gbc);
        addLibrarianButton.setFont(MAIN_FONT);
        addLibrarianButton.setBackground(new Color(46, 204, 113)); // Green
        addLibrarianButton.setForeground(Color.WHITE);
        addLibrarianButton.setFocusPainted(false);
        addLibrarianButton.setBorder(new RoundedBorder(BORDER_RADIUS));
        gbc.gridy = gridY; gbc.weighty = 1.0; addLibrarianPanel.add(new JLabel(), gbc);
        viewLibrariansPanel = new JPanel(new BorderLayout(10, 10)); viewLibrariansPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PRIMARY_COLOR), "Current Librarians", 0, 0, SUBTITLE_FONT, PRIMARY_COLOR));
        viewLibrariansPanel.setBackground(SECONDARY_COLOR);
        String[] libColumns = {"Librarian ID", "Name", "Username", "Email"};
        librariansTableModel = new DefaultTableModel(libColumns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; }};
        librariansTable = new JTable(librariansTableModel); librariansTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); librariansTable.setAutoCreateRowSorter(true);
        librariansTable.setFont(MAIN_FONT);
        librariansTable.getTableHeader().setFont(MAIN_FONT);
        JScrollPane librarianScrollPane = new JScrollPane(librariansTable); viewLibrariansPanel.add(librarianScrollPane, BorderLayout.CENTER);
        librarianScrollPane.getViewport().setBackground(Color.WHITE);
        JPanel deleteButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); deleteLibrarianButton = new JButton("Delete Selected Librarian"); deleteLibrarianButton.addActionListener(this);
        deleteLibrarianButton.setFont(MAIN_FONT);
        deleteLibrarianButton.setBackground(ACCENT_COLOR);
        deleteLibrarianButton.setForeground(Color.WHITE);
        deleteLibrarianButton.setFocusPainted(false);
        deleteLibrarianButton.setBorder(new RoundedBorder(BORDER_RADIUS));
        deleteButtonPanel.add(deleteLibrarianButton); viewLibrariansPanel.add(deleteButtonPanel, BorderLayout.SOUTH);
        deleteButtonPanel.setBackground(SECONDARY_COLOR);
        librarianSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, addLibrarianPanel, viewLibrariansPanel);
        librarianSplitPane.setDividerLocation(4000); librarianSplitPane.setResizeWeight(0.4);
        librarianManagementPanel.add(librarianSplitPane, BorderLayout.CENTER);
    }

    private void createFineManagementTab() {
        fineManagementPanel = new JPanel(new BorderLayout(10, 10)); fineManagementPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        fineManagementPanel.setBackground(SECONDARY_COLOR);
        fineManagementPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); fineSearchStudentIdField = new JTextField(15);
        fineSearchButton = new JButton("Filter by Student ID"); fineSearchButton.setToolTipText("Enter Student ID (e.g., S001) or leave blank for all.");
        fineSearchButton.setFont(MAIN_FONT);
        fineSearchButton.setBackground(PRIMARY_COLOR);
        fineSearchButton.setForeground(Color.WHITE);
        fineSearchButton.setFocusPainted(false);
        fineSearchButton.setBorder(new RoundedBorder(BORDER_RADIUS));
        fineSearchButton.addActionListener(this); fineSearchStudentIdField.addActionListener(e -> fineSearchButton.doClick());
        searchPanel.add(new JLabel("Filter by Student ID:")); searchPanel.add(fineSearchStudentIdField); searchPanel.add(fineSearchButton);
        fineManagementPanel.add(searchPanel, BorderLayout.NORTH);
        String[] fineColumns = {"Fine ID", "Student ID", "Student Name", "Issue ID", "Book Title", "Amount", "Fine Date"};
        finesTableModel = new DefaultTableModel(fineColumns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; }};
        finesTable = new JTable(finesTableModel); finesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); finesTable.setAutoCreateRowSorter(true);
        finesTable.setFont(MAIN_FONT); finesTable.getTableHeader().setFont(MAIN_FONT);
        finesTable.getColumnModel().getColumn(0).setPreferredWidth(60); finesTable.getColumnModel().getColumn(1).setPreferredWidth(80); finesTable.getColumnModel().getColumn(2).setPreferredWidth(150); finesTable.getColumnModel().getColumn(3).setPreferredWidth(60); finesTable.getColumnModel().getColumn(4).setPreferredWidth(250); finesTable.getColumnModel().getColumn(5).setPreferredWidth(80); finesTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        JScrollPane fineScrollPane = new JScrollPane(finesTable); fineManagementPanel.add(fineScrollPane, BorderLayout.CENTER);
        fineScrollPane.getViewport().setBackground(Color.WHITE);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); waiveFineButton = new JButton("Waive Selected Fine");
        waiveFineButton.setFont(MAIN_FONT);
        waiveFineButton.setBackground(PRIMARY_COLOR);
        waiveFineButton.setForeground(Color.WHITE);
        waiveFineButton.setFocusPainted(false);
        waiveFineButton.setBorder(new RoundedBorder(BORDER_RADIUS));
        waiveFineButton.setToolTipText("Mark the selected unpaid fine as paid/waived."); waiveFineButton.addActionListener(this);
        buttonPanel.add(waiveFineButton); fineManagementPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    // --- Reports Tab Implementation ---
    private void createReportsTab() {
        reportsPanel = new JPanel(new BorderLayout());
        reportsPanel.setBackground(SECONDARY_COLOR);
        reportsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        reportsSubTabs = new JTabbedPane();
        reportsSubTabs.setFont(MAIN_FONT);
        reportsSubTabs.setBackground(SECONDARY_COLOR);

        // Create and add sub-tabs
        createAvailableBooksReportTab();
        createBorrowedBooksReportTab();
        createFineReportsTabContainer(); // Creates the container with its own sub-tabs

        reportsSubTabs.addTab("Available Books", availableBooksReportPanel);
        reportsSubTabs.addTab("Borrowed Books", borrowedBooksReportPanel);
        reportsSubTabs.addTab("Fine Reports", fineReportsPanel); // Add the container panel

        reportsPanel.add(reportsSubTabs, BorderLayout.CENTER);
    }

    private void createAvailableBooksReportTab() {
        availableBooksReportPanel = new JPanel(new BorderLayout(10, 10));
        availableBooksReportPanel.setBackground(SECONDARY_COLOR);
        availableBooksReportPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        String[] columns = {"Book ID", "Title", "Author", "Category", "Available Copies"};
        availableBooksReportModel = new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; }};
        availableBooksReportTable = new JTable(availableBooksReportModel);
        availableBooksReportTable.setAutoCreateRowSorter(true);
        availableBooksReportPanel.add(new JScrollPane(availableBooksReportTable), BorderLayout.CENTER);

        refreshAvailButton = new JButton("Refresh");
        refreshAvailButton.setFont(MAIN_FONT);
        refreshAvailButton.setBackground(PRIMARY_COLOR);
        refreshAvailButton.setForeground(Color.WHITE);
        refreshAvailButton.setFocusPainted(false);
        refreshAvailButton.setBorder(new RoundedBorder(BORDER_RADIUS));
        refreshAvailButton.addActionListener(e -> loadAvailableBooksReport());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(refreshAvailButton);
        availableBooksReportPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void createBorrowedBooksReportTab() {
        borrowedBooksReportPanel = new JPanel(new BorderLayout(10, 10));
        borrowedBooksReportPanel.setBackground(SECONDARY_COLOR);
        borrowedBooksReportPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        String[] columns = {"Issue ID", "Book ID", "Book Title", "Student ID", "Student Name", "Issue Date", "Due Date", "Status"};
        borrowedBooksReportModel = new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; }};
        borrowedBooksReportTable = new JTable(borrowedBooksReportModel);
        borrowedBooksReportTable.setAutoCreateRowSorter(true);
        borrowedBooksReportPanel.add(new JScrollPane(borrowedBooksReportTable), BorderLayout.CENTER);

        refreshBorrowedButton = new JButton("Refresh");
        refreshAvailButton.setFont(MAIN_FONT);
        refreshAvailButton.setBackground(PRIMARY_COLOR);
        refreshAvailButton.setForeground(Color.WHITE);
        refreshAvailButton.setFocusPainted(false);
        refreshAvailButton.setBorder(new RoundedBorder(BORDER_RADIUS));
        refreshBorrowedButton.addActionListener(e -> loadBorrowedBooksReport());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(refreshBorrowedButton);
        borrowedBooksReportPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void createFineReportsTabContainer() {
        fineReportsPanel = new JPanel(new BorderLayout());
        fineReportsPanel.setBackground(SECONDARY_COLOR);
        fineReportsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));// Panel to hold the sub-tabs
        fineReportsSubTabs = new JTabbedPane();
        fineReportsSubTabs.setFont(MAIN_FONT);
        fineReportsSubTabs.setBackground(SECONDARY_COLOR);// Sub-tabs for fine reports

        createIndividualFineReportTab(); // Create the individual fine report tab panel
        createMonthlyFineReportTab(); // Create the monthly fine report tab panel

        fineReportsSubTabs.addTab("By Student", individualFineReportPanel);
        fineReportsSubTabs.addTab("Monthly Summary", monthlyFineReportPanel);

        fineReportsPanel.add(fineReportsSubTabs, BorderLayout.CENTER); // Add sub-tabs to the container
    }

    private void createIndividualFineReportTab() {
        individualFineReportPanel = new JPanel(new BorderLayout(10, 10));
        individualFineReportPanel.setBackground(SECONDARY_COLOR);
        individualFineReportPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        studentIdFineReportField = new JTextField(15);
        searchStudentFineReportButton = new JButton("Get Student Fine Report");
        searchStudentFineReportButton.addActionListener(this);
        searchStudentFineReportButton.setFont(MAIN_FONT);
        searchStudentFineReportButton.setBackground(PRIMARY_COLOR);
        searchStudentFineReportButton.setForeground(Color.WHITE);
        searchStudentFineReportButton.setFocusPainted(false);
        searchStudentFineReportButton.setBorder(new RoundedBorder(BORDER_RADIUS));
        studentIdFineReportField.addActionListener(e -> searchStudentFineReportButton.doClick());
        searchPanel.add(new JLabel("Enter Student ID (e.g., S001):"));
        searchPanel.add(studentIdFineReportField);
        searchPanel.add(searchStudentFineReportButton);
        individualFineReportPanel.add(searchPanel, BorderLayout.NORTH);
        // Table
        String[] columns = {"Fine ID", "Issue ID", "Book Title", "Amount", "Fine Date", "Status"};
        individualFineReportModel = new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int r, int c) { return false; }};
        individualFineReportTable = new JTable(individualFineReportModel);
        individualFineReportTable.setAutoCreateRowSorter(true);
        individualFineReportPanel.add(new JScrollPane(individualFineReportTable), BorderLayout.CENTER);
    }

    private void createMonthlyFineReportTab() {
        monthlyFineReportPanel = new JPanel(new BorderLayout(10, 10));
        monthlyFineReportPanel.setBackground(SECONDARY_COLOR);
        monthlyFineReportPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // Controls Panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Vector<Integer> years = new Vector<>();
        for (int y = currentYear + 1; y >= currentYear - 5; y--) { years.add(y); } // Populate years
        yearSelector = new JComboBox<>(years);
        yearSelector.setBorder(new RoundedBorder(BORDER_RADIUS));
        yearSelector.setFont(MAIN_FONT);
        yearSelector.setBackground(PRIMARY_COLOR);
        yearSelector.setForeground(Color.WHITE);
        yearSelector.setFocusable(false);
        monthSelector = new JComboBox<>(Month.values()); monthSelector.setSelectedItem(LocalDate.now().getMonth());
        monthSelector.setBorder(new RoundedBorder(BORDER_RADIUS));
        monthSelector.setFont(MAIN_FONT);
        monthSelector.setBackground(PRIMARY_COLOR);
        monthSelector.setForeground(Color.WHITE);
        monthSelector.setFocusable(false);
        searchMonthlyFineReportButton = new JButton("Get Monthly Report"); searchMonthlyFineReportButton.addActionListener(this);
        searchMonthlyFineReportButton.setFont(MAIN_FONT);
        searchMonthlyFineReportButton.setBackground(PRIMARY_COLOR);
        searchMonthlyFineReportButton.setForeground(Color.WHITE);
        searchMonthlyFineReportButton.setFocusPainted(false);
        searchMonthlyFineReportButton.setBorder(new RoundedBorder(BORDER_RADIUS));
        controlsPanel.add(new JLabel("Year:")); controlsPanel.add(yearSelector);
        controlsPanel.add(new JLabel("Month:")); controlsPanel.add(monthSelector);
        controlsPanel.add(searchMonthlyFineReportButton);
        monthlyFineReportPanel.add(controlsPanel, BorderLayout.NORTH);
        // Result Display
        monthlyTotalLabel = new JLabel("Total Fines for Selected Month: N/A", SwingConstants.CENTER);
        monthlyTotalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        monthlyFineReportPanel.add(monthlyTotalLabel, BorderLayout.CENTER);
    }


    // --- Action Listener Implementation ---
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        try {
            // User/Librarian Management
            if (source == activateUserButton) { handleSetUserStatus(true); }
            else if (source == deactivateUserButton) { handleSetUserStatus(false); }
            else if (source == userSearchButton) { loadAllUsers(); }
            else if (source == addLibrarianButton) { handleAddLibrarian(); }
            else if (source == deleteLibrarianButton) { handleDeleteLibrarian(); }
            // Fine Management
            else if (source == fineSearchButton) { loadUnpaidFines(); }
            else if (source == waiveFineButton) { handleWaiveFine(); }
            // Reports
            else if (source == refreshAvailButton) { loadAvailableBooksReport(); } // Handle refresh
            else if (source == refreshBorrowedButton) { loadBorrowedBooksReport(); } // Handle refresh
            else if (source == searchStudentFineReportButton) { loadFineReportByStudent(); }
            else if (source == searchMonthlyFineReportButton) { loadFineReportByMonth(); }
            // Logout
            else if (source == logoutButton) { performLogout(); }
        } catch (Exception ex) { showError("UI Error: " + ex.getMessage()); ex.printStackTrace(); }
    }

    // --- UI Action Handlers ---

    private void handleSetUserStatus(boolean activate) {
        int selectedRow = usersTable.getSelectedRow(); if (selectedRow == -1) { showWarning("Select user."); return; }
        int modelRow = usersTable.convertRowIndexToModel(selectedRow); String username = (String) usersTableModel.getValueAt(modelRow, 0);
        String currentStatus = (String) usersTableModel.getValueAt(modelRow, 2);
        if ((activate && "Active".equalsIgnoreCase(currentStatus)) || (!activate && "Inactive".equalsIgnoreCase(currentStatus))) { showInfo("User already " + currentStatus + "."); return; }
        if (!activate && username.equals(session.getUsername())) { showWarning("Cannot deactivate self."); return; }
        String action = activate ? "Activate" : "Deactivate"; int confirm = JOptionPane.showConfirmDialog(this, action + " user '" + username + "'?", "Confirm", JOptionPane.YES_NO_OPTION); if (confirm != JOptionPane.YES_OPTION) return;
        activateUserButton.setEnabled(false); deactivateUserButton.setEnabled(false);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() throws Exception { return adminService.setUserStatus(username, activate); }
            @Override protected void done() { try { if (get()) { showSuccess("User status updated."); loadAllUsers(); } else { showError("Failed update status."); } } catch (Exception e) { handleLoadingError(e, "updating status"); } finally { activateUserButton.setEnabled(true); deactivateUserButton.setEnabled(true); } } }; worker.execute();
    }
    private void handleAddLibrarian() {
        String username = libUsernameField.getText().trim(); String password = new String(libPasswordField.getPassword()); String name = libNameField.getText().trim(); String email = libEmailField.getText().trim();
        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || email.isEmpty()) { showWarning("Fill all fields."); return; } if (!isValidEmail(email)) { showWarning("Invalid email."); return; }
        addLibrarianButton.setEnabled(false);
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() throws Exception { return adminService.addLibrarian(username, password, name, email); }
            @Override protected void done() { try { String newId = get(); showSuccess("Librarian '" + name + "' added (ID: " + newId + ")"); libUsernameField.setText(""); libPasswordField.setText(""); libNameField.setText(""); libEmailField.setText(""); loadAllLibrarians(); loadAllUsers(); } catch (Exception e) { handleLoadingError(e, "adding librarian"); } finally { addLibrarianButton.setEnabled(true); } } }; worker.execute();
    }
    private void handleDeleteLibrarian() {
        int selectedRow = librariansTable.getSelectedRow(); if (selectedRow == -1) { showWarning("Select librarian."); return; } int modelRow = librariansTable.convertRowIndexToModel(selectedRow);
        String libId = (String) librariansTableModel.getValueAt(modelRow, 0); String libName = (String) librariansTableModel.getValueAt(modelRow, 1); String libUsername = (String) librariansTableModel.getValueAt(modelRow, 2);
        if (libUsername.equals(session.getUsername())) { showWarning("Cannot delete self."); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete librarian:\nID: " + libId + "\nName: " + libName + "\nUsername: " + libUsername + "\n\nThis deletes login.", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); if (confirm != JOptionPane.YES_OPTION) return;
        deleteLibrarianButton.setEnabled(false);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() throws Exception { return adminService.deleteLibrarian(libId); }
            @Override protected void done() { try { if (get()) { showSuccess("Librarian deleted."); loadAllLibrarians(); loadAllUsers(); } else { showError("Failed delete librarian."); } } catch (Exception e) { handleLoadingError(e, "deleting librarian"); } finally { deleteLibrarianButton.setEnabled(true); } } }; worker.execute();
    }
    private void handleWaiveFine() {
        int selectedRow = finesTable.getSelectedRow(); if (selectedRow == -1) { showWarning("Select fine record."); return; } int modelRow = finesTable.convertRowIndexToModel(selectedRow);
        int fineId = (int) finesTableModel.getValueAt(modelRow, 0); String studentId = (String) finesTableModel.getValueAt(modelRow, 1); double amount = (double) finesTableModel.getValueAt(modelRow, 5);
        int confirm = JOptionPane.showConfirmDialog(this, String.format("Waive fine ID %d (Amt: %.2f) for Student %s?", fineId, amount, studentId), "Confirm Waiver", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); if (confirm != JOptionPane.YES_OPTION) return;
        waiveFineButton.setEnabled(false);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() throws Exception { return adminService.waiveFine(fineId); }
            @Override protected void done() { try { if (get()) { showSuccess("Fine ID " + fineId + " waived."); loadUnpaidFines(); } else { showError("Failed waive fine ID " + fineId + "."); } } catch (Exception e) { handleLoadingError(e, "waiving fine"); } finally { waiveFineButton.setEnabled(true); } } }; worker.execute();
    }
    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) { UserSession.clearInstance(); this.dispose(); SwingUtilities.invokeLater(LoginFrame::new); }
    }

    // --- Data Loading Methods ---

    private void loadAllUsers() {
        String searchTerm = userSearchField != null ? userSearchField.getText().trim() : "";
        usersTableModel.setRowCount(0); usersTableModel.addRow(new Object[]{"Loading...", "", "", ""});
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() throws Exception { return adminService.getAllUsers(); }
            @Override protected void done() { try { List<Object[]> users = get(); usersTableModel.setRowCount(0); if(users.isEmpty()){ System.out.println("No users."); } else { users.forEach(usersTableModel::addRow); } applyUserFilter(searchTerm); } catch (Exception e) { handleLoadingError(e, "users"); usersTableModel.setRowCount(0); } } }; worker.execute();
    }
    private void applyUserFilter(String searchTerm) {
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) usersTable.getRowSorter(); if (sorter == null) { sorter = new TableRowSorter<>(usersTableModel); usersTable.setRowSorter(sorter); } if (searchTerm.isEmpty()) { sorter.setRowFilter(null); } else { try { RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + searchTerm, 0, 3); sorter.setRowFilter(rf); } catch (java.util.regex.PatternSyntaxException e) { System.err.println("Invalid regex: " + searchTerm); sorter.setRowFilter(null); } }
    }
    private void loadAllLibrarians() {
        librariansTableModel.setRowCount(0); librariansTableModel.addRow(new Object[]{"Loading...", "", "", ""});
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() throws Exception { return adminService.getAllLibrarians(); }
            @Override protected void done() { try { List<Object[]> librarians = get(); librariansTableModel.setRowCount(0); if(librarians.isEmpty()){ System.out.println("No librarians."); } else { librarians.forEach(librariansTableModel::addRow); } } catch (Exception e) { handleLoadingError(e, "librarians"); librariansTableModel.setRowCount(0); } } }; worker.execute();
    }
    private void loadUnpaidFines() {
        String studentIdFilter = fineSearchStudentIdField != null ? fineSearchStudentIdField.getText().trim() : "";
        finesTableModel.setRowCount(0); finesTableModel.addRow(new Object[]{"Loading...", "", "", "", "", "", null});
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() throws Exception { return adminService.getUnpaidFines(studentIdFilter); }
            @Override protected void done() { try { List<Object[]> fines = get(); finesTableModel.setRowCount(0); if (fines.isEmpty()) { System.out.println("No unpaid fines."); } else { fines.forEach(finesTableModel::addRow); } } catch (Exception e) { handleLoadingError(e, "unpaid fines"); finesTableModel.setRowCount(0); } } }; worker.execute();
    }

    // --- Report Loading Methods ---
    private void loadAvailableBooksReport() {
        availableBooksReportModel.setRowCount(0); availableBooksReportModel.addRow(new Object[]{"Loading...", "", "", "", ""});
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() throws Exception { return adminService.getAvailableBooksReport(); }
            @Override protected void done() { try { List<Object[]> books = get(); availableBooksReportModel.setRowCount(0); if(books.isEmpty()){ System.out.println("No available books."); } else { books.forEach(availableBooksReportModel::addRow); } } catch (Exception e) { handleLoadingError(e, "available books report"); availableBooksReportModel.setRowCount(0); } } }; worker.execute();
    }
    private void loadBorrowedBooksReport() {
        borrowedBooksReportModel.setRowCount(0); borrowedBooksReportModel.addRow(new Object[]{"Loading...", "", "", "", "", null, null, ""});
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() throws Exception { return adminService.getAllBorrowedBooksReport(); }
            @Override protected void done() { try { List<Object[]> books = get(); borrowedBooksReportModel.setRowCount(0); if(books.isEmpty()){ System.out.println("No borrowed books."); } else { books.forEach(borrowedBooksReportModel::addRow); } } catch (Exception e) { handleLoadingError(e, "borrowed books report"); borrowedBooksReportModel.setRowCount(0); } } }; worker.execute();
    }
    private void loadFineReportByStudent() {
        String studentId = studentIdFineReportField.getText().trim(); if (studentId.isEmpty()) { showWarning("Enter Student ID."); return; }
        individualFineReportModel.setRowCount(0); individualFineReportModel.addRow(new Object[]{"Loading...", "", "", "", null, ""});
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() throws Exception { return adminService.getFineReportByStudent(studentId); }
            @Override protected void done() { try { List<Object[]> fines = get(); individualFineReportModel.setRowCount(0); if(fines.isEmpty()){ showInfo("No fines for student " + studentId); } else { fines.forEach(individualFineReportModel::addRow); } } catch (Exception e) { handleLoadingError(e, "individual fine report"); individualFineReportModel.setRowCount(0); } } }; worker.execute();
    }
    private void loadFineReportByMonth() {
        Integer selectedYear = (Integer) yearSelector.getSelectedItem(); Month selectedMonth = (Month) monthSelector.getSelectedItem();
        if (selectedYear == null || selectedMonth == null) { showWarning("Select year and month."); return; }
        int year = selectedYear; int month = selectedMonth.getValue();
        monthlyTotalLabel.setText("Loading total for " + selectedMonth + " " + year + "...");
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() throws Exception { return adminService.getFineReportByMonth(year, month); }
            @Override protected void done() { try { List<Object[]> result = get(); if (!result.isEmpty() && result.get(0) != null) { double total = (double) result.get(0)[0]; monthlyTotalLabel.setText(String.format("Total Fines for %s %d: %.2f", selectedMonth, year, total)); } else { monthlyTotalLabel.setText("No fines for " + selectedMonth + " " + year + "."); } } catch (Exception e) { handleLoadingError(e, "monthly fine report"); monthlyTotalLabel.setText("Error loading report."); } } }; worker.execute();
    }

    // --- UI Helper Methods ---
    private void showMessage(String message, String title, int messageType){ SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message, title, messageType)); }
    private void showError(String message) { showMessage(message, "Error", JOptionPane.ERROR_MESSAGE); }
    private void showWarning(String message) { showMessage(message, "Warning", JOptionPane.WARNING_MESSAGE); }
    private void showSuccess(String message) { showMessage(message, "Success", JOptionPane.INFORMATION_MESSAGE); }
    private void showInfo(String message) { showMessage(message, "Information", JOptionPane.INFORMATION_MESSAGE); }
    private void handleLoadingError(Exception e, String context) { Throwable cause = e.getCause() != null ? e.getCause() : e; showError("Error loading " + context + ": " + cause.getMessage()); cause.printStackTrace(); }
    private boolean isValidEmail(String email) { if (email == null || email.isEmpty()) return false; String emailRegex = "^[a-zA-Z0-9_+&-]+(?:\\.[a-zA-Z0-9_+&-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"; return email.matches(emailRegex); }

}