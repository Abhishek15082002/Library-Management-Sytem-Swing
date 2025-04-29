package library;
import java.sql.*;
import java.io.*;
public class DatabaseConnection {
    public static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver"; 
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db"; // Replace 'library_db' with your DB name
    private static final String DB_USER = "root"; 
    private static final String DB_PASSWORD = "root"; 

    private static Connection connection = null;
    private DatabaseConnection() {}
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Database connection established successfully."); 
            } catch (SQLException e) {
                System.err.println("Error connecting to the database: " + e.getMessage());
                e.printStackTrace(); 
                throw e; 
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Database connection closed."); 
                    connection = null; 
                }
            } catch (SQLException e) {
                System.err.println("Error closing the database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Main method for testing the connection.
     * This is not part of the library management system but can be used for quick testing.
     */
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                System.out.println("Test Connection Successful!");
    
                // Read and execute SQL statements from schemas.sql
                try (BufferedReader reader = new BufferedReader(new FileReader("../sql/schemas.sql"))) {
                    StringBuilder sqlBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sqlBuilder.append(line).append("\n");
                    }
                    String[] sqlStatements = sqlBuilder.toString().split(";");
                    try (Statement stmt = conn.createStatement()) {
                        for (String sql : sqlStatements) {
                            sql = sql.trim();
                            if (!sql.isEmpty()) {
                                stmt.execute(sql);
                            }
                        }
                    }
                    System.out.println("Schema imported from schemas.sql.");
                } catch (IOException e) {
                    System.err.println("Error reading schemas.sql: " + e.getMessage());
                }
    
                DatabaseConnection.closeConnection();
            } else {
                System.err.println("Test Connection Failed!");
            }
        } catch (SQLException e) {
            System.err.println("Test Connection Failed with Exception: " + e.getMessage());
        }
    }
}