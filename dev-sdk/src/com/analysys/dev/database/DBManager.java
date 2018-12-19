package com.analysys.dev.database;

import com.analysys.dev.internal.utils.reflectinon.EContextHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description:
 * @Version: 1.0
 * @Create: 2018/10/13 15:11
 * @Author: Wang-X-C
 */
public class DBManager {

    private static Context mContext = null;
    private static DBHelper dbHelper = null;
    private SQLiteDatabase db = null;

    public DBManager() {}

    private static class Holder {
        private static final DBManager INSTANCE = new DBManager();
    }

    public static synchronized DBManager getInstance(Context context) {
        mContext = EContextHelper.getContext(context);
        if (dbHelper == null) {
            dbHelper = DBHelper.getInstance(mContext);
        }
        return Holder.INSTANCE;
    }

    public synchronized SQLiteDatabase openDB() {
        db = dbHelper.getWritableDatabase();
        return db;
    }

    public synchronized void closeDB() {
        try {
            if (db != null) {
                db.close();
            }
        } finally {
            db = null;
        }
    }
}
