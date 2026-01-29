package com.example.passwordreminder;

public class SessionManager {
    private static volatile SessionManager INSTANCE;
    private User currentUser;

    public static SessionManager get() {
        if (INSTANCE == null) {
            synchronized (SessionManager.class) {
                if (INSTANCE == null) INSTANCE = new SessionManager();
            }
        }
        return INSTANCE;
    }

    public void setCurrentUser(User user) { this.currentUser = user; }
    public User getCurrentUser() { return currentUser; }

    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.role);
    }
}

