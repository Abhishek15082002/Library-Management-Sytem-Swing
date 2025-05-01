package library.frontend;

// Necessary imports
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import library.UserSession;
import library.backend.StudentService;

public class StudentDashboard extends JFrame implements ActionListener {

    // --- GUI Components ---
    private JTabbedPane mainTabbedPane;
    private JPanel borrowBooksPanel;
    private JPanel myBooksPanel;
    private JPanel requestBooksPanel;
    private JPanel notificationsPanel;
    private JPanel logoutPanel;
    private JTextField searchField;
    private JButton searchButton;
    private JTable availableBooksTable;
    private DefaultTableModel availableBooksModel;
    private JButton borrowButton;
    private JTable borrowedBooksTable;
    private DefaultTableModel borrowedBooksModel;
    private JButton returnButton;
    private JButton reissueButton;
    private JButton viewFineButton;
    private JTextField requestTitleField;
    private JTextField requestAuthorField;
    private JTextArea requestReasonArea;
    private JButton submitRequestButton;
    private JTable notificationsTable;
    private DefaultTableModel notificationsModel;
    private JButton markReadButton;
    private List<Integer> notificationIds; // Maps view row index to notification_id
    private JButton logoutButton;

    // --- Backend Service and Session ---
    private final StudentService studentService; // Keep it final
    private final UserSession session; // Keep it final

