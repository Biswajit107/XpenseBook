package com.app.biswajit.xpensebook.database;

import android.content.Context;

import com.app.biswajit.xpensebook.TimestampConverter;
import com.app.biswajit.xpensebook.dao.ExpenseDao;
import com.app.biswajit.xpensebook.dao.MessageDao;
import com.app.biswajit.xpensebook.entity.Expense;
import com.app.biswajit.xpensebook.entity.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Message.class, Expense.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MessageDao messageDao();
    public abstract ExpenseDao expenseDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "expense_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
