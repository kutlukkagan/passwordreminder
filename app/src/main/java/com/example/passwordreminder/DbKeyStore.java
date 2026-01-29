package com.example.passwordreminder;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.security.SecureRandom;

public class DbKeyStore {
    private static final String PREF_NAME = "secure_prefs";
    private static final String KEY_DB_PASSPHRASE = "db_passphrase";

    public static char[] getOrCreatePassphrase(Context ctx) {
        try {
            MasterKey masterKey = new MasterKey.Builder(ctx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sp = EncryptedSharedPreferences.create(
                    ctx,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String existing = sp.getString(KEY_DB_PASSPHRASE, null);
            if (existing != null) {
                return existing.toCharArray();
            }

            // 32 byte random -> hex string yapalım (printable ve stabil)
            byte[] raw = new byte[32];
            new SecureRandom().nextBytes(raw);

            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) sb.append(String.format("%02x", b));
            String pass = sb.toString();

            sp.edit().putString(KEY_DB_PASSPHRASE, pass).apply();
            return pass.toCharArray();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
