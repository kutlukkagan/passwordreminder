package com.example.passwordreminder;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.Executors;

public class EditCredentialActivity extends AppCompatActivity {

    private VaultViewModel vm;
    private long id = -1;
    private Credential loaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ✅ Screenshot + screen record engelle
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);

        if (SessionManager.get().getCurrentUser() == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_edit_credential);

        vm = new ViewModelProvider(this).get(VaultViewModel.class);

        EditText etTitle = findViewById(R.id.etTitle);
        EditText etUser  = findViewById(R.id.etUser);
        EditText etPass  = findViewById(R.id.etPass);
        Button btnSave   = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);

        boolean admin = SessionManager.get().isAdmin();

        id = getIntent().getLongExtra("id", -1);
        boolean isNew = (id <= 0);

        // parola görünür olsun (istersen sadece edit modunda açık yapabiliriz)
        etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        btnDelete.setVisibility(isNew ? View.GONE : View.VISIBLE);
        btnSave.setText(isNew ? "Kaydet" : "Güncelle");

        if (!admin) {
            btnSave.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            etTitle.setEnabled(false);
            etUser.setEnabled(false);
            etPass.setEnabled(false);
        }

        // düzenleme modunda veriyi yükle
        if (!isNew) {
            Executors.newSingleThreadExecutor().execute(() -> {
                loaded = AppDatabase.getInstance(getApplicationContext())
                        .credentialDao()
                        .getById(id);

                runOnUiThread(() -> {
                    if (loaded == null) {
                        Toast.makeText(this, "Kayıt bulunamadı", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    etTitle.setText(loaded.title);
                    etUser.setText(loaded.username);
                    etPass.setText(loaded.password);
                });
            });
        }

        btnSave.setOnClickListener(v -> {
            if (!admin) return;

            String t = etTitle.getText().toString().trim();
            String u = etUser.getText().toString().trim();
            String p = etPass.getText().toString();

            if (t.isEmpty() || u.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Tüm alanlar dolu olmalı", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isNew) {
                Credential c = new Credential(t, u, p, System.currentTimeMillis());
                vm.getRepo().insertCredential(c);
            } else {
                if (loaded == null) return;
                loaded.title = t;
                loaded.username = u;
                loaded.password = p;
                vm.getRepo().updateCredential(loaded);
            }

            finish();
        });

        btnDelete.setOnClickListener(v -> {
            if (!admin) return;
            if (loaded == null) return;

            vm.getRepo().deleteCredential(loaded);
            finish();
        });
    }
}
