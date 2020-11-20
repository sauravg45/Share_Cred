package com.example.credpass.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.credpass.Dao.UserPassDataDao;
import com.example.credpass.Entity.UserPassDataBase;

@Database(entities = {UserPassDataBase.class},version = 1,exportSchema = false)
public abstract class   AppDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();

    private static AppDatabase INSTANCE;

    public static  AppDatabase getAppDatabase(final Context context){
        if (INSTANCE == null) {
            synchronized (LOCK) {

                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "user-db").build();


            }
        }
        return INSTANCE;
    }

    public abstract UserPassDataDao userPassDataDao();

}
