package com.example.passwordreminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BaseSecureActivity extends AppCompatActivity {

    protected boolean requiresAuth() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Screenshot engeli
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );
        SessionManager.get().init(getApplicationContext());
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requiresAuth()) {
            lockIfExpired(); // resume'da da kontrol
        }
    }

    // ⭐ En kritik yer: Kullanıcı ilk dokunuş yaptığında önce kontrol et
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (requiresAuth()) {
            if (lockIfExpired()) {
                return true; // dokunuşu yut, işlem devam etmesin
            }
            // expired değilse normal akışa izin ver
            SessionManager.get().touch();
        }
        return super.dispatchTouchEvent(ev);
    }

    // true dönerse kilitledi (login’e gönderdi) demek
    private boolean lockIfExpired() {
        if (SessionManager.get().isExpired()) {
            SessionManager.get().logout();

            Toast.makeText(this, "Oturum süresi doldu. Tekrar giriş yapın.", Toast.LENGTH_SHORT).show();

            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
            return true;
        }
        return false;
    }
}

