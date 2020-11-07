package com.example.credpass;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.credpass.Dao.UserPassDataDao;
import com.example.credpass.Entity.UserPassDataBase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {UserPassDataBase.class},version = 1,exportSchema = false)
public abstract class   AppDatabase extends RoomDatabase {

    public abstract UserPassDataDao userPassDataDao();


    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 1;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "user_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