    // Updated Constructor based on user input
    public StudentDashboard() {
        // Get session first
        session = UserSession.getInstance(); // Get session from library package
        // Validate session
        if (session == null || !"Student".equalsIgnoreCase(session.getRole())) {
            JOptionPane.showMessageDialog(null, "Access Denied: Invalid session or role.", "Session Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            SwingUtilities.invokeLater(LoginFrame::new); // LoginFrame is in this package
            // Need to initialize final fields even if returning early, or structure differently
            // To satisfy compiler, assign here before return, though object won't be used
            this.studentService = null; // Assign null to satisfy final field check before returning
            return; // Stop constructor if session is invalid
        }

        // Initialize the final service field after successful session validation
        this.studentService = new StudentService(); // Instantiate service from backend

        // Proceed with setting up the rest of the UI
        setTitle("Student Dashboard - Welcome, " + session.getUsername() + " (ID: " + session.getUserId() + ")");
        setSize(950, 700);
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                performLogout();
            }
        });

        mainTabbedPane = new JTabbedPane();
        notificationIds = new ArrayList<>(); // Initialize list

        // Create UI tabs
        createBorrowBooksTab();
        createMyBooksTab();
        createRequestBooksTab();
        createNotificationsTab();

        // Add tabs
        mainTabbedPane.addTab("Borrow Books", borrowBooksPanel);
        mainTabbedPane.addTab("My Borrowed Books", myBooksPanel);
        mainTabbedPane.addTab("Request New Book", requestBooksPanel);
        mainTabbedPane.addTab("Notifications", notificationsPanel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Student Dashboard - Welcome, " + session.getUsername());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.WEST);

        logoutButton = new JButton("Logout"); // Initialize
        logoutButton.setPreferredSize(new Dimension(120, 35));
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.setToolTipText("Logout and return to the login screen.");
        logoutButton.addActionListener(this);
        topPanel.add(logoutButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(mainTabbedPane, BorderLayout.CENTER);

        // Load initial data
        loadInitialData();
    }

    // --- Tab Creation Methods (GUI setup - unchanged) ---
    private void createBorrowBooksTab() {
        borrowBooksPanel = new JPanel(new BorderLayout(10, 10));
        borrowBooksPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(30);
        searchButton = new JButton("Search");
        searchButton.addActionListener(this);
        searchField.addActionListener(e -> searchButton.doClick());
        searchPanel.add(new JLabel("Search by Title/Author/Category:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        borrowBooksPanel.add(searchPanel, BorderLayout.NORTH);
        String[] availableBookColumns = {"Book ID", "Title", "Author", "Category", "Avg Rating", "Available"};
        availableBooksModel = new DefaultTableModel(availableBookColumns, 0) { @Override public boolean isCellEditable(int r, int c){ return false; }};
        availableBooksTable = new JTable(availableBooksModel);
        availableBooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availableBooksTable.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(availableBooksTable);
        borrowBooksPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        borrowButton = new JButton("Borrow Selected Book");
        borrowButton.addActionListener(this);
        actionPanel.add(borrowButton);
        borrowBooksPanel.add(actionPanel, BorderLayout.SOUTH);
    }
    private void createMyBooksTab() {
        myBooksPanel = new JPanel(new BorderLayout(10, 10));
        myBooksPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        String[] borrowedBookColumns = {"Issue ID", "Book ID", "Title", "Issue Date", "Due Date", "Return Date", "Status", "Reissues", "Fine (Unpaid)"};
        borrowedBooksModel = new DefaultTableModel(borrowedBookColumns, 0){ @Override public boolean isCellEditable(int r, int c){ return false; }};
        borrowedBooksTable = new JTable(borrowedBooksModel);
        borrowedBooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        borrowedBooksTable.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(borrowedBooksTable);
        myBooksPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        returnButton = new JButton("Return Selected");
        reissueButton = new JButton("Reissue Selected");
        viewFineButton = new JButton("View/Pay Fine");
        returnButton.setToolTipText("Mark the selected borrowed book as returned.");
        reissueButton.setToolTipText("Request to extend the due date for the selected book.");
        viewFineButton.setToolTipText("View details of any unpaid fines for the selected record.");
        returnButton.addActionListener(this);
        reissueButton.addActionListener(this);
        viewFineButton.addActionListener(this);
        actionPanel.add(returnButton);
        actionPanel.add(reissueButton);
        actionPanel.add(viewFineButton);
        myBooksPanel.add(actionPanel, BorderLayout.SOUTH);
    }
    private void createRequestBooksTab() {
        requestBooksPanel = new JPanel(new GridBagLayout());
        requestBooksPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; requestBooksPanel.add(new JLabel("Book Title:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; requestTitleField = new JTextField(30); requestBooksPanel.add(requestTitleField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; requestBooksPanel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; requestAuthorField = new JTextField(30); requestBooksPanel.add(requestAuthorField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.NORTHWEST; requestBooksPanel.add(new JLabel("Reason (Optional):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        requestReasonArea = new JTextArea(5, 30); requestReasonArea.setLineWrap(true); requestReasonArea.setWrapStyleWord(true);
        JScrollPane reasonScrollPane = new JScrollPane(requestReasonArea); requestBooksPanel.add(reasonScrollPane, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER; gbc.weighty = 0.0;
        submitRequestButton = new JButton("Submit Request"); submitRequestButton.addActionListener(this); requestBooksPanel.add(submitRequestButton, gbc);
    }
    private void createNotificationsTab() {
        notificationsPanel = new JPanel(new BorderLayout(10, 10));
        notificationsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        String[] notificationColumns = {"Date", "Type", "Message", "Read"};
        notificationsModel = new DefaultTableModel(notificationColumns, 0){ @Override public boolean isCellEditable(int r, int c){ return false; }};
        notificationsTable = new JTable(notificationsModel);
        notificationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notificationsTable.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(notificationsTable);
        notificationsPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        markReadButton = new JButton("Mark Selected as Read");
        markReadButton.addActionListener(this);
        actionPanel.add(markReadButton);
        notificationsPanel.add(actionPanel, BorderLayout.SOUTH);
    }

    /** Loads initial data for all relevant tables. */
    private void loadInitialData() {
        loadAvailableBooks(""); // Load all initially
        loadMyBorrowedBooks();
        loadNotifications();
    }

    // --- Action Listener Implementation (Calls handlers) ---
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        try {
            if (source == searchButton) { handleSearchBooks(); }
            else if (source == borrowButton) { handleBorrowBook(); }
            else if (source == returnButton) { handleReturnBook(); }
            else if (source == reissueButton) { handleReissueBook(); }
            else if (source == viewFineButton) { handleViewFine(); }
            else if (source == submitRequestButton) { handleSubmitRequest(); }
            else if (source == markReadButton) { handleMarkNotificationRead(); }
            else if (source == logoutButton) { performLogout(); }
        } catch (Exception ex) {
            showError("An unexpected UI error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // --- UI Action Handlers (Delegate to Service) ---

    private void handleSearchBooks() {
        loadAvailableBooks(searchField.getText().trim());
    }

    private void handleBorrowBook() {
        int selectedRow = availableBooksTable.getSelectedRow();
        if (selectedRow == -1) { showWarning("Please select a book to borrow."); return; }
        int modelRow = availableBooksTable.convertRowIndexToModel(selectedRow);
        String bookId = (String) availableBooksModel.getValueAt(modelRow, 0);

        borrowButton.setEnabled(false);
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() throws Exception {
                return studentService.borrowBook(session.getUserId(), bookId);
            }
            @Override protected void done() {
                try {
                    String successMessage = get(); showSuccess(successMessage);
                    loadAvailableBooks(searchField.getText().trim()); loadMyBorrowedBooks();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    showError("Error borrowing book: " + cause.getMessage());
                    loadAvailableBooks(searchField.getText().trim());
                } finally { borrowButton.setEnabled(true); }
            }
        };
        worker.execute();
    }

    private void handleReturnBook() {
        int selectedRow = borrowedBooksTable.getSelectedRow();
        if (selectedRow == -1) { showWarning("Please select a book to return."); return; }
        int modelRow = borrowedBooksTable.convertRowIndexToModel(selectedRow);

        int issueId = (int) borrowedBooksModel.getValueAt(modelRow, 0);
        String bookId = (String) borrowedBooksModel.getValueAt(modelRow, 1);
        String currentStatus = (String) borrowedBooksModel.getValueAt(modelRow, 6);
        Object dueDateObj = borrowedBooksModel.getValueAt(modelRow, 4);
        LocalDate dueDate = (dueDateObj instanceof java.sql.Date) ? ((java.sql.Date) dueDateObj).toLocalDate() : null;

        if ("Returned".equalsIgnoreCase(currentStatus)) { showInfo("Book already returned."); return; }

        int confirm = JOptionPane.showConfirmDialog(this, "Return this book?", "Confirm Return", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        returnButton.setEnabled(false);
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() throws Exception {
                return studentService.returnBook(issueId, bookId, session.getUserId(), dueDate);
            }
            @Override protected void done() {
                try {
                    String resultMessage = get(); showSuccess(resultMessage);
                    loadMyBorrowedBooks(); loadAvailableBooks(searchField.getText().trim());
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    showError("Error returning book: " + cause.getMessage());
                    loadMyBorrowedBooks();
                } finally { returnButton.setEnabled(true); }
            }
        };
        worker.execute();
    }

    private void handleReissueBook() {
        int selectedRow = borrowedBooksTable.getSelectedRow();
        if (selectedRow == -1) { showWarning("Please select a book to reissue."); return; }
        int modelRow = borrowedBooksTable.convertRowIndexToModel(selectedRow);

        int issueId = (int) borrowedBooksModel.getValueAt(modelRow, 0);
        String currentStatus = (String) borrowedBooksModel.getValueAt(modelRow, 6);
        int reissueCount = (int) borrowedBooksModel.getValueAt(modelRow, 7);

        if (!"Issued".equalsIgnoreCase(currentStatus) && !"Overdue".equalsIgnoreCase(currentStatus)) {
            showWarning("Only 'Issued' or 'Overdue' books can be reissued."); return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Request a reissue for this book?", "Confirm Reissue", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        reissueButton.setEnabled(false);
        SwingWorker<LocalDate, Void> worker = new SwingWorker<>() {
            @Override protected LocalDate doInBackground() throws Exception {
                return studentService.reissueBook(issueId, reissueCount);
            }
            @Override protected void done() {
                try {
                    LocalDate newDueDate = get(); showSuccess("Book reissued successfully! New Due Date: " + newDueDate);
                    loadMyBorrowedBooks();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    showError("Error reissuing book: " + cause.getMessage());
                    loadMyBorrowedBooks();
                } finally { reissueButton.setEnabled(true); }
            }
        };
        worker.execute();
    }

    private void handleViewFine() {
        int selectedRow = borrowedBooksTable.getSelectedRow();
        if (selectedRow == -1) { showWarning("Please select a record to view fines."); return; }
        int modelRow = borrowedBooksTable.convertRowIndexToModel(selectedRow);
        int issueId = (int) borrowedBooksModel.getValueAt(modelRow, 0);

        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                return studentService.getUnpaidFineDetails(issueId);
            }
            @Override protected void done() {
                try {
                    List<Object[]> unpaidFines = get();
                    if (unpaidFines.isEmpty()) { showInfo("No unpaid fines found for this book issue."); }
                    else { showFineDetailsDialog(issueId, unpaidFines); }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    showError("Error checking fines: " + cause.getMessage());
                    cause.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void showFineDetailsDialog(int issueId, List<Object[]> unpaidFines) {
        StringBuilder fineDetails = new StringBuilder("Unpaid Fine Details for Issue ID " + issueId + ":\n");
        double totalFine = 0; List<Integer> fineIds = new ArrayList<>();
        for (Object[] fine : unpaidFines) {
            int fineId = (int) fine[0]; double amount = (double) fine[1]; Date fineDate = (Date) fine[2];
            fineDetails.append(String.format(" - Fine ID: %d, Amount: %.2f, Date: %s\n", fineId, amount, fineDate));
            totalFine += amount; fineIds.add(fineId);
        }
        fineDetails.append(String.format("\nTotal Unpaid Fine: %.2f", totalFine));
        int choice = JOptionPane.showConfirmDialog(this, fineDetails.toString() + "\n\nMark fine(s) as paid (Placeholder)?",
                "Fine Details", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) { markFinesAsPaid(fineIds); }
    }

    private void markFinesAsPaid(List<Integer> fineIds) {
        if (fineIds == null || fineIds.isEmpty()) return;
        viewFineButton.setEnabled(false);
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override protected Integer doInBackground() throws Exception {
                return studentService.markFinesPaid(fineIds);
            }
            @Override protected void done() {
                try {
                    int updatedCount = get();
                    if (updatedCount > 0) { showSuccess(updatedCount + " fine(s) marked as paid successfully!"); loadMyBorrowedBooks(); }
                    else { showInfo("Could not update fine status (already paid or issue?)."); loadMyBorrowedBooks(); }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    showError("Error during fine payment: " + cause.getMessage());
                    cause.printStackTrace();
                } finally { viewFineButton.setEnabled(true); }
            }
        };
        worker.execute();
    }

    private void handleSubmitRequest() {
        String title = requestTitleField.getText().trim(); String author = requestAuthorField.getText().trim(); String reason = requestReasonArea.getText().trim();
        if (title.isEmpty() || author.isEmpty()) { showWarning("Please enter Title and Author."); return; }
        submitRequestButton.setEnabled(false);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() throws Exception {
                return studentService.submitBookRequest(session.getUserId(), title, author, reason);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        showSuccess("Book request submitted successfully."); requestTitleField.setText(""); requestAuthorField.setText(""); requestReasonArea.setText("");
                    } else { showError("Failed to submit book request."); }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    showError("Error submitting request: " + cause.getMessage());
                    cause.printStackTrace();
                } finally { submitRequestButton.setEnabled(true); }
            }
        };
        worker.execute();
    }

    private void handleMarkNotificationRead() {
        int selectedRow = notificationsTable.getSelectedRow();
        if (selectedRow == -1) { showWarning("Please select a notification."); return; }
        int modelRow = notificationsTable.convertRowIndexToModel(selectedRow);
        final int notificationId; // Make final for use in worker
        if (modelRow < notificationIds.size()) { notificationId = notificationIds.get(modelRow); }
        else { showError("Error identifying selected notification."); return; }

        markReadButton.setEnabled(false);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() throws Exception {
                return studentService.markNotificationRead(notificationId, session.getUsername());
            }
            @Override protected void done() {
                try {
                    if (get()) { System.out.println("Notification " + notificationId + " marked read."); loadNotifications(); }
                    else { showInfo("Could not mark notification as read (already read or invalid?)."); loadNotifications(); }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    showError("Error marking notification read: " + cause.getMessage());
                    cause.printStackTrace();
                } finally { markReadButton.setEnabled(true); }
            }
        };
        worker.execute();
    }

    private void performLogout() {
        int confirmation = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            UserSession.clearInstance(); this.dispose(); SwingUtilities.invokeLater(LoginFrame::new);
        }
    }

    // --- Data Loading Methods (Use SwingWorker) ---
    private void loadAvailableBooks(String searchTerm) {
        availableBooksModel.setRowCount(0); availableBooksModel.addRow(new Object[]{"Loading...", "", "", "", "", ""});
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() throws Exception { return studentService.getAvailableBooks(searchTerm); }
            @Override protected void done() {
                try {
                    List<Object[]> books = get(); availableBooksModel.setRowCount(0);
                    if(books.isEmpty()){ System.out.println("No available books found."); } else { books.forEach(availableBooksModel::addRow); }
                } catch (Exception e) { handleLoadingError(e, "available books"); availableBooksModel.setRowCount(0); }
            }
        };
        worker.execute();
    }
    private void loadMyBorrowedBooks() {
        borrowedBooksModel.setRowCount(0); borrowedBooksModel.addRow(new Object[]{"Loading...", "", "", null, null, null, "", "", ""});
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() throws Exception { return studentService.getBorrowedBooks(session.getUserId()); }
            @Override protected void done() {
                try {
                    List<Object[]> books = get(); borrowedBooksModel.setRowCount(0);
                    if(books.isEmpty()){ System.out.println("No books currently borrowed."); } else { books.forEach(borrowedBooksModel::addRow); }
                } catch (Exception e) { handleLoadingError(e, "borrowed books"); borrowedBooksModel.setRowCount(0); }
            }
        };
        worker.execute();
    }
    private void loadNotifications() {
        notificationsModel.setRowCount(0); notificationIds.clear(); notificationsModel.addRow(new Object[]{"Loading...", "", "", ""});
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() throws Exception { return studentService.getNotifications(session.getUsername()); }
            @Override protected void done() {
                try {
                    List<Object[]> notifications = get(); notificationsModel.setRowCount(0); notificationIds.clear();
                    if(notifications.isEmpty()){ System.out.println("No notifications found."); }
                    else {
                        for (Object[] data : notifications) {
                            notificationIds.add((Integer) data[0]); // Store ID
                            notificationsModel.addRow(new Object[]{data[1], data[2], data[3], data[4]}); // Add display data
                        }
                    }
                } catch (Exception e) { handleLoadingError(e, "notifications"); notificationsModel.setRowCount(0); notificationIds.clear(); }
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

}