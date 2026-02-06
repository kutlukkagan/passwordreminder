package com.example.passwordreminder;

import android.content.Context;

public class SessionManager {

    private static SessionManager INSTANCE;



    // ✅ Uygulama context'i (timeout prefs okumak için)
    private Context appCtx;

    private User currentUser;
    private long lastActiveAt = 0L;


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
        touch(); // ✅ login olur olmaz aktif zamanı set et
    }

    /**
     * ✅ Tek sefer init et (BaseSecureActivity içinde çağıracağız)
     */
    public void init(Context ctx) {
        if (appCtx == null) {
            appCtx = ctx.getApplicationContext();
        }
    }
    public void logout() {
        // ✅ kullanıcıyı temizle
        currentUser = null;
        // ✅ son aktif zamanı sıfırla
        lastActiveAt = 0L;


    }

    public void touch() {
        lastActiveAt = System.currentTimeMillis();
    }

    public boolean isExpired() {
        if (currentUser == null) return true;
        if (lastActiveAt <= 0) return true;


        long now = System.currentTimeMillis();
        // ✅ Context yoksa (init edilmediyse) varsayılan 2dk kabul et
        long timeoutMs = (appCtx != null) ? SettingsPrefs.getTimeoutMs(appCtx) : (2 * 60L * 1000L);
        return (now - lastActiveAt) > TIMEOUT_MS;
    }




    // Eğer bazı yerlerde admin kontrolü yapıyorsan yardımcı metot:
    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.role);
    }
}
