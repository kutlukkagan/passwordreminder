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

    // ✅ Kullanıcı değişiklik yaptı mı?
    private boolean isDirty = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // ✅ Screenshot + screen record engelle
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        super.onCreate(savedInstanceState);

        // ✅ Oturum yoksa ekranı kapat
        if (SessionManager.get().getCurrentUser() == null) {
            finish();
            return;
        }

        // ✅ ÖNEMLİ: Önce layout'u set et ki findViewById çalışsın
        setContentView(R.layout.activity_edit_credential);

        vm = new ViewModelProvider(this).get(VaultViewModel.class);

        // ✅ Intent'ten id al (yeni kayıt mı, düzenleme mi?)
        id = getIntent().getLongExtra("id", -1);
        boolean isNew = (id <= 0);

        // ✅ Toolbar'ı ActionBar olarak bağla (başlık kontrolü için)
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ✅ Geri ok göster (up button)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // ✅ Başlığı set et (burada kesin set ediyoruz)
            getSupportActionBar().setTitle(isNew ? "Yeni Kayıt" : "Kayıt Düzenle");
        } else {
            // nadiren düşer ama fallback
            toolbar.setTitle(isNew ? "Yeni Kayıt" : "Kayıt Düzenle");
        }

        // ✅ View'ları bağla
        EditText etTitle = findViewById(R.id.etTitle);
        EditText etUser  = findViewById(R.id.etUser);
        EditText etPass  = findViewById(R.id.etPass);
        Button btnSave   = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);

        boolean admin = SessionManager.get().isAdmin();


        // ✅ Yeni kayıtsa sil butonu yok, save butonu "Kaydet"
        btnDelete.setVisibility(isNew ? View.GONE : View.VISIBLE);
        btnSave.setText(isNew ? "Kaydet" : "Güncelle");

        // ✅ Admin değilse her şeyi kapat
        if (!admin) {
            btnSave.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            etTitle.setEnabled(false);
            etUser.setEnabled(false);
            etPass.setEnabled(false);
        }

        // ✅ Değişiklik takibi (kullanıcı yazarsa isDirty=true)
        TextWatcher dirtyWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                isDirty = true;
            }
        };

        etTitle.addTextChangedListener(dirtyWatcher);
        etUser.addTextChangedListener(dirtyWatcher);
        etPass.addTextChangedListener(dirtyWatcher);

        // ✅ Düzenleme modunda veriyi DB’den yükle
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

                    // ✅ setText watcher'ı tetikler → bu yüzden sonra isDirty=false yapıyoruz
                    etTitle.setText(loaded.title);
                    etUser.setText(loaded.username);
                    etPass.setText(loaded.password);

                    isDirty = false; // ✅ yükleme sonrası değişiklik yok kabul
                });
            });
        } else {
            isDirty = false; // ✅ yeni ekranda başlangıçta değişiklik yok
        }

        // ✅ Kaydet / Güncelle
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
                // ✅ Yeni kayıt
                Credential c = new Credential(t, u, p, System.currentTimeMillis());
                vm.getRepo().insertCredential(c);
            } else {
                // ✅ Güncelle
                if (loaded == null) return;
                loaded.title = t;
                loaded.username = u;
                loaded.password = p;
                vm.getRepo().updateCredential(loaded);
            }

            isDirty = false; // ✅ kaydedildi
            finish();
        });

        // ✅ Sil (onay popup ile)
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

    // ✅ Geri tuşu / geri ok için "kaydetmeden çıkış" popup
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

    // ✅ Toolbar'daki geri oka basınca da aynı popup çalışsın
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
