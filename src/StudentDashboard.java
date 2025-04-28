import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

public class StudentDashboard extends JFrame {

    public StudentDashboard() {
        setTitle("Student Dashboard");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main TabbedPane
        JTabbedPane mainTabbedPane = new JTabbedPane();

        // Add all tabs
        mainTabbedPane.addTab("Borrow Books", createBorrowBooksTab());
        mainTabbedPane.addTab("Return Books", createReturnBooksTab());
        mainTabbedPane.addTab("View Status", createViewStatusTab());
        mainTabbedPane.addTab("Request New Books", createRequestNewBooksTab());
        mainTabbedPane.addTab("Reissue Books", createReissueBooksTab());
        mainTabbedPane.addTab("View Notifications", createViewNotificationsTab());
        mainTabbedPane.addTab("Logout", createLogoutTab());

        add(mainTabbedPane);
        setVisible(true);
    }

    // Tab 1: Borrow Books
    private JPanel createBorrowBooksTab() {
        JPanel borrowPanel = new JPanel(new GridBagLayout());
        borrowPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField bookIdField = new JTextField(20);
        JButton borrowButton = new JButton("Borrow Book");

        gbc.gridx = 0; gbc.gridy = 0;
        borrowPanel.add(new JLabel("Book ID:"), gbc);
        gbc.gridx = 1;
        borrowPanel.add(bookIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        borrowPanel.add(borrowButton, gbc);

        // Add event listener for borrowing
        borrowButton.addActionListener(e -> borrowBook(bookIdField.getText()));

        return borrowPanel;
    }

    // Function to handle borrowing a book
    private void borrowBook(String bookId) {
        // Check if book is available and issue book (backend logic here)
        System.out.println("Borrowing book with ID: " + bookId);
        // Backend logic should verify availability and issue the book
    }

    // Tab 2: Return Books
    private JPanel createReturnBooksTab() {
        JPanel returnPanel = new JPanel(new BorderLayout());
        JTable borrowedBooksTable = new JTable(new DefaultTableModel(new String[]{"Book Title", "Issue Date", "Due Date", "Status"}, 0));
        returnPanel.add(new JScrollPane(borrowedBooksTable), BorderLayout.CENTER);

        JButton returnButton = new JButton("Return Book");
        returnButton.addActionListener(e -> returnBook(borrowedBooksTable));

        returnPanel.add(returnButton, BorderLayout.SOUTH);
        return returnPanel;
    }

    // Function to handle returning a book
    private void returnBook(JTable table) {
        // Handle book return logic (fetch selected book, update status, etc.)
        System.out.println("Returning selected book...");
    }

    // Tab 3: View Status
    private JPanel createViewStatusTab() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        JTable statusTable = new JTable(new DefaultTableModel(new String[]{"Book Title", "Issue Date", "Due Date", "Fine"}, 0));
        statusPanel.add(new JScrollPane(statusTable), BorderLayout.CENTER);
        return statusPanel;
    }

    // Tab 4: Request New Books
    private JPanel createRequestNewBooksTab() {
        JPanel requestPanel = new JPanel(new GridBagLayout());
        requestPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField titleField = new JTextField(20);
        JTextField authorField = new JTextField(20);
        JTextArea reasonArea = new JTextArea(5, 20);
        JButton requestButton = new JButton("Request Book");

        gbc.gridx = 0; gbc.gridy = 0;
        requestPanel.add(new JLabel("Book Title:"), gbc);
        gbc.gridx = 1;
        requestPanel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        requestPanel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1;
        requestPanel.add(authorField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        requestPanel.add(new JLabel("Reason for Request:"), gbc);
        gbc.gridx = 1;
        requestPanel.add(new JScrollPane(reasonArea), gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        requestPanel.add(requestButton, gbc);

        // Add event listener for requesting a book
        requestButton.addActionListener(e -> requestBook(titleField.getText(), authorField.getText(), reasonArea.getText()));

        return requestPanel;
    }

    // Function to handle requesting a new book
    private void requestBook(String title, String author, String reason) {
        // Backend logic to handle book request
        System.out.println("Requesting new book: " + title + " by " + author);
        // Add request to the database or send request to the library
    }

    // Tab 5: Reissue Books
    private JPanel createReissueBooksTab() {
        JPanel reissuePanel = new JPanel(new GridBagLayout());
        reissuePanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JComboBox<String> borrowedBooksComboBox = new JComboBox<>();
        JButton reissueButton = new JButton("Reissue Book");

        gbc.gridx = 0; gbc.gridy = 0;
        reissuePanel.add(new JLabel("Select Borrowed Book:"), gbc);
        gbc.gridx = 1;
        reissuePanel.add(borrowedBooksComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        reissuePanel.add(reissueButton, gbc);

        // Add event listener for reissuing a book
        reissueButton.addActionListener(e -> reissueBook((String) borrowedBooksComboBox.getSelectedItem()));

        return reissuePanel;
    }

    // Function to handle reissuing a book
    private void reissueBook(String bookTitle) {
        // Extend the due date of the selected book by 7 days
        System.out.println("Reissuing book: " + bookTitle);
        // Backend logic to extend the due date
    }

    // Tab 6: View Notifications
    private JPanel createViewNotificationsTab() {
        JPanel notificationsPanel = new JPanel(new BorderLayout());
        JTable notificationsTable = new JTable(new DefaultTableModel(new String[]{"Notification", "Date"}, 0));
        notificationsPanel.add(new JScrollPane(notificationsTable), BorderLayout.CENTER);
        return notificationsPanel;
    }

    // Tab 7: Logout
    private JPanel createLogoutTab() {
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 100));
        JButton logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(200, 50));
        logoutButton.addActionListener(e -> logout());
        logoutPanel.add(logoutButton);
        return logoutPanel;
    }

    // Function to handle logout
    private void logout() {
        // Logic to log out the user and return to login screen
        System.out.println("Logging out...");
        // Go back to the Login Screen
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudentDashboard::new);
    }
}
