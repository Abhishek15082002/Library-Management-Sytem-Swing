package library.frontend;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class LibrarianDashboard extends JFrame {

    public LibrarianDashboard() {
        setTitle("Librarian Dashboard - Tabbed Version");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main TabbedPane
        JTabbedPane mainTabbedPane = new JTabbedPane();

        // Add all tabs
        mainTabbedPane.addTab("Manage Books", createManageBooksTab());
        mainTabbedPane.addTab("Issue Books", createIssueBooksTab());
        mainTabbedPane.addTab("View Issued Books", createViewIssuedBooksTab());
        mainTabbedPane.addTab("Return Books", createReturnBooksTab());
        mainTabbedPane.addTab("Student Records", createStudentRecordsTab());
        mainTabbedPane.addTab("Overdue Notifications", createOverdueNotificationsTab());
        mainTabbedPane.addTab("Logout", createLogoutTab());

        add(mainTabbedPane);
        setVisible(true);
    }

    // Tab 1: Manage Books
    private JPanel createManageBooksTab() {
        JTabbedPane manageBooksTabs = new JTabbedPane();

        // Sub-tab: Add Book
        JPanel addBookPanel = new JPanel(new GridBagLayout());
        addBookPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField titleField = new JTextField(20);
        JTextField authorField = new JTextField(20);
        JTextField copiesField = new JTextField(20);
        JButton addButton = new JButton("Add Book");

        gbc.gridx = 0; gbc.gridy = 0;
        addBookPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        addBookPanel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        addBookPanel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1;
        addBookPanel.add(authorField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        addBookPanel.add(new JLabel("Copies:"), gbc);
        gbc.gridx = 1;
        addBookPanel.add(copiesField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        addBookPanel.add(addButton, gbc);

        // Add action listener to add book
        addButton.addActionListener(e -> addBook(titleField.getText(), authorField.getText(), Integer.parseInt(copiesField.getText())));

        // Sub-tab: View Books
        JPanel viewBooksPanel = new JPanel(new BorderLayout());
        JTable booksTable = new JTable(new DefaultTableModel(new String[]{"Book ID", "Title", "Author", "Copies"}, 0));
        viewBooksPanel.add(new JScrollPane(booksTable), BorderLayout.CENTER);

        // Sub-tab: Delete Book
        JPanel deleteBookPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(10, 10, 10, 10);
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.weightx = 1.0;

        JTextField bookIdField = new JTextField(20);
        JButton deleteButton = new JButton("Delete Book");

        gbc2.gridx = 0; gbc2.gridy = 0;
        deleteBookPanel.add(new JLabel("Book ID:"), gbc2);
        gbc2.gridx = 1;
        deleteBookPanel.add(bookIdField, gbc2);

        gbc2.gridx = 0; gbc2.gridy = 1;
        gbc2.gridwidth = 2;
        gbc2.anchor = GridBagConstraints.CENTER;
        deleteBookPanel.add(deleteButton, gbc2);

        // Add action listener to delete book
        deleteButton.addActionListener(e -> deleteBook(Integer.parseInt(bookIdField.getText())));

        // Add sub-tabs
        manageBooksTabs.addTab("Add Book", addBookPanel);
        manageBooksTabs.addTab("View Books", viewBooksPanel);
        manageBooksTabs.addTab("Delete Book", deleteBookPanel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(manageBooksTabs, BorderLayout.CENTER);
        return panel;
    }

    // Backend function: Add a new book to the database or list
    private void addBook(String title, String author, int copies) {
        // Placeholder for adding a book to the database or list
        System.out.println("Adding Book: " + title + ", Author: " + author + ", Copies: " + copies);
    }

    // Backend function: Delete a book from the database or list
    private void deleteBook(int bookId) {
        // Placeholder for deleting a book from the database or list
        System.out.println("Deleting Book with ID: " + bookId);
    }

    // Tab 2: Issue Books
    private JPanel createIssueBooksTab() {
        JPanel issuePanel = new JPanel(new GridBagLayout());
        issuePanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField studentIdField = new JTextField(20);
        JTextField bookIdField = new JTextField(20);
        JLabel issueDateLabel = new JLabel("Issue Date: " + LocalDate.now());
        JLabel dueDateLabel = new JLabel("Due Date: " + LocalDate.now().plusDays(14));
        JButton issueButton = new JButton("Issue Book");

        gbc.gridx = 0; gbc.gridy = 0;
        issuePanel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1;
        issuePanel.add(studentIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        issuePanel.add(new JLabel("Book ID:"), gbc);
        gbc.gridx = 1;
        issuePanel.add(bookIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        issuePanel.add(issueDateLabel, gbc);
        gbc.gridx = 1;
        issuePanel.add(dueDateLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        issuePanel.add(issueButton, gbc);

        // Add action listener to issue book
        issueButton.addActionListener(e -> issueBook(Integer.parseInt(studentIdField.getText()), Integer.parseInt(bookIdField.getText())));

        return issuePanel;
    }

    // Backend function: Issue a book to a student
    private void issueBook(int studentId, int bookId) {
        // Placeholder for issuing a book
        System.out.println("Issuing Book with ID: " + bookId + " to Student with ID: " + studentId);
    }

    // Tab 3: View Issued Books
    private JPanel createViewIssuedBooksTab() {
        JPanel viewIssuedPanel = new JPanel(new BorderLayout());
        JTable issuedBooksTable = new JTable(new DefaultTableModel(new String[]{"Student ID", "Book ID", "Issue Date", "Due Date", "Status"}, 0));
        viewIssuedPanel.add(new JScrollPane(issuedBooksTable), BorderLayout.CENTER);
        return viewIssuedPanel;
    }

    // Tab 4: Return Books
    private JPanel createReturnBooksTab() {
        JPanel returnPanel = new JPanel(new GridBagLayout());
        returnPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField studentIdField = new JTextField(20);
        JTextField bookIdField = new JTextField(20);
        JLabel fineLabel = new JLabel("Fine: Rs. 0");
        JButton calculateFineButton = new JButton("Calculate Fine");
        JButton returnButton = new JButton("Return Book");

        gbc.gridx = 0; gbc.gridy = 0;
        returnPanel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1;
        returnPanel.add(studentIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        returnPanel.add(new JLabel("Book ID:"), gbc);
        gbc.gridx = 1;
        returnPanel.add(bookIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        returnPanel.add(calculateFineButton, gbc);
        gbc.gridx = 1;
        returnPanel.add(fineLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        returnPanel.add(returnButton, gbc);

        // Add action listener to return book
        returnButton.addActionListener(e -> returnBook(Integer.parseInt(studentIdField.getText()), Integer.parseInt(bookIdField.getText())));

        return returnPanel;
    }

    // Backend function: Return a book and calculate fine
    private void returnBook(int studentId, int bookId) {
        // Placeholder for returning a book and calculating fine
        System.out.println("Returning Book with ID: " + bookId + " from Student with ID: " + studentId);
    }

    // Tab 5: Student Records
    private JPanel createStudentRecordsTab() {
        JPanel studentRecordsPanel = new JPanel(new BorderLayout());
        JTable studentsTable = new JTable(new DefaultTableModel(new String[]{"Student ID", "Name", "Email", "Phone"}, 0));
        studentRecordsPanel.add(new JScrollPane(studentsTable), BorderLayout.CENTER);
        return studentRecordsPanel;
    }

    // Tab 6: Overdue Notifications
    private JPanel createOverdueNotificationsTab() {
        JPanel overduePanel = new JPanel(new BorderLayout());
        JTable overdueTable = new JTable(new DefaultTableModel(new String[]{"Student ID", "Book ID", "Due Date"}, 0));
        JButton sendNotificationButton = new JButton("Send Email Notification");

        overduePanel.add(new JScrollPane(overdueTable), BorderLayout.CENTER);
        overduePanel.add(sendNotificationButton, BorderLayout.SOUTH);

        // Add action listener to send overdue notifications
        sendNotificationButton.addActionListener(e -> sendOverdueNotification());

        return overduePanel;
    }

    // Backend function: Send overdue notifications to students
    private void sendOverdueNotification() {
        // Placeholder for sending overdue email notifications
        System.out.println("Sending overdue notifications...");
    }

    // Tab 7: Logout
    private JPanel createLogoutTab() {
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 100));
        JButton logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(200, 50));
        logoutPanel.add(logoutButton);

        // Add action listener to handle logout
        logoutButton.addActionListener(e -> logout());

        return logoutPanel;
    }

    // Backend function: Logout functionality
    private void logout() {
        // Placeholder for logging out the user
        System.out.println("Logging out...");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibrarianDashboard::new);
    }
}
