package com.example.passwordreminder;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VaultRepository {

    // ===== LOGIN CALLBACK =====
    public interface LoginCallback {
        void onResult(boolean ok, User user, String error);
    }
    public LiveData<List<Credential>> searchCredentials(String q) {
        return credentialDao.search(q);
    }

    private final AppDatabase db;
    private final CredentialDao credentialDao;
    private final UserDao userDao;

    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public VaultRepository(Context ctx) {
        db = AppDatabase.getInstance(ctx.getApplicationContext());
        credentialDao = db.credentialDao();
        userDao = db.userDao();
    }

    // ===== CREDENTIAL LIST =====
    public LiveData<List<Credential>> getAllCredentials() {
        return credentialDao.getAll();
    }

    // ===== CREDENTIAL CRUD =====
    public void insertCredential(Credential c) {
        io.execute(() -> credentialDao.insert(c));
    }

    public void updateCredential(Credential c) {
        io.execute(() -> credentialDao.update(c));
    }

    public void deleteCredential(Credential c) {
        io.execute(() -> credentialDao.delete(c));
    }

    // ===== LOGIN =====
    public void login(String username, String password, LoginCallback cb) {
        io.execute(() -> {
            User u = userDao.findByUsername(username);
            if (u == null) {
                cb.onResult(false, null, "Kullanıcı bulunamadı");
                return;
            }

            String candidate = CryptoUtil.saltedHash(password, u.salt);
            if (!candidate.equals(u.passwordHash)) {
                cb.onResult(false, null, "Şifre hatalı");
                return;
            }

            cb.onResult(true, u, null);
        });
    }

    // ===== SETUP / ADMIN =====
    public int countUsersSync() {
        return userDao.countUsers();
    }
}

