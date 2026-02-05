package com.example.passwordreminder;

public class SessionManager {

    private static SessionManager INSTANCE;

    private User currentUser;
    private long lastActiveAt = -1;

    // 2 dakika
    private static final long TIMEOUT_MS = 2 * 60 * 1000L;

    public static SessionManager get() {
        if (INSTANCE == null) {
            INSTANCE = new SessionManager();
        }
        return INSTANCE;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User u) {
        this.currentUser = u;
        touch(); // ✅ login olur olmaz zamanı set et
    }

    public void touch() {
        lastActiveAt = System.currentTimeMillis();
    }

    public boolean isExpired() {
        // session yoksa expired
        if (currentUser == null) return true;

        // ilk girişte lastActiveAt set edilmemişse set et, expired sayma
        if (lastActiveAt <= 0) {
            lastActiveAt = System.currentTimeMillis();
            return false;
        }

        long now = System.currentTimeMillis();
        return (now - lastActiveAt) > TIMEOUT_MS;
    }

    public void logout() {
        // ✅ kullanıcıyı temizle
        currentUser = null;

        // ✅ son aktif zamanı sıfırla (tekrar yanlış expired olmasın)
        lastActiveAt = 0;
    }


    // Eğer bazı yerlerde admin kontrolü yapıyorsan yardımcı metot:
    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.role);
    }
}
