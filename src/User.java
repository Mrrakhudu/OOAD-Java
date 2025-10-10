package models;

import enums.UserRole;

// Abstract User class
public abstract class User {
    private String username;
    private String password;
    private UserRole role;

    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public boolean authenticate(String inputUsername, String inputPassword) {
        return this.username.equals(inputUsername) && this.password.equals(inputPassword);
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public abstract String getDisplayName();

    public String toFileString() {
        return username + "|" + password + "|" + role.toString();
    }
}