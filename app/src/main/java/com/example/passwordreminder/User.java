package com.example.passwordreminder;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = {"username"}, unique = true)})
public class User {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String username;

    @NonNull
    public String passwordHash; // salted hash

    @NonNull
    public String salt;

    @NonNull
    public String role; // "ADMIN" | "READONLY"

    public User(@NonNull String username, @NonNull String passwordHash, @NonNull String salt, @NonNull String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
    }
}
