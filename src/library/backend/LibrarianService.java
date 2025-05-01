package library.backend;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import library.DatabaseConnection;

public class LibrarianService {

    private String getSettingValue(String key, String defaultValue) {
        String sql = "SELECT setting_value FROM Settings WHERE setting_key = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection(); // Should resolve now
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed."); }
            // ... rest of method ...
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, key);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String value = rs.getString("setting_value");
                        return (value != null) ? value : defaultValue;
                    }
                }
            }
        } catch (SQLException e) { System.err.println("DB Error retrieving setting '" + key + "'. Using default."); }
        return defaultValue;
    }

    private int getSettingValueInt(String key, int defaultValue) {
        try { return Integer.parseInt(getSettingValue(key, String.valueOf(defaultValue))); }
        catch (NumberFormatException e) { System.err.println("Invalid int format for setting '" + key + "'. Using default."); return defaultValue; }
    }

    private double getSettingValueDouble(String key, double defaultValue) {
        try { return Double.parseDouble(getSettingValue(key, String.valueOf(defaultValue))); }
        catch (NumberFormatException e) { System.err.println("Invalid double format for setting '" + key + "'. Using default."); return defaultValue; }
    }

    public List<Object[]> getAllBooks(String librarianId,String searchTerm) throws SQLException {
        List<Object[]> books = new ArrayList<>();
        // ... (rest of method implementation using DatabaseConnection) ...
        String sql = "SELECT book_id, title, author, category, total_copies, available_copies " +
                "FROM Books ";
        boolean searching = searchTerm != null && !searchTerm.trim().isEmpty();
        if (searching) {
            sql += "AND (LOWER(title) LIKE LOWER(?) OR LOWER(author) LIKE LOWER(?) OR LOWER(category) LIKE LOWER(?))";
        }
        sql += " ORDER BY title";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection(); // Should resolve now
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Database connection failed or is closed.");
            }
            // ... rest of try-with-resources ...
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (searching) {
                    String searchPattern = "%" + searchTerm.trim() + "%";
                    pstmt.setString(1, searchPattern);
                    pstmt.setString(2, searchPattern);
                    pstmt.setString(3, searchPattern);
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        books.add(new Object[]{
                                rs.getString("book_id"), rs.getString("title"), rs.getString("author"),
                                rs.getString("category"), rs.getDouble("total_copies"), rs.getInt("available_copies")
                        });
                    }
                }

                //update activity log

            }
        } finally {
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return books;

    }

    public List<Object[]> getAllIssuedBooks(String searchTerm) throws SQLException {
        List<Object[]> issuedBooks = new ArrayList<>();

        String sql = "SELECT i.issue_id, b.book_id, b.title, s.student_id, " +
                "i.issue_date, i.due_date, i.return_date, i.status " +
                "FROM Issues i " +
                "JOIN Books b ON i.book_id = b.book_id " +
                "JOIN Students s ON i.student_id = s.student_id " +
                "WHERE i.status IN ('Issued', 'Overdue') ";

        boolean searching = searchTerm != null && !searchTerm.trim().isEmpty();
        if (searching) {
            sql += "AND (LOWER(b.title) LIKE LOWER(?) OR LOWER(b.author) LIKE LOWER(?) " +
                    "OR LOWER(s.student_id) LIKE LOWER(?) OR LOWER(s.name) LIKE LOWER(?)) ";
        }

        sql += "ORDER BY i.due_date ASC";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Database connection failed or is closed.");
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (searching) {
                    String searchPattern = "%" + searchTerm.trim() + "%";
                    pstmt.setString(1, searchPattern);
                    pstmt.setString(2, searchPattern);
                    pstmt.setString(3, searchPattern);
                    pstmt.setString(4, searchPattern);
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        issuedBooks.add(new Object[]{
                                rs.getString("book_id"),
                                rs.getString("student_id"),
                                rs.getString("title"),
                                rs.getDate("issue_date"),
                                rs.getDate("due_date"),
                                rs.getDate("return_date"),
                                rs.getString("status")
                        });
                    }
                }
            }
        } finally {
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return issuedBooks;
    }

    public boolean issueBookToStudent(String bookId, String studentId) throws SQLException {
        Connection conn = null;
        int borrowingPeriodDays = 14;
        boolean autoCommitOriginal = false;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Database connection failed or is closed.");
            }

            // Save original auto-commit setting and disable it for transaction
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // Check if book exists and is available
            String checkBookSql = "SELECT available_copies FROM Books WHERE book_id = ?";
            try (PreparedStatement checkBookStmt = conn.prepareStatement(checkBookSql)) {
                checkBookStmt.setString(1, bookId);
                try (ResultSet rs = checkBookStmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Book with ID " + bookId + " does not exist.");
                    }
                    int availableCopies = rs.getInt("available_copies");
                    if (availableCopies <= 0) {
                        throw new SQLException("Book with ID " + bookId + " is not available for borrowing.");
                    }
                }
            }

            // Check if student exists
            String checkStudentSql = "SELECT 1 FROM Students WHERE student_id = ?";
            try (PreparedStatement checkStudentStmt = conn.prepareStatement(checkStudentSql)) {
                checkStudentStmt.setString(1, studentId);
                try (ResultSet rs = checkStudentStmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Student with ID " + studentId + " does not exist.");
                    }
                }
            }
            String checkDuePeriodSql = "SELECT setting_value FROM Settings WHERE setting_key = 'DefaultBorrowingPeriodDays'";
            //execute the query
            try (PreparedStatement checkDuePeriodStmt = conn.prepareStatement(checkDuePeriodSql)) {
                try (ResultSet rs = checkDuePeriodStmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Default borrowing period not found.");
                    }
                    borrowingPeriodDays = rs.getInt("setting_value");

                }
            }


            // Issue the book
            String issueSql = "INSERT INTO IssuedBooks (book_id, student_id, issue_date, due_date, status,reissue_count) VALUES (?, ?, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), 'Issued',0)";
            try (PreparedStatement issueStmt = conn.prepareStatement(issueSql)) {
                issueStmt.setString(1, bookId);
                issueStmt.setString(2, studentId);
                issueStmt.executeUpdate();
            }


            // Update the book availability
            String updateBookSql = "UPDATE Books SET available_copies = available_copies - 1 WHERE book_id = ?";
            try (PreparedStatement updateBookStmt = conn.prepareStatement(updateBookSql)) {
                updateBookStmt.setString(1, bookId);
                updateBookStmt.executeUpdate();
            }


            // If everything succeeded, commit the transaction
            conn.commit();
            return true;

        } catch (SQLException ex) {
            // If anything failed, roll back
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            throw ex; // Re-throw the exception
        } finally {
            // Restore original auto-commit setting and close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(autoCommitOriginal);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean returnBook( String bookId, String studentId) throws SQLException {
        Connection conn = null;
        boolean autoCommitOriginal = false;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Database connection failed or is closed.");
            }

            // Save original auto-commit setting and disable it for transaction
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);
            int issue_id;
            // Check if the book is issued to the student
            String checkIssuedSql = "SELECT * FROM IssuedBooks WHERE book_id = ? AND student_id = ? AND status = 'Issued'";
            try (PreparedStatement checkIssuedStmt = conn.prepareStatement(checkIssuedSql)) {
                checkIssuedStmt.setString(1, bookId);
                checkIssuedStmt.setString(2, studentId);
                try (ResultSet rs = checkIssuedStmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Book with ID " + bookId + " is not issued to student with ID " + studentId + ".");
                    }
                    issue_id = rs.getInt("issue_id");
                }
            }

            // Update the status of the issued book to 'Returned'
            String updateIssuedSql = "UPDATE IssuedBooks SET status = 'Returned', return_date = CURRENT_DATE WHERE  book_id = ? AND issue_id =? AND student_id = ?";
            try (PreparedStatement updateIssuedStmt = conn.prepareStatement(updateIssuedSql)) {
                updateIssuedStmt.setString(1, bookId);
                updateIssuedStmt.setString(2, String.valueOf(issue_id));
                updateIssuedStmt.setString(3, studentId);
                updateIssuedStmt.executeUpdate();
            }

            // Update the available copies of the book
            String updateBookSql = "UPDATE Books SET available_copies = available_copies + 1 WHERE book_id = ?";
            try (PreparedStatement updateBookStmt = conn.prepareStatement(updateBookSql)) {
                updateBookStmt.setString(1, bookId);
                updateBookStmt.executeUpdate();
            }

            // If everything succeeded, commit the transaction
            conn.commit();
            return true;

        } catch (SQLException ex) {
            // If anything failed, roll back
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            throw ex; // Re-throw the exception
        } finally {
            // Restore original auto-commit setting and close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(autoCommitOriginal);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean reissueBook(String bookId, String studentId) throws SQLException {
        Connection conn = null;
        boolean autoCommitOriginal = false;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Database connection failed or is closed.");
            }

            // Save original auto-commit setting and disable it for transaction
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // Check if the book is issued to the student
            String checkIssuedSql = "SELECT * FROM IssuedBooks WHERE book_id = ? AND student_id = ? AND status = 'Issued'";
            try (PreparedStatement checkIssuedStmt = conn.prepareStatement(checkIssuedSql)) {
                checkIssuedStmt.setString(1, bookId);
                checkIssuedStmt.setString(2, studentId);
                try (ResultSet rs = checkIssuedStmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Book with ID " + bookId + " is not issued to student with ID " + studentId + ".");
                    }
                }
            }

            // Check if the book is overdue
            String checkOverdueSql = "SELECT DATEDIFF(CURRENT_DATE, due_date) AS overdue_days FROM IssuedBooks WHERE book_id = ? AND student_id = ? AND status = 'Issued'";
            try (PreparedStatement checkOverdueStmt = conn.prepareStatement(checkOverdueSql)) {
                checkOverdueStmt.setString(1, bookId);
                checkOverdueStmt.setString(2, studentId);
                try (ResultSet rs = checkOverdueStmt.executeQuery()) {
                    if (rs.next()) {
                        int overdueDays = rs.getInt("overdue_days");
                        if (overdueDays > 0) {
                            throw new SQLException("Book with ID " + bookId + " is overdue by " + overdueDays + " days.");
                        }
                    }
                }
            }

            // check if it has reached maximum reissue count
            String checkReissueCountSql = "SELECT reissue_count FROM IssuedBooks WHERE book_id = ? AND student_id = ?";
            try (PreparedStatement checkReissueCountStmt = conn.prepareStatement(checkReissueCountSql)) {
                checkReissueCountStmt.setString(1, bookId);
                checkReissueCountStmt.setString(2, studentId);
                checkReissueCountStmt.executeQuery();
                try (ResultSet rs = checkReissueCountStmt.executeQuery()) {
                    if (rs.next()) {
                        int reissueCount = rs.getInt("reissue_count");
                        if (reissueCount >= getSettingValueInt("MaxReissuesAllowed",2)) {
                            throw new SQLException("Book with ID " + bookId + " has reached the maximum reissue limit.");
                        }
                    }
                }


            }

            // Update the due date of the issued book
            String updateDueDateSql = "UPDATE IssuedBooks SET due_date = DATE_ADD(due_date, INTERVAL 14 DAY), reissue_count=reissue_count+1 WHERE book_id = ? AND student_id = ?";
            try (PreparedStatement updateDueDateStmt = conn.prepareStatement(updateDueDateSql)) {
                updateDueDateStmt.setString(1, bookId);
                updateDueDateStmt.setString(2, studentId);
                updateDueDateStmt.executeUpdate();
            }

            // If everything succeeded, commit the transaction
            conn.commit();
            return true;

        } catch (SQLException ex) {
            // If anything failed, roll back
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            throw ex; // Re-throw the exception
        } finally {
            // Restore original auto-commit setting and close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(autoCommitOriginal);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean addBook(String bookId, String title, String author, String category, int totalCopies) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Database connection failed or is closed.");
            }

            String sql = "INSERT INTO Books (book_id, title, author, category, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, bookId);
                pstmt.setString(2, title);
                pstmt.setString(3, author);
                pstmt.setString(4, category);
                pstmt.setInt(5, totalCopies);
                pstmt.setInt(6, totalCopies); // Initially available copies are the same as total copies
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        } finally {
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Object[]> getAllStudents( String searchTerm) throws SQLException {
        List<Object[]> students = new ArrayList<>();
        String sql = "SELECT s.student_id, s.name, u.email " +
                "FROM Students s JOIN Users u ON s.student_id = u.username " +
                "ORDER BY s.student_id";

        boolean searching = searchTerm != null && !searchTerm.trim().isEmpty();
        if (searching) {
            sql += "AND (LOWER(student_id) LIKE LOWER(?) OR LOWER(name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?))";
        }
        sql += " ORDER BY name";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Database connection failed or is closed.");
            }
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    students.add(new Object[]{
                            rs.getString("student_id"), rs.getString("name"),
                            rs.getString("email")
                    });
                }
            }
        } finally {
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return students;
    }


    private LocalDate calculateDueDate(LocalDate issueDate) {
        int borrowPeriod = getSettingValueInt("SETTING_BORROW_PERIOD", 14 );
        return issueDate.plusDays(borrowPeriod);
    }

    public boolean removeBook(String bookId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Database connection failed or is closed.");
            }
            //check if available copies and issued copies are equal before deleting
            String checkCopiesSql = "SELECT total_copies, available_copies FROM Books WHERE book_id = ?";
            try (PreparedStatement checkCopiesStmt = conn.prepareStatement(checkCopiesSql)) {
                checkCopiesStmt.setString(1, bookId);
                try (ResultSet rs = checkCopiesStmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Book with ID " + bookId + " does not exist.");
                    }
                    int totalCopies = rs.getInt("total_copies");
                    int availableCopies = rs.getInt("available_copies");
                    if (totalCopies != availableCopies) {
                        throw new SQLException("Book with ID " + bookId + " cannot be deleted because it is currently issued to students.");
                    }
                }
            }

            String sql = "DELETE FROM Books WHERE book_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, bookId);
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        } finally {
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}