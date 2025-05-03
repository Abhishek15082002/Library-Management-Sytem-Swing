package library.backend; 
import library.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Backend service class for handling student-related database operations and logic.
 */
public class StudentService {

    // Constants for Settings keys
    private static final String SETTING_BORROW_PERIOD = "DefaultBorrowingPeriodDays";
    private static final String SETTING_FINE_RATE = "FinePerDay";
    private static final String SETTING_MAX_REISSUES = "MaxReissuesAllowed";
    private static final int DEFAULT_BORROW_PERIOD = 14;
    private static final double DEFAULT_FINE_RATE = 1.0;
    private static final int DEFAULT_MAX_REISSUES = 2;

    // --- Methods like getAvailableBooks, getBorrowedBooks, getNotifications ---
    public List<Object[]> getAvailableBooks(String searchTerm) throws SQLException {
        List<Object[]> books = new ArrayList<>();
        String sql = "SELECT book_id, title, author, category, avg_rating, available_copies " +
                     "FROM Books WHERE available_copies > 0 ";
        boolean searching = searchTerm != null && !searchTerm.trim().isEmpty();
        if (searching) {
            sql += "AND (LOWER(title) LIKE LOWER(?) OR LOWER(author) LIKE LOWER(?) OR LOWER(category) LIKE LOWER(?))";
        }
        sql += " ORDER BY title";

        Connection conn = null;
        try {
             conn = DatabaseConnection.getConnection(); // Should resolve now
             if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed or is closed."); }
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
                            rs.getString("category"), rs.getDouble("avg_rating"), rs.getInt("available_copies")
                        });
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available books: " + e.getMessage());
            throw e; 
        }
        return books;

    }

     public List<Object[]> getBorrowedBooks(String studentId) throws SQLException {
        List<Object[]> borrowedBooks = new ArrayList<>();
        
         String sql = "SELECT i.issue_id, i.book_id, b.title, i.issue_date, i.due_date, i.return_date, i.status, i.reissue_count, f.fine_amount " +
                     "FROM IssuedBooks i JOIN Books b ON i.book_id = b.book_id " +
                     "LEFT JOIN Fines f ON i.issue_id = f.issue_id AND f.status = 'Unpaid' " +
                     "WHERE i.student_id = ? ORDER BY i.status ASC, i.due_date ASC";

        Connection conn = null;
        try {
             conn = DatabaseConnection.getConnection(); // Should resolve now
             if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed or is closed."); }
            
              try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, studentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Double fineAmount = rs.getObject("fine_amount") != null ? rs.getDouble("fine_amount") : null;
                        borrowedBooks.add(new Object[]{
                            rs.getInt("issue_id"), rs.getString("book_id"), rs.getString("title"),
                            rs.getDate("issue_date"), rs.getDate("due_date"), rs.getDate("return_date"),
                            rs.getString("status"), rs.getInt("reissue_count"),
                            (fineAmount != null) ? String.format("%.2f", fineAmount) : "N/A"
                        });
                    }
                }
            }
        } finally {
            // Manage connection if necessary
        }
        return borrowedBooks;
    }

    public List<Object[]> getNotifications(String username) throws SQLException {
        List<Object[]> notifications = new ArrayList<>();
         String sql = "SELECT notification_id, created_at, type, message, is_read " +
                     "FROM Notifications WHERE user_id = ? ORDER BY created_at DESC";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection(); // Should resolve now
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed or is closed."); }
             try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    java.text.SimpleDateFormat timestampFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
                    while (rs.next()) {
                        int id = rs.getInt("notification_id");
                        Timestamp createdAt = rs.getTimestamp("created_at");
                        String formattedDate = (createdAt != null) ? timestampFormat.format(createdAt) : "N/A";
                        notifications.add(new Object[]{
                            id, // Include ID for marking as read
                            formattedDate,
                            rs.getString("type"),
                            rs.getString("message"),
                            rs.getBoolean("is_read") ? "Yes" : "No"
                        });
                    }
                }
            }
        } finally {
            // Manage connection if necessary
        }
        return notifications;
    }

    // --- Action Methods (borrowBook, returnBook, etc.) ---
    // (Ensure they use DatabaseConnection correctly and throw the public exceptions)
     public String borrowBook(String studentId, String bookId) throws SQLException, BorrowException {
        Connection conn = null;
        LocalDate dueDate = null;
        try {
            conn = DatabaseConnection.getConnection(); // Should resolve now
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed or is closed."); }
             conn.setAutoCommit(false); // Start transaction

            // 1. Check availability (lock row)
            String checkSql = "SELECT available_copies FROM Books WHERE book_id = ? FOR UPDATE";
            int availableCopies = 0;
            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setString(1, bookId);
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (rs.next()) {
                        availableCopies = rs.getInt("available_copies");
                    } else { throw new SQLException("Book ID " + bookId + " not found."); }
                }
            }

            if (availableCopies <= 0) {
                conn.rollback(); // Rollback before throwing custom exception
                throw new BorrowException("Sorry, this book is no longer available.");
            }

            // 2. Decrement available_copies
            String updateBookSql = "UPDATE Books SET available_copies = available_copies - 1 WHERE book_id = ? AND available_copies > 0";
            try (PreparedStatement updateBookPstmt = conn.prepareStatement(updateBookSql)) {
                updateBookPstmt.setString(1, bookId);
                if (updateBookPstmt.executeUpdate() == 0) { throw new SQLException("Failed to update book copies (concurrent update?)."); }
            }

            // 3. Insert into IssuedBooks
            String insertIssueSql = "INSERT INTO IssuedBooks (student_id, book_id, issue_date, due_date, status, reissue_count) VALUES (?, ?, ?, ?, 'Issued', 0)";
            LocalDate issueDate = LocalDate.now();
            dueDate = calculateDueDate(issueDate); // Calculate due date
            try (PreparedStatement insertIssuePstmt = conn.prepareStatement(insertIssueSql)) {
                insertIssuePstmt.setString(1, studentId);
                insertIssuePstmt.setString(2, bookId);
                insertIssuePstmt.setDate(3, Date.valueOf(issueDate));
                insertIssuePstmt.setDate(4, Date.valueOf(dueDate));
                if (insertIssuePstmt.executeUpdate() == 0) { throw new SQLException("Failed to insert book issue record."); }
            }

            conn.commit(); // Commit transaction
            return "Book borrowed successfully! Due Date: " + dueDate;

        } catch (SQLException | BorrowException ex) {
            if (conn != null) { try { if (!conn.isClosed()) { conn.rollback(); } } catch (SQLException rollbackEx) { System.err.println("CRITICAL: Error during transaction rollback: " + rollbackEx.getMessage()); } }
            throw ex; // Re-throw
        } finally {
            if (conn != null) { try { if (!conn.isClosed()) { conn.setAutoCommit(true); } } catch (SQLException closeEx) { System.err.println("Error resetting auto-commit: " + closeEx.getMessage()); } }
        }
     }

     public String returnBook(int issueId, String bookId, String studentId, LocalDate dueDate) throws SQLException, ReturnException {
         Connection conn = null;
        String fineMessage = "";
        try {
            conn = DatabaseConnection.getConnection(); // Should resolve now
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed or is closed."); }
            conn.setAutoCommit(false); // Start transaction

            LocalDate returnDate = LocalDate.now();

            // 1. Update IssuedBooks status
            String updateIssueSql = "UPDATE IssuedBooks SET status = 'Returned', return_date = ? WHERE issue_id = ? AND status != 'Returned'"; // Prevent double returns
            try (PreparedStatement updateIssuePstmt = conn.prepareStatement(updateIssueSql)) {
                updateIssuePstmt.setDate(1, Date.valueOf(returnDate));
                updateIssuePstmt.setInt(2, issueId);
                if (updateIssuePstmt.executeUpdate() == 0) {
                    throw new ReturnException("Book might already be returned or issue ID is invalid.");
                }
            }

            // 2. Increment available_copies
            String updateBookSql = "UPDATE Books SET available_copies = available_copies + 1 WHERE book_id = ?";
            try (PreparedStatement updateBookPstmt = conn.prepareStatement(updateBookSql)) {
                updateBookPstmt.setString(1, bookId);
                 if (updateBookPstmt.executeUpdate() == 0) {
                    System.err.println("Warning: Could not increment copies for book ID " + bookId + ". Book might be deleted.");
                 }
            }

            // 3. Calculate and record fine if applicable
            double fineAmount = 0;
            if (dueDate != null && returnDate.isAfter(dueDate)) {
                fineAmount = calculateFine(dueDate, returnDate);
            }

            if (fineAmount > 0) {
                upsertFine(conn, issueId, studentId, fineAmount, returnDate); // Use helper
                fineMessage = String.format(" A fine of %.2f has been applied.", fineAmount);
            }

            conn.commit(); // Commit transaction
            return "Book returned successfully!" + fineMessage;

        } catch (SQLException | ReturnException ex) {
            if (conn != null) { try { if (!conn.isClosed()) { conn.rollback(); } } catch (SQLException e) { e.printStackTrace(); } }
            throw ex; // Re-throw
        } finally {
            if (conn != null) { try { if (!conn.isClosed()) { conn.setAutoCommit(true); } } catch (SQLException e) { e.printStackTrace(); } }
        }
     }

     public LocalDate reissueBook(int issueId, int reissueCount) throws SQLException, ReissueException {
         // --- Eligibility Checks ---
        if (hasUnpaidFine(issueId)) {
            throw new ReissueException("Cannot reissue book with an outstanding fine.");
        }
        int maxReissues = getSettingValueInt(SETTING_MAX_REISSUES, DEFAULT_MAX_REISSUES);
        if (reissueCount >= maxReissues) {
            throw new ReissueException("Maximum reissue limit (" + maxReissues + ") reached.");
        }
        // --- End Checks ---

        Connection conn = null;
        try {
             conn = DatabaseConnection.getConnection(); // Should resolve now
             if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed or is closed."); }
             LocalDate newDueDate = calculateDueDate(LocalDate.now()); // New due date from today

            // Update due date, increment reissue count, ensure status is 'Issued'
            String sql = "UPDATE IssuedBooks SET due_date = ?, reissue_count = reissue_count + 1, status = 'Issued' " +
                         "WHERE issue_id = ? AND status IN ('Issued', 'Overdue')"; // Only update if status allows
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDate(1, Date.valueOf(newDueDate));
                pstmt.setInt(2, issueId);

                if (pstmt.executeUpdate() > 0) {
                    return newDueDate; // Return new due date on success
                } else {
                     throw new ReissueException("Failed to reissue book (status changed or record not found?).");
                }
            }
        } catch (SQLException | ReissueException ex) {
            throw ex; // Re-throw
        }
     }

     // ... other action methods (getUnpaidFineDetails, markFinesPaid, submitBookRequest, markNotificationRead) ...
     // Ensure they also use DatabaseConnection correctly. Example:
      public List<Object[]> getUnpaidFineDetails(int issueId) throws SQLException {
        List<Object[]> fineDetails = new ArrayList<>();
        String sql = "SELECT fine_id, fine_amount, fine_date FROM Fines WHERE issue_id = ? AND status = 'Unpaid'";
        Connection conn = null;
        try {
             conn = DatabaseConnection.getConnection(); // Should resolve now
             if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed or is closed."); }
             try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, issueId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        fineDetails.add(new Object[]{
                            rs.getInt("fine_id"),
                            rs.getDouble("fine_amount"),
                            rs.getDate("fine_date")
                        });
                    }
                }
            }
        } finally {
            // Manage connection if necessary
        }
        return fineDetails;
    }

     public int markFinesPaid(List<Integer> fineIds) throws SQLException {
         if (fineIds == null || fineIds.isEmpty()) { return 0; }
         StringBuilder sqlBuilder = new StringBuilder("UPDATE Fines SET status = 'Paid' WHERE fine_id IN (");
        for (int i = 0; i < fineIds.size(); i++) {
            sqlBuilder.append("?");
            if (i < fineIds.size() - 1) { sqlBuilder.append(","); }
        }
        sqlBuilder.append(")");
        String sql = sqlBuilder.toString();

        Connection conn = null;
        int updatedCount = 0;
        try {
            conn = DatabaseConnection.getConnection(); // Should resolve now
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed or is closed."); }
             conn.setAutoCommit(false); // Use transaction

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < fineIds.size(); i++) {
                    pstmt.setInt(i + 1, fineIds.get(i));
                }
                updatedCount = pstmt.executeUpdate();
            }

            if (updatedCount != fineIds.size()) {
                 conn.rollback();
                 System.err.println("Warning: Expected to update " + fineIds.size() + " fines, but updated " + updatedCount + ". Rolling back.");
                 throw new SQLException("Failed to update all specified fines.");
            }
            conn.commit(); // Commit if successful
            return updatedCount;
        } catch (SQLException ex) {
            if (conn != null) { try { if (!conn.isClosed()) { conn.rollback(); } } catch (SQLException e) { e.printStackTrace(); } }
            throw ex; // Re-throw
        } finally {
            if (conn != null) { try { if (!conn.isClosed()) { conn.setAutoCommit(true); } } catch (SQLException e) { e.printStackTrace(); } }
        }
     }

      public boolean submitBookRequest(String studentId, String title, String author, String reason) throws SQLException {
        String sql = "INSERT INTO BookRequests (student_id, title, author, reason, status, request_date) VALUES (?, ?, ?, ?, 'Pending', NOW())";
        Connection conn = null;
        try {
             conn = DatabaseConnection.getConnection(); // Should resolve now
             if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed or is closed."); }
              try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, studentId);
                pstmt.setString(2, title);
                pstmt.setString(3, author);
                pstmt.setString(4, reason.isEmpty() ? null : reason);
                return pstmt.executeUpdate() > 0; // Return true if insert succeeded
            }
        } finally {
            // Manage connection if necessary
        }
    }

     public boolean markNotificationRead(int notificationId, String username) throws SQLException {
        String sql = "UPDATE Notifications SET is_read = TRUE WHERE notification_id = ? AND user_id = ?";
        Connection conn = null;
        try {
             conn = DatabaseConnection.getConnection(); // Should resolve now
             if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed or is closed."); }
             try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, notificationId);
                pstmt.setString(2, username);
                return pstmt.executeUpdate() > 0; // Return true if update succeeded
            }
        } finally {
             // Manage connection if necessary
        }
    }


    // --- Helper Methods (calculateDueDate, calculateFine, hasUnpaidFine, getSettingValue...) ---
    // (Ensure they use DatabaseConnection correctly)
     private boolean hasUnpaidFine(int issueId) {
        String sql = "SELECT 1 FROM Fines WHERE issue_id = ? AND status = 'Unpaid' LIMIT 1";
        Connection conn = null;
        try {
             conn = DatabaseConnection.getConnection(); // Should resolve now
             if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed."); }
             try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, issueId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next(); // Returns true if any row exists
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking for unpaid fine: " + e.getMessage());
            return false;
        }
    }
     private LocalDate calculateDueDate(LocalDate issueDate) {
        int borrowPeriod = getSettingValueInt(SETTING_BORROW_PERIOD, DEFAULT_BORROW_PERIOD);
        return issueDate.plusDays(borrowPeriod);
    }
     private double calculateFine(LocalDate dueDate, LocalDate returnDate) {
        if (dueDate != null && returnDate != null && returnDate.isAfter(dueDate)) {
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
            double fineRate = getSettingValueDouble(SETTING_FINE_RATE, DEFAULT_FINE_RATE);
            return Math.max(0, daysOverdue * fineRate);
        }
        return 0.0;
    }
    private void upsertFine(Connection conn, int issueId, String studentId, double fineAmount, LocalDate fineDate) throws SQLException {
        // Using SELECT then INSERT/UPDATE for better DB compatibility
        String checkFineSql = "SELECT fine_id FROM Fines WHERE issue_id = ?";
        String insertFineSql = "INSERT INTO Fines (student_id, issue_id, fine_amount, status, fine_date) VALUES (?, ?, ?, 'Unpaid', ?)";
        String updateFineSql = "UPDATE Fines SET fine_amount = ?, status = 'Unpaid', fine_date = ? WHERE fine_id = ?";
        Integer existingFineId = null;
    
        try (PreparedStatement checkPstmt = conn.prepareStatement(checkFineSql)) {
            checkPstmt.setInt(1, issueId);
            try (ResultSet rs = checkPstmt.executeQuery()) {
                if (rs.next()) {
                    existingFineId = rs.getInt("fine_id");
                }
            }
        }
    
        if (existingFineId != null) { // Update
            try (PreparedStatement updateFinePstmt = conn.prepareStatement(updateFineSql)) {
                updateFinePstmt.setDouble(1, fineAmount);
                updateFinePstmt.setDate(2, Date.valueOf(fineDate)); // Use java.sql.Date
                updateFinePstmt.setInt(3, existingFineId);
                if (updateFinePstmt.executeUpdate() == 0) {
                     System.err.println("Warning: Failed to update existing fine record for issue ID " + issueId);
                }
            }
        } else { // Insert
            try (PreparedStatement insertFinePstmt = conn.prepareStatement(insertFineSql)) {
                insertFinePstmt.setString(1, studentId);
                insertFinePstmt.setInt(2, issueId);
                insertFinePstmt.setDouble(3, fineAmount);
                insertFinePstmt.setDate(4, Date.valueOf(fineDate)); // Use java.sql.Date
                 if (insertFinePstmt.executeUpdate() == 0) {
                     throw new SQLException("Failed to insert new fine record for issue ID " + issueId);
                 }
            }
        }
         System.out.println("Fine recorded/updated for issue ID: " + issueId + ", Amount: " + fineAmount);
    }
     private String getSettingValue(String key, String defaultValue) {
        String sql = "SELECT setting_value FROM Settings WHERE setting_key = ?";
        Connection conn = null;
        try {
             conn = DatabaseConnection.getConnection(); // Should resolve now
             if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed."); }
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

}

class ReturnException extends Exception {
    public ReturnException(String message) {
        super(message);
    }
}

class BorrowException extends Exception { 
    public BorrowException(String message) {
        super(message);
    }
}

class ReissueException extends Exception { 
    public ReissueException(String message) {
        super(message);
    }
}