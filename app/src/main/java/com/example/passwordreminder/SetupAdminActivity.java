package com.example.passwordreminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executors;

public class SetupAdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // (İsteğe bağlı) Screenshot engeli
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_admin);
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(SetupAdminActivity.this, "Kurulum tamamlanmalı", Toast.LENGTH_SHORT).show();
                // geri çıkışı engelliyoruz
            }
        });

        EditText etU = findViewById(R.id.etAdminUsername);
        EditText etP1 = findViewById(R.id.etAdminPassword);
        EditText etP2 = findViewById(R.id.etAdminPassword2);
        Button btn = findViewById(R.id.btnCreateAdmin);

        btn.setOnClickListener(v -> {
            String u = etU.getText().toString().trim();
            String p1 = etP1.getText().toString();
            String p2 = etP2.getText().toString();

            if (u.isEmpty() || p1.isEmpty() || p2.isEmpty()) {
                Toast.makeText(this, "Tüm alanlar dolu olmalı", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!p1.equals(p2)) {
                Toast.makeText(this, "Şifreler uyuşmuyor", Toast.LENGTH_SHORT).show();
                return;
            }
            if (p1.length() < 6) {
                Toast.makeText(this, "Şifre en az 6 karakter olmalı", Toast.LENGTH_SHORT).show();
                return;
            }

            // DB’ye admin ekle
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());

                // Eğer bu ekrana yanlışlıkla tekrar gelindiyse, tekrar admin oluşturmayalım
                if (db.userDao().countUsers() > 0) {
                    runOnUiThread(() -> {
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    });
                    return;
                }

                String salt = CryptoUtil.randomSalt();
                String hash = CryptoUtil.saltedHash(p1, salt);
                db.userDao().insert(new User(u, hash, salt, "ADMIN"));

                runOnUiThread(() -> {
                    Toast.makeText(this, "Admin oluşturuldu. Giriş yapabilirsiniz.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                });
            });
        });
    }



}

