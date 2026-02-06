package com.example.passwordreminder;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * ✅ Uygulama ayarlarını (timeout gibi) tek yerde tutmak için helper.
 * SharedPreferences kullanır.
 */
public final class SettingsPrefs {

    private SettingsPrefs() {}

    // ✅ Pref key
    private static final String KEY_TIMEOUT_MIN = "pref_timeout_min";

    // ✅ Varsayılan (dakika)
    private static final int DEFAULT_TIMEOUT_MIN = 2;

    /**
     * ✅ Timeout (ms) döner.
     */
    public static long getTimeoutMs(Context ctx) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        int min = sp.getInt(KEY_TIMEOUT_MIN, DEFAULT_TIMEOUT_MIN);
        return min * 60L * 1000L;
    }

    /**
     * ✅ Timeout (dakika) kaydeder.
     */
    public static void setTimeoutMin(Context ctx, int minutes) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        sp.edit().putInt(KEY_TIMEOUT_MIN, minutes).apply();
    }

    /**
     * ✅ Timeout (dakika) okur (UI seçimleri için).
     */
    public static int getTimeoutMin(Context ctx) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        return sp.getInt(KEY_TIMEOUT_MIN, DEFAULT_TIMEOUT_MIN);
    }
}
