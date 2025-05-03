package library.frontend;

import library.backend.LibrarianService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class LibrarianDashboard extends JFrame implements ActionListener {

    private JButton logoutButton;

    public LibrarianDashboard() {
        setTitle("Librarian Dashboard - Tabbed Version");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Librarian Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.WEST);

        logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(120, 35));
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout(); // Call the logout functionality
            }
        });
        topPanel.add(logoutButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Main TabbedPane
        JTabbedPane mainTabbedPane = new JTabbedPane();

        // Add all tabs
        mainTabbedPane.addTab("Manage Books", createManageBooksTab());
        mainTabbedPane.addTab("Issue Books", createIssueBooksTab());
        mainTabbedPane.addTab("View Issued Books", createViewIssuedBooksTab());
        mainTabbedPane.addTab("Return Books", createReturnBooksTab());
        mainTabbedPane.addTab("Student Records", createStudentRecordsTab());
        mainTabbedPane.addTab("Overdue Notifications", createOverdueNotificationsTab());

        add(mainTabbedPane, BorderLayout.CENTER);
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
        JTextField categoryField = new JTextField(20); // Uncomment if category is needed
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
        addBookPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        addBookPanel.add(categoryField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        addBookPanel.add(new JLabel("Copies:"), gbc);
        gbc.gridx = 1;
        addBookPanel.add(copiesField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        addBookPanel.add(addButton, gbc);
        // Add action listener to add book
        addButton.addActionListener(e -> addBook(titleField.getText(), authorField.getText(),categoryField.getText(), Integer.parseInt(copiesField.getText())));


        // Sub-tab: View Books
        JPanel viewBooksPanel;
        JTable booksTable;
        DefaultTableModel booksTableModel;
        JButton refreshButton;


        viewBooksPanel = new JPanel(new BorderLayout(10, 10));
        viewBooksPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        String[] bookColumns = {"Book ID", "Title", "Author","Category", "Total Copies", "Available Copies"};
        booksTableModel = new DefaultTableModel(bookColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try {
            List<Object[]> books = LibrarianService.getAllBooks(null);
            for (Object[] book : books) {
                booksTableModel.addRow(book);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading books: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        booksTable = new JTable(booksTableModel);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        booksTable.setAutoCreateRowSorter(true);

        // Customize table appearance
        booksTable.getTableHeader().setReorderingAllowed(false);
        booksTable.setRowHeight(25);
        booksTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(booksTable);
        viewBooksPanel.add(scrollPane, BorderLayout.CENTER);

        // Create action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        refreshButton = new JButton("Refresh");


        refreshButton.setToolTipText("Refresh all data");
        refreshButton.addActionListener(e -> {
            try {
                List<Object[]> books = LibrarianService.getAllBooks(null);
                booksTableModel.setRowCount(0); // Clear existing rows
                for (Object[] book : books) {
                    booksTableModel.addRow(book);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading books: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        actionPanel.add(refreshButton);

        viewBooksPanel.add(actionPanel, BorderLayout.SOUTH);


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
        deleteButton.addActionListener(e -> deleteBook(bookIdField.getText()));

        // Add sub-tabs
        manageBooksTabs.addTab("Add Book", addBookPanel);
        manageBooksTabs.addTab("View Books", viewBooksPanel);
        manageBooksTabs.addTab("Delete Book", deleteBookPanel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(manageBooksTabs, BorderLayout.CENTER);
        return panel;
    }


    // Backend function: Add a new book to the database or list
    private void addBook(String title, String author,String category, int copies) {

        System.out.println("Adding Book");
        try {
            System.out.println("Inside try");
            LibrarianService.addBook(title.toUpperCase(), author.toUpperCase(),category.toUpperCase(), copies); // Call the backend service to add the book
        } catch (SQLException e) {
            System.out.print(e.getMessage());
        }
        //popup to show book added successfully
        JOptionPane.showMessageDialog(this, "Book added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        System.out.println("Adding Book: " + title + ", Author: " + author + ", Copies: " + copies);
    }

    // Backend function: Delete a book from the database or list
    private void deleteBook(String bookId) {
        try{
            LibrarianService.removeBook(bookId.toUpperCase()); // Call the backend service to delete the book
        } catch (SQLException e) {
            System.out.print(e.getMessage());
        }
        System.out.println("Deleting Book with ID: " + bookId);
        //popup to show book deleted successfully
        JOptionPane.showMessageDialog(this, "Book deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
        issueButton.addActionListener(e -> issueBook(studentIdField.getText().trim(),bookIdField.getText().trim()));

        return issuePanel;
    }

    // todo Backend function: Issue a book to a student
    private void issueBook(String studentId, String bookId) {
        // Placeholder for issuing a book
        try {
            System.out.println("Issuing Book with ID: " + bookId + " to Student with ID: " + studentId);
            LibrarianService.issueBookToStudent( bookId.toUpperCase(),studentId.toUpperCase()); // Call the backend service to issue the book
            //popup to show book issued successfully
            JOptionPane.showMessageDialog(this, "Book issued successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            System.out.print(e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("Issuing Book with ID: " + bookId + " to Student with ID: " + studentId);
    }

    // Tab 3: View Issued Books
    private JPanel createViewIssuedBooksTab() {
        List<Object[]> issuedBooks;
        try {
            issuedBooks = LibrarianService.getAllIssuedBooks(null);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading issued books: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return new JPanel();
        }

        JPanel viewIssuedPanel = new JPanel(new BorderLayout());
        viewIssuedPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        DefaultTableModel issuedBooksTableModel = new DefaultTableModel(new String[]{ "Book ID", "Student ID","Title","Issue Date", "Due Date","Return Date","Status"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Object[] issuedBook : issuedBooks) {
            issuedBooksTableModel.addRow(issuedBook);
        }
        // refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            try {
                List<Object[]> issued_Books = LibrarianService.getAllIssuedBooks(null);
                issuedBooksTableModel.setRowCount(0); // Clear existing rows
                for (Object[] issuedBook : issued_Books) {
                    issuedBooksTableModel.addRow(issuedBook);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading issued books: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        viewIssuedPanel.add(refreshButton, BorderLayout.NORTH);

        JTable issuedBooksTable = new JTable(issuedBooksTableModel);

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

        calculateFineButton.addActionListener(e->{
            try {
                double fine = LibrarianService.calculateFine( bookIdField.getText().trim().toUpperCase(),studentIdField.getText().trim().toUpperCase());
                fineLabel.setText("Fine: Rs. " + fine);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Add action listener to return book
        returnButton.addActionListener(e -> returnBook(studentIdField.getText().trim(), bookIdField.getText().trim()));

        return returnPanel;
    }


    // Backend function: Return a book and calculate fine
    private void returnBook(String studentId, String bookId) {
        try{
            System.out.println("Returning Book with ID: " + bookId + " from Student with ID: " + studentId);
            boolean success = LibrarianService.returnBook(bookId.toUpperCase(),studentId.toUpperCase());
            if(success)
                JOptionPane.showMessageDialog(null, "Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            else
                throw new SQLException("Error Occured while returning book");
        } catch (SQLException e) {
            System.out.print(e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    // Tab 5: Student Records
    private JPanel createStudentRecordsTab() {
        JPanel studentRecordsPanel = new JPanel(new BorderLayout());
        JTable studentsTable = new JTable(new DefaultTableModel(new String[]{"Student ID", "Name", "Email"}, 0));
        try{
            List<Object[]> students = LibrarianService.getAllStudents(null);
            DefaultTableModel studentsTableModel = (DefaultTableModel) studentsTable.getModel();
            for (Object[] student : students) {
                studentsTableModel.addRow(student);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading students: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Create action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setToolTipText("Refresh all data");
        refreshButton.addActionListener(e -> {
            try {
                List<Object[]> students = LibrarianService.getAllStudents(null);
                DefaultTableModel studentsTableModel = (DefaultTableModel) studentsTable.getModel();
                studentsTableModel.setRowCount(0); // Clear existing rows
                for (Object[] student : students) {
                    studentsTableModel.addRow(student);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading students: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        studentRecordsPanel.add(new JScrollPane(studentsTable), BorderLayout.CENTER);
        return studentRecordsPanel;
    }

    // Tab 6: Overdue Notifications
    private JPanel createOverdueNotificationsTab() {
        JPanel overduePanel = new JPanel(new BorderLayout());
        JTable overdueTable = new JTable(new DefaultTableModel(new String[]{"Book ID", "Title","Student ID", "Due Date"}, 0));

        try {
            List<Object[]> overdueBooks = LibrarianService.getOverdueBooks();
            DefaultTableModel overdueTableModel = (DefaultTableModel) overdueTable.getModel();
            for (Object[] overdueBook : overdueBooks) {
                overdueTableModel.addRow(overdueBook);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading overdue books: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);

        }
        JButton sendNotificationButton = new JButton("Send Notification");

        overduePanel.add(new JScrollPane(overdueTable), BorderLayout.CENTER);
        overduePanel.add(sendNotificationButton, BorderLayout.SOUTH);

        // Add action listener to send overdue notifications

        sendNotificationButton.addActionListener(e -> {
            try {
                List<Object[]> overdueBooks = LibrarianService.getOverdueBooks();

                for (Object[] overdueBook : overdueBooks) {
                    System.out.println("Sending notification for overdue book with ID: " + overdueBook[0]);
                    // Call the backend function to send notification
                    String message = "Dear Student, \n\nThe book '" + overdueBook[1] + "' (ID: " + overdueBook[0] + ") is overdue since " + overdueBook[3] + ". Please return it as soon as possible.\n\nThank you.";

                    LibrarianService.sendNotifications(overdueBook[2].toString(),message,"DueDate");

                }

                JOptionPane.showMessageDialog(this, "Overdue notifications sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException f) {
                JOptionPane.showMessageDialog(this,
                        "Error loading overdue books: " + f.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        });

        return overduePanel;
    }


    // Backend function: Logout functionality
    private void logout() {
        // Placeholder for logging out the user
        System.out.println("Logging out...");
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibrarianDashboard::new);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
    }
}