package com.example.passwordreminder;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class VaultViewModel extends AndroidViewModel {

    private final VaultRepository repo;
    private final LiveData<List<Credential>> credentials;

    public VaultViewModel(@NonNull Application app) {
        super(app);
        repo = new VaultRepository(app);

        // ❌ kaldırıldı:
        // repo.ensureDefaultAdminIfEmpty();

        credentials = repo.getAllCredentials();
    }

    public LiveData<List<Credential>> getCredentials() {
        return credentials;
    }

    public VaultRepository getRepo() {
        return repo;
    }
}

