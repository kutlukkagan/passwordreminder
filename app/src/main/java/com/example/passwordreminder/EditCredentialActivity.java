package com.example.passwordreminder;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.Executors;

public class EditCredentialActivity extends BaseSecureActivity {

    private VaultViewModel vm;
    private long id = -1;
    private Credential loaded;

    private boolean isDirty = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // ✅ Screenshot + screen record engelle
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

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

        // Parola görünür olsun
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

        // ✅ Değişiklik takibi
        TextWatcher dirtyWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isDirty = true;
            }
        };

        etTitle.addTextChangedListener(dirtyWatcher);
        etUser.addTextChangedListener(dirtyWatcher);
        etPass.addTextChangedListener(dirtyWatcher);

        // düzenleme modunda veriyi yükle
        if (!isNew) {
            Executors.newSingleThreadExecutor().execute(() -> {
                loaded = AppDatabase.getInstance(getApplicationContext())
                        .credentialDao()
                        .getById(id);

                runOnUiThread(() -> {
                    if (loaded == null) {
                        Toast.makeText(EditCredentialActivity.this, "Kayıt bulunamadı", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // setText watcher'ı tetikler, o yüzden setText'ten sonra isDirty'yi sıfırlarız
                    etTitle.setText(loaded.title);
                    etUser.setText(loaded.username);
                    etPass.setText(loaded.password);

                    isDirty = false; // ✅ yükleme sonrası "değişiklik yok" kabul et
                });
            });
        } else {
            // Yeni kayıt ekranı boş açılıyorsa "değişiklik yok" başlasın
            isDirty = false;
        }

        btnSave.setOnClickListener(v -> {
            if (!admin) return;

            String t = etTitle.getText().toString().trim();
            String u = etUser.getText().toString().trim();
            String p = etPass.getText().toString();

            if (t.isEmpty() || u.isEmpty() || p.isEmpty()) {
                Toast.makeText(EditCredentialActivity.this, "Tüm alanlar dolu olmalı", Toast.LENGTH_SHORT).show();
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

            isDirty = false; // ✅ kaydedildi
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            if (!admin) return;
            if (loaded == null) return;

            new AlertDialog.Builder(EditCredentialActivity.this)
                    .setTitle("Silme Onayı")
                    .setMessage("Bu kaydı silmek istediğinize emin misiniz?\n\n" + loaded.title)
                    .setPositiveButton("Evet, Sil", (dialog, which) -> {
                        vm.getRepo().deleteCredential(loaded);
                        isDirty = false;
                        finish();
                    })
                    .setNegativeButton("İptal", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    @Override
    public void onBackPressed() {
        if (!isDirty) {
            super.onBackPressed();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Değişiklikler kaydedilmedi")
                .setMessage("Kaydetmeden çıkmak istiyor musunuz?")
                .setPositiveButton("Evet, Çık", (d, w) -> finish())
                .setNegativeButton("Hayır", (d, w) -> d.dismiss())
                .show();
    }
}
