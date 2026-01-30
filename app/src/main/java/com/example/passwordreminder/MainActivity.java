package com.example.passwordreminder;
import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends BaseSecureActivity {

    private VaultViewModel vm;
    private androidx.lifecycle.LiveData<java.util.List<Credential>> currentSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {



// ✅ Screenshot + screen record engelle
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);

        if (SessionManager.get().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        vm = new ViewModelProvider(this).get(VaultViewModel.class);

        boolean isAdmin = SessionManager.get().isAdmin();

        RecyclerView rv = findViewById(R.id.rvCredentials);
        Button btnNew = findViewById(R.id.btnNew);

        if (!isAdmin) {
            btnNew.setEnabled(false);
            btnNew.setAlpha(0.4f);
        }

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

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Silme Onayı")
                        .setMessage("Bu kaydı silmek istediğinize emin misiniz?\n\n" + c.title+"\n"+c.username)
                        .setPositiveButton("Evet, Sil", (dialog, which) -> {
                            vm.getRepo().deleteCredential(c);
                            Toast.makeText(MainActivity.this, "Silindi", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("İptal", (dialog, which) -> dialog.dismiss())
                        .show();
            }

        }, isAdmin);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        vm.getCredentials().observe(this, adapter::submit);

        btnNew.setOnClickListener(v -> {
            if (!isAdmin) return;
            startActivity(new Intent(MainActivity.this, EditCredentialActivity.class));
        });


        EditText etSearch = findViewById(R.id.etSearch);

// İlk kaynak: tüm liste
        currentSource = vm.getCredentials();
        currentSource.observe(this, adapter::submit);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s.toString().trim();

                // Eski observer'ı kaldır
                if (currentSource != null) {
                    currentSource.removeObservers(MainActivity.this);
                }

                // Yeni kaynağı seç
                if (q.isEmpty()) {
                    currentSource = vm.getCredentials();
                } else {
                    currentSource = vm.search(q);
                }

                // Yeni kaynağı bağla
                currentSource.observe(MainActivity.this, adapter::submit);
            }
        });


    }
}
