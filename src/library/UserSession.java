package library;
public class UserSession {

    private static UserSession instance;

    private String username;
    private String role;
    private String userId; // Store specific ID (S001, L001, etc.) if needed globally

    // Private constructor to prevent instantiation
    private UserSession(String username, String role, String userId) {
        this.username = username;
        this.role = role;
        this.userId = userId;
    }

    /**
     * Creates the singleton session instance. Call this upon successful login.
     *
     * @param username The username of the logged-in user.
     * @param role The role of the logged-in user.
     * @param userId The specific ID (S001, L001, etc.) associated with the username/role.
     */
    public static void createInstance(String username, String role, String userId) {
        if (instance == null) {
            instance = new UserSession(username, role, userId);
        }
        else {
            System.out.println("User session already exists. Please log out first.");
        }
    }

    /**
     * Gets the singleton instance of the UserSession.
     *
     * @return The UserSession instance, or null if no user is logged in.
     */
    public static UserSession getInstance() {
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }

    // Accessors
    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getUserId() {
        return userId;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}