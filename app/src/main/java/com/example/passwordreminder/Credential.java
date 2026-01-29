package com.example.passwordreminder;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "credentials")
public class Credential {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String title;     // "Gmail", "VPN", "Banka" vs

    @NonNull
    public String username;

    @NonNull
    public String password;  // DB şifreli olacağı için burada plain tutulabilir (ama DB şifreli!)

    public long createdAt;

    public Credential(@NonNull String title, @NonNull String username, @NonNull String password, long createdAt) {
        this.title = title;
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
    }
}

