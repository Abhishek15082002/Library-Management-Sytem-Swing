package library.backend;

import library.DatabaseConnection;
// import library.backend.PasswordUtils; // If using

import java.sql.*;
import java.time.YearMonth; // For monthly report
import java.util.ArrayList;
import java.util.List;

/**
 * Backend service class for handling administrator-related database operations and logic.
 * Includes user management, librarian management, fine management, and reporting.
 */
public class AdminService {

    // --- User Management Methods ---

    /**
     * Retrieves a list of all users (admins, librarians, students) with their details.
     *
     * @return A List of Object arrays, each representing a user row {username, role, status, email}.
     * @throws SQLException if a database access error occurs.
     */
    public List<Object[]> getAllUsers() throws SQLException {
        List<Object[]> users = new ArrayList<>();
        String sql = "SELECT username, role, status, email FROM Users ORDER BY role, username";
        Connection conn = null; Statement stmt = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed."); }
            stmt = conn.createStatement(); rs = stmt.executeQuery(sql);
            while (rs.next()) {
                users.add(new Object[]{
                    rs.getString("username"), rs.getString("role"),
                    rs.getString("status"), rs.getString("email")
                });
            }
        } finally {
            // Clean up resources
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            // Connection closing should be handled based on DatabaseConnection strategy
        }
        return users;
    }

    /**
     * Sets the status (Active/Inactive) for a given user.
     *
     * @param username The username of the user to update.
     * @param isActive True to set status to 'Active', false for 'Inactive'.
     * @return true if the status was successfully updated, false otherwise.
     * @throws SQLException if a database error occurs.
     */
    public boolean setUserStatus(String username, boolean isActive) throws SQLException {
        String newStatus = isActive ? "Active" : "Inactive";
        String sql = "UPDATE Users SET status = ? WHERE username = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed."); }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newStatus);
                pstmt.setString(2, username);
                return pstmt.executeUpdate() > 0;
            }
        } finally { /* Connection management */ }
    }

    // --- Librarian Management Methods ---

    /**
     * Retrieves details of all librarians.
     *
     * @return List of Object arrays, each {librarian_id, name, username, email}.
     * @throws SQLException if a database error occurs.
     */
     public List<Object[]> getAllLibrarians() throws SQLException {
        List<Object[]> librarians = new ArrayList<>();
        String sql = "SELECT l.librarian_id, l.name, l.username, u.email " +
                     "FROM Librarians l JOIN Users u ON l.username = u.username " +
                     "ORDER BY l.librarian_id";
        Connection conn = null; Statement stmt = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
             if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed."); }
            stmt = conn.createStatement(); rs = stmt.executeQuery(sql);
            while (rs.next()) {
                librarians.add(new Object[]{
                    rs.getString("librarian_id"), rs.getString("name"),
                    rs.getString("username"), rs.getString("email")
                });
            }
        } finally {
             if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
             if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return librarians;
    }

    /**
     * Adds a new librarian to the system. Handles transaction for Users and Librarians tables.
     * REMINDER: Implement password hashing for security.
     *
     * @param username The desired username (must be unique).
     * @param plainPassword The desired password (should be hashed before storing).
     * @param name The full name of the librarian.
     * @param email The email address (must be unique).
     * @return The generated librarian ID (e.g., "L002").
     * @throws SQLException if a database error occurs (e.g., username/email exists) or insertion fails.
     * @throws AdminActionException for specific business rule failures.
     */
     public String addLibrarian(String username, String plainPassword, String name, String email) throws SQLException, AdminActionException {
        Connection conn = null; String generatedLibrarianId = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed."); }
            conn.setAutoCommit(false); // Start transaction

            // --- Password Hashing Placeholder ---
            // String hashedPassword = PasswordUtils.hashPassword(plainPassword); // Use this when implemented
            String hashedPassword = plainPassword; // !! INSECURE PLACEHOLDER !! Replace with hashed password
            // ---

            // 1. Insert into Users table
            String userSql = "INSERT INTO Users (username, password, role, status, email) VALUES (?, ?, 'Librarian', 'Active', ?)";
            try (PreparedStatement userPstmt = conn.prepareStatement(userSql)) {
                userPstmt.setString(1, username);
                userPstmt.setString(2, hashedPassword); // Store the hash!
                userPstmt.setString(3, email);
                if (userPstmt.executeUpdate() == 0) { throw new SQLException("Creating user failed, no rows affected."); }
            } catch (SQLException e) {
                 conn.rollback(); // Rollback on user insertion failure
                 if (e.getMessage().contains("Duplicate entry")) { // Check for unique constraint violation
                     if (e.getMessage().contains("username")) throw new AdminActionException("Username '" + username + "' already exists.");
                     if (e.getMessage().contains("email")) throw new AdminActionException("Email '" + email + "' already exists.");
                 }
                 throw e; // Re-throw other SQL exceptions
            }

            // 2. Generate next Librarian ID
            generatedLibrarianId = generateNextLibrarianId(conn); // Pass connection for transaction

            // 3. Insert into Librarians table
            String libSql = "INSERT INTO Librarians (librarian_id, username, name) VALUES (?, ?, ?)";
            try (PreparedStatement libPstmt = conn.prepareStatement(libSql)) {
                libPstmt.setString(1, generatedLibrarianId);
                libPstmt.setString(2, username);
                libPstmt.setString(3, name);
                 if (libPstmt.executeUpdate() == 0) { throw new SQLException("Creating librarian record failed, no rows affected."); }
            }

            conn.commit(); // Commit transaction if both inserts succeed
            return generatedLibrarianId;

        } catch (SQLException | AdminActionException ex) {
            // Rollback transaction in case of any error during the process
            if (conn != null) { try { if (!conn.isClosed()) { conn.rollback(); } } catch (SQLException e) { System.err.println("Rollback failed: " + e.getMessage()); } }
            throw ex; // Re-throw the original exception
        } finally {
            // Ensure auto-commit is turned back on
            if (conn != null) { try { if (!conn.isClosed()) { conn.setAutoCommit(true); } } catch (SQLException e) { System.err.println("Failed to reset auto-commit: " + e.getMessage());} }
        }
    }

    /** Generates the next sequential librarian ID (e.g., L001, L002). */
    private String generateNextLibrarianId(Connection conn) throws SQLException {
        String query = "SELECT librarian_id FROM Librarians ORDER BY librarian_id DESC LIMIT 1";
        String lastId = null; int nextNumericId = 1;
        try (PreparedStatement pstmt = conn.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                lastId = rs.getString("librarian_id");
                try { nextNumericId = Integer.parseInt(lastId.substring(1)) + 1; }
                catch (Exception e) { System.err.println("Warning: Could not parse last librarian ID '" + lastId + "'."); nextNumericId = 1; }
            }
        } return String.format("L%03d", nextNumericId);
    }

    /** Deletes a librarian and their associated user account. Uses transaction. */
    public boolean deleteLibrarian(String librarianId) throws SQLException, AdminActionException {
        Connection conn = null; String usernameToDelete = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed."); }
            conn.setAutoCommit(false); // Start transaction
            // 1. Find the username
            String findUserSql = "SELECT username FROM Librarians WHERE librarian_id = ?";
            try (PreparedStatement findPstmt = conn.prepareStatement(findUserSql)) {
                findPstmt.setString(1, librarianId);
                try (ResultSet rs = findPstmt.executeQuery()) {
                    if (rs.next()) { usernameToDelete = rs.getString("username"); }
                    else { throw new AdminActionException("Librarian ID '" + librarianId + "' not found."); }
                }
            }
            // 2. Delete from Users (CASCADE should handle Librarians deletion)
            String deleteUserSql = "DELETE FROM Users WHERE username = ?";
            int rowsAffected = 0;
            try (PreparedStatement delUserPstmt = conn.prepareStatement(deleteUserSql)) {
                delUserPstmt.setString(1, usernameToDelete); rowsAffected = delUserPstmt.executeUpdate();
            }
            conn.commit(); // Commit if deletion successful
            return rowsAffected > 0;
        } catch (SQLException | AdminActionException ex) {
            if (conn != null) { try { if (!conn.isClosed()) { conn.rollback(); } } catch (SQLException e) { e.printStackTrace(); } }
            throw ex; // Re-throw
        } finally {
            if (conn != null) { try { if (!conn.isClosed()) { conn.setAutoCommit(true); } } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    // --- Fine Management Methods ---

    /** Retrieves all unpaid fines, optionally filtered by student ID. */
    public List<Object[]> getUnpaidFines(String studentIdFilter) throws SQLException {
        List<Object[]> fines = new ArrayList<>();
        String sql = "SELECT f.fine_id, f.student_id, s.name AS student_name, f.issue_id, b.title AS book_title, f.fine_amount, f.fine_date " +
                     "FROM Fines f LEFT JOIN Students s ON f.student_id = s.student_id " +
                     "LEFT JOIN IssuedBooks i ON f.issue_id = i.issue_id LEFT JOIN Books b ON i.book_id = b.book_id " +
                     "WHERE f.status = 'Unpaid' ";
        boolean filtering = studentIdFilter != null && !studentIdFilter.trim().isEmpty();
        if (filtering) { sql += "AND f.student_id = ? "; }
        sql += "ORDER BY f.fine_date DESC, f.student_id";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed."); }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (filtering) { pstmt.setString(1, studentIdFilter.trim()); }
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        fines.add(new Object[]{
                            rs.getInt("fine_id"), rs.getString("student_id"), rs.getString("student_name"),
                            rs.getObject("issue_id") != null ? rs.getInt("issue_id") : "N/A", rs.getString("book_title"),
                            rs.getDouble("fine_amount"), rs.getDate("fine_date") });
                    }
                }
            }
        } finally { /* Connection management */ }
        return fines;
    }

    /** Waives a specific fine by setting its status to 'Paid'. */
    public boolean waiveFine(int fineId) throws SQLException {
        String sql = "UPDATE Fines SET status = 'Paid' WHERE fine_id = ? AND status = 'Unpaid'";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed."); }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, fineId);
                return pstmt.executeUpdate() > 0;
            }
        } finally { /* Connection management */ }
    }

    // --- Report Methods ---

    /** Retrieves a report of all books currently available. */
    public List<Object[]> getAvailableBooksReport() throws SQLException {
        List<Object[]> books = new ArrayList<>();
        String sql = "SELECT book_id, title, author, category, available_copies FROM Books WHERE available_copies > 0 ORDER BY title";
        Connection conn = null; Statement stmt = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed."); }
            stmt = conn.createStatement(); rs = stmt.executeQuery(sql);
            while (rs.next()) {
                books.add(new Object[]{ rs.getString("book_id"), rs.getString("title"), rs.getString("author"), rs.getString("category"), rs.getInt("available_copies") });
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return books;
    }

     /** Retrieves a report of all books currently issued or overdue. */
    public List<Object[]> getAllBorrowedBooksReport() throws SQLException {
        List<Object[]> borrowed = new ArrayList<>();
        String sql = "SELECT i.issue_id, i.book_id, b.title AS book_title, i.student_id, s.name AS student_name, i.issue_date, i.due_date, i.status " +
                     "FROM IssuedBooks i JOIN Books b ON i.book_id = b.book_id JOIN Students s ON i.student_id = s.student_id " +
                     "WHERE i.status IN ('Issued', 'Overdue') ORDER BY i.due_date ASC, s.name ASC";
        Connection conn = null; Statement stmt = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed."); }
            stmt = conn.createStatement(); rs = stmt.executeQuery(sql);
            while (rs.next()) {
                borrowed.add(new Object[]{ rs.getInt("issue_id"), rs.getString("book_id"), rs.getString("book_title"), rs.getString("student_id"), rs.getString("student_name"), rs.getDate("issue_date"), rs.getDate("due_date"), rs.getString("status") });
            }
        } finally {
             if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
             if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return borrowed;
    }

    /** Retrieves a fine report for a specific student. */
    public List<Object[]> getFineReportByStudent(String studentId) throws SQLException {
        List<Object[]> fines = new ArrayList<>();
        String sql = "SELECT f.fine_id, f.issue_id, b.title AS book_title, f.fine_amount, f.fine_date, f.status " +
                     "FROM Fines f LEFT JOIN IssuedBooks i ON f.issue_id = i.issue_id LEFT JOIN Books b ON i.book_id = b.book_id " +
                     "WHERE f.student_id = ? ORDER BY f.fine_date DESC";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed."); }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, studentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        fines.add(new Object[]{ rs.getInt("fine_id"), rs.getObject("issue_id") != null ? rs.getInt("issue_id") : "N/A", rs.getString("book_title"), rs.getDouble("fine_amount"), rs.getDate("fine_date"), rs.getString("status") });
                    }
                }
            }
        } finally { /* Connection management */ }
        return fines;
    }

    /** Retrieves a summary report of total fines generated within a specific month and year. */
    public List<Object[]> getFineReportByMonth(int year, int month) throws SQLException {
        List<Object[]> report = new ArrayList<>();
        String sql = "SELECT SUM(fine_amount) AS total_fine FROM Fines WHERE YEAR(fine_date) = ? AND MONTH(fine_date) = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed."); }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, year); pstmt.setInt(2, month);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Object totalFine = rs.getObject("total_fine");
                        report.add(new Object[]{ totalFine != null ? rs.getDouble("total_fine") : 0.0 });
                    } else { report.add(new Object[]{ 0.0 }); }
                }
            }
        } finally { /* Connection management */ }
        return report;
    }

}

class AdminActionException extends Exception {
    public AdminActionException(String message) {
        super(message);
    }
}