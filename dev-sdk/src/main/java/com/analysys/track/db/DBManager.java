package com.analysys.track.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.analysys.track.utils.EContextHelper;

import java.util.concurrent.atomic.AtomicInteger;

public class DBManager {

    private static Context mContext = null;
    private static DBHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private AtomicInteger mOpenWriteCounter = new AtomicInteger();

    public DBManager() {
    }

    public static synchronized DBManager getInstance(Context context) {
        if (mContext == null) {
            mContext = EContextHelper.getContext();
        }
        if (dbHelper == null) {
            dbHelper = DBHelper.getInstance(mContext);
        }
        return Holder.INSTANCE;
    }

    public synchronized SQLiteDatabase openDB() {
        try {
            if (mOpenWriteCounter.incrementAndGet() == 1) {
                // Opening new database
                db = dbHelper.getWritableDatabase();
            }
        } catch (Throwable e) {
        }
        return db;
    }

    public synchronized void closeDB() {
        try {
            if (mOpenWriteCounter.decrementAndGet() == 0) {
                // Closing database
                if (db != null) {
                    db.close();
                }
            }
        } catch (Throwable e) {
        }
    }

    private static class Holder {
        private static final DBManager INSTANCE = new DBManager();
    }
}
