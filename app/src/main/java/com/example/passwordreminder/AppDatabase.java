package com.example.passwordreminder;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

@Database(entities = {User.class, Credential.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract CredentialDao credentialDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context ctx) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    char[] passphrase = DbKeyStore.getOrCreatePassphrase(ctx.getApplicationContext());
                    SupportFactory factory = new SupportFactory(SQLiteDatabase.getBytes(passphrase));

                    INSTANCE = Room.databaseBuilder(ctx.getApplicationContext(), AppDatabase.class, "vault.db")
                            .openHelperFactory(factory)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
