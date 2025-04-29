package library.backend;

import library.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

    public List<Object[]> getAllUsers() throws SQLException {
        List<Object[]> users = new ArrayList<>();
        String sql = "SELECT username, role, status, email FROM Users ORDER BY role, username";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Database connection failed or is closed.");
            }
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                try {
                    String username = rs.getString("username");
                    String role = rs.getString("role");
                    String status = rs.getString("status");
                    String email = rs.getString("email");
                    users.add(new Object[]{ username, role, status, email });
                } catch (Exception e) {
                    System.err.println("AdminService: Error getting data for row " + rowCount + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.err.println("AdminService: SQLException in getAllUsers: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("AdminService: Unexpected Exception in getAllUsers: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Unexpected error fetching users: " + e.getMessage(), e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { System.err.println("AdminService: Error closing ResultSet: " + e.getMessage()); }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) { System.err.println("AdminService: Error closing Statement: " + e.getMessage()); }
            }
        }
        return users;
    }

    public boolean setUserStatus(String username, boolean isActive) throws SQLException {
        String newStatus = isActive ? "Active" : "Inactive";
        String sql = "UPDATE Users SET status = ? WHERE username = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed or is closed."); }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newStatus);
                pstmt.setString(2, username);
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        } finally { }
    }

    public List<Object[]> getAllLibrarians() throws SQLException {
        List<Object[]> librarians = new ArrayList<>();
        String sql = "SELECT l.librarian_id, l.name, l.username, u.email " +
                     "FROM Librarians l JOIN Users u ON l.username = u.username " +
                     "ORDER BY l.librarian_id";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed or is closed."); }
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    librarians.add(new Object[]{
                        rs.getString("librarian_id"), rs.getString("name"),
                        rs.getString("username"), rs.getString("email")
                    });
                }
            }
        } finally { }
        return librarians;
    }

    public String addLibrarian(String username, String password, String name, String email) throws SQLException, AdminActionException {
        Connection conn = null;
        String generatedLibrarianId = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed."); }
            conn.setAutoCommit(false);
            String userSql = "INSERT INTO Users (username, password, role, status, email) VALUES (?, ?, 'Librarian', 'Active', ?)";
            try (PreparedStatement userPstmt = conn.prepareStatement(userSql)) {
                userPstmt.setString(1, username); userPstmt.setString(2, password); userPstmt.setString(3, email);
                if (userPstmt.executeUpdate() == 0) { throw new SQLException("Creating user failed."); }
            } catch (SQLException e) {
                conn.rollback();
                if (e.getMessage().contains("Duplicate entry")) {
                    if (e.getMessage().contains("username")) throw new AdminActionException("Username exists.");
                    if (e.getMessage().contains("email")) throw new AdminActionException("Email exists.");
                } throw e;
            }
            generatedLibrarianId = generateNextLibrarianId(conn);
            String libSql = "INSERT INTO Librarians (librarian_id, username, name) VALUES (?, ?, ?)";
            try (PreparedStatement libPstmt = conn.prepareStatement(libSql)) {
                libPstmt.setString(1, generatedLibrarianId); libPstmt.setString(2, username); libPstmt.setString(3, name);
                if (libPstmt.executeUpdate() == 0) { throw new SQLException("Creating librarian record failed."); }
            }
            conn.commit();
            return generatedLibrarianId;
        } catch (SQLException | AdminActionException ex) {
            if (conn != null) { try { if (!conn.isClosed()) { conn.rollback(); } } catch (SQLException e) { e.printStackTrace(); } }
            throw ex;
        } finally {
            if (conn != null) { try { if (!conn.isClosed()) { conn.setAutoCommit(true); } } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

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

    public boolean deleteLibrarian(String librarianId) throws SQLException, AdminActionException {
        Connection conn = null; String usernameToDelete = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) { throw new SQLException("Database connection failed."); }
            conn.setAutoCommit(false);
            String findUserSql = "SELECT username FROM Librarians WHERE librarian_id = ?";
            try (PreparedStatement findPstmt = conn.prepareStatement(findUserSql)) {
                findPstmt.setString(1, librarianId);
                try (ResultSet rs = findPstmt.executeQuery()) {
                    if (rs.next()) { usernameToDelete = rs.getString("username"); }
                    else { throw new AdminActionException("Librarian ID '" + librarianId + "' not found."); }
                }
            }
            String deleteUserSql = "DELETE FROM Users WHERE username = ?";
            int rowsAffected = 0;
            try (PreparedStatement delUserPstmt = conn.prepareStatement(deleteUserSql)) {
                delUserPstmt.setString(1, usernameToDelete); rowsAffected = delUserPstmt.executeUpdate();
            }
            conn.commit();
            return rowsAffected > 0;
        } catch (SQLException | AdminActionException ex) {
            if (conn != null) { try { if (!conn.isClosed()) { conn.rollback(); } } catch (SQLException e) { e.printStackTrace(); } }
            throw ex;
        } finally {
            if (conn != null) { try { if (!conn.isClosed()) { conn.setAutoCommit(true); } } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

}

class AdminActionException extends Exception {
    public AdminActionException(String message) {
        super(message);
    }
}
