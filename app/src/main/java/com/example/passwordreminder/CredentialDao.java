package com.example.passwordreminder;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface CredentialDao {

    @Query("SELECT * FROM credentials ORDER BY createdAt DESC")
    LiveData<List<Credential>> observeAll();

    @Query("SELECT * FROM credentials WHERE id = :id LIMIT 1")
    Credential getById(long id);

    @Query("SELECT * FROM credentials ORDER BY id DESC")
    LiveData<List<Credential>> getAll();

    @Insert
    long insert(Credential c);

    @Update
    int update(Credential c);

    @Delete
    int delete(Credential c);
}
