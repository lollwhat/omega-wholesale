package controller;

import model.*;

public class SessionController {
    private static SessionController instance;
    private User currentUser;

    private SessionController() {
        // Private constructor to prevent instantiation
    }

    public static SessionController getInstance() {
        if (instance == null) {
            instance = new SessionController();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // Checks whether the user is logged in
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Gets the user ID of the current user
    public String getUserId() {
        return isLoggedIn() ? currentUser.getUserID() : null;
    }

    // Logs out the current user
    public void logout() {
        currentUser = null;
    }
}
