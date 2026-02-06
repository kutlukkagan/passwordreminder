package com.example.passwordreminder;

import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends BaseSecureActivity {

    private VaultViewModel vm;

    // ✅ Arama / normal liste arasında geçiş için active LiveData
    private LiveData<List<Credential>> currentSource;

    // ✅ UI referansları
    private TextView tvEmpty;
    private View emptyContainer;
    private RecyclerView rv;
    private EditText etSearch;

    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // ✅ Screenshot + screen record engelle
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        super.onCreate(savedInstanceState);

        // ✅ Oturum yoksa login'e dön
        if (SessionManager.get().getCurrentUser() == null) {
            goLoginAndFinish();
            return;
        }

        setContentView(R.layout.activity_main);

        // ✅ Toolbar
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Kayıtlar");

        // ✅ ViewModel
        vm = new ViewModelProvider(this).get(VaultViewModel.class);

        // ✅ Admin mi?
        isAdmin = SessionManager.get().isAdmin();

        // ✅ UI
        rv = findViewById(R.id.rvCredentials);
        emptyContainer = findViewById(R.id.emptyContainer);
        tvEmpty = findViewById(R.id.tvEmpty);
        etSearch = findViewById(R.id.etSearch);
        Button btnNew = findViewById(R.id.btnNew);

        // ✅ Admin değilse yeni kayıt pasif
        if (!isAdmin) {
            btnNew.setEnabled(false);
            btnNew.setAlpha(0.4f);
        }

        // ✅ Adapter
        CredentialAdapter adapter = new CredentialAdapter(new CredentialAdapter.Actions() {
            @Override
            public void onUpdate(Credential c) {
                if (!isAdmin) return;
                Intent i = new Intent(MainActivity.this, EditCredentialActivity.class);
                i.putExtra("id", c.id);
                startActivity(i);
            }

            @Override
            public void onDelete(Credential c) {
                if (!isAdmin) return;

                // ✅ Silme onayı popup
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Silme Onayı")
                        .setMessage("Bu kaydı silmek istediğinize emin misiniz?\n\n" + c.title + "\n" + c.username)
                        .setPositiveButton("Evet, Sil", (dialog, which) -> {
                            vm.getRepo().deleteCredential(c);
                            Toast.makeText(MainActivity.this, "Silindi", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("İptal", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }, isAdmin);

        // ✅ RecyclerView
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        rv.setHasFixedSize(true);

        // ✅ Yeni kayıt
        btnNew.setOnClickListener(v -> {
            if (!isAdmin) return;
            startActivity(new Intent(MainActivity.this, EditCredentialActivity.class));
        });

        // ✅ Listeyi ekrana basan tek fonksiyon (hem normal hem arama için)
        java.util.function.Consumer<List<Credential>> render = (list) -> {
            adapter.submit(list);

            boolean hasData = (list != null && !list.isEmpty());
            String q = etSearch.getText().toString().trim();

            if (hasData) {
                rv.setVisibility(View.VISIBLE);
                emptyContainer.setVisibility(View.GONE);
            } else {
                rv.setVisibility(View.GONE);
                emptyContainer.setVisibility(View.VISIBLE);

                // ✅ Mesajı arama durumuna göre değiştir
                if (q.isEmpty()) tvEmpty.setText("Henüz kayıt yok");
                else tvEmpty.setText("Sonuç bulunamadı");
            }
        };

        // ✅ İlk kaynak: tüm liste
        currentSource = vm.getCredentials();
        currentSource.observe(this, render::accept);

        // ✅ Arama: yazdıkça LiveData kaynağını değiştir
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s.toString().trim();

                // ✅ Eski observer'ı kaldır
                if (currentSource != null) {
                    currentSource.removeObservers(MainActivity.this);
                }

                // ✅ Yeni kaynağı seç
                currentSource = q.isEmpty() ? vm.getCredentials() : vm.search(q);

                // ✅ Yeni kaynağı bağla
                currentSource.observe(MainActivity.this, render::accept);
            }
        });
    }

    // ✅ Toolbar menüsünü bağla
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // ✅ Admin değilse ayarlar menüsünü gizleyebilirsin (istersen)
        // menu.findItem(R.id.action_settings).setVisible(isAdmin);

        return true;
    }

    // ✅ Menü tıklamaları
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // ✅ Ayarlar ekranına git
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_logout) {
            // ✅ Logout
            SessionManager.get().logout();
            goLoginAndFinish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goLoginAndFinish() {
        Intent i = new Intent(this, LoginActivity.class);

        // ✅ Back stack temizle (geri ile ana ekrana dönmesin)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(i);
        finish();
    }
}
