package model;

/**
 * Abstract base class for every logged-in person.
 * Shows abstraction (abstract class) and encapsulation (private fields).
 */
public abstract class User {
    private int userId;
    private String username;
    private String role;

    protected User(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public abstract String getDisplayName();

    public abstract String getWelcomeMessage();
}
