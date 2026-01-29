package com.example.passwordreminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.Executors;


public class LoginActivity extends AppCompatActivity {

    private VaultViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ✅ Screenshot + screen record engelle
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Eğer hiç kullanıcı yoksa Setup ekranına git
        Executors.newSingleThreadExecutor().execute(() -> {
            int count = AppDatabase.getInstance(getApplicationContext()).userDao().countUsers();
            if (count == 0) {
                runOnUiThread(() -> {
                    startActivity(new Intent(this, SetupAdminActivity.class));
                    finish();
                });
            }
        });



        vm = new ViewModelProvider(this).get(VaultViewModel.class);


        EditText etU = findViewById(R.id.etUsername);
        EditText etP = findViewById(R.id.etPassword);
        Button btn = findViewById(R.id.btnLogin);



        btn.setOnClickListener(v -> {
            String u = etU.getText().toString().trim();
            String p = etP.getText().toString();

            vm.getRepo().login(u, p, (ok, user, error) -> runOnUiThread(() -> {
                if (!ok) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                    return;
                }
                SessionManager.get().setCurrentUser(user);
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }));
        });
    }
}

