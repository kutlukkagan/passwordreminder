package com.example.passwordreminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private VaultViewModel vm;

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
                vm.getRepo().deleteCredential(c);
                Toast.makeText(MainActivity.this, "Silindi", Toast.LENGTH_SHORT).show();
            }
        }, isAdmin);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        vm.getCredentials().observe(this, adapter::submit);

        btnNew.setOnClickListener(v -> {
            if (!isAdmin) return;
            startActivity(new Intent(MainActivity.this, EditCredentialActivity.class));
        });
    }
}
