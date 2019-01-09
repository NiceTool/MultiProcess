package com.analysys.dev.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.analysys.dev.internal.Content.DeviceKeyContacts;
import com.analysys.dev.internal.impl.OCImpl;
import com.analysys.dev.utils.ELOG;
import com.analysys.dev.utils.Utils;
import com.analysys.dev.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/17 12:06
 * @Author: Wang-X-C
 */
public class TableOCCount {
    // sv 查询的值selectValue，iv写入的值insertValue
    //private final String sv = "0";
    //private final String iv = "1";

    private final String ZERO = "0";
    private final String ONE = "1";
    Context mContext;

    private static class Holder {
        private static final TableOCCount INSTANCE = new TableOCCount();
    }

    public static TableOCCount getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    /**
     * 存储数据
     */
    public void insert(JSONObject ocInfo) {
        try {
            if (ocInfo == null) {
                return;
            }
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            ContentValues cv = getContentValues(ocInfo);
            cv.put(DBConfig.OCCount.Column.CU, 0);
            db.insert(DBConfig.OCCount.TABLE_NAME, null, cv);
        } catch (Exception e) {
            ELOG.e(e);
        }
        DBManager.getInstance(mContext).closeDB();
    }

    /**
     * @param ocInfo
     */
    public void insertArray(List<JSONObject> ocInfo) {
        SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
        try {
            if (ocInfo != null && !ocInfo.isEmpty()) {
                db.beginTransaction();
                for (int i = 0; i < ocInfo.size(); i++) {
                    ContentValues cv = getContentValues(ocInfo.get(i));
                    cv.put(DBConfig.OCCount.Column.CU, 0);
                    db.insert(DBConfig.OCCount.TABLE_NAME, null, cv);
                }
                db.setTransactionSuccessful();
            }
        } catch (Throwable e) {
            ELOG.e(e);
        } finally {
            db.endTransaction();
        }
        DBManager.getInstance(mContext).closeDB();
    }

    /**
     * 该时段有应用操作记录，更新记录状态做缓存
     */
    public void update(JSONObject ocInfo) {
        try {
            if (ocInfo == null) {
                return;
            }
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            String day = Utils.getDay();
            int timeInterval = Utils.getTimeTag(System.currentTimeMillis());
            ContentValues cv = getContentValues(ocInfo);
            ELOG.i(ocInfo+"  ocInfo update()");
            db.update(DBConfig.OCCount.TABLE_NAME, cv,
                    DBConfig.OCCount.Column.APN + "=? and " + DBConfig.OCCount.Column.DY + "=? and "
                            + DBConfig.OCCount.Column.TI + "=? and " + DBConfig.OCCount.Column.RS + "=?",
                    new String[]{ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName), day, String.valueOf(timeInterval), ZERO});
        } catch (Throwable e) {
            ELOG.e(e);
        }
        DBManager.getInstance(mContext).closeDB();
    }

    /**
     * json数据转成ContentValues
     */
    private ContentValues getContentValues(JSONObject ocInfo) {
        ContentValues cv = null;
        if (ocInfo != null) {
            cv = new ContentValues();
            long insertTime = System.currentTimeMillis();
            String an = Utils.encrypt(ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationName), insertTime);
            ELOG.i(ocInfo.toString()+"     ocInfo  ");
            cv.put(DBConfig.OCCount.Column.APN, ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName));
            cv.put(DBConfig.OCCount.Column.AN, an);
            cv.put(DBConfig.OCCount.Column.AOT, ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationOpenTime));
            cv.put(DBConfig.OCCount.Column.DY, Utils.getDay());
            cv.put(DBConfig.OCCount.Column.IT, String.valueOf(insertTime));
            cv.put(DBConfig.OCCount.Column.AVC, ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationVersionCode));
            cv.put(DBConfig.OCCount.Column.NT, ocInfo.optString(DeviceKeyContacts.OCInfo.NetworkType));
            cv.put(DBConfig.OCCount.Column.AT, ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationType));
            cv.put(DBConfig.OCCount.Column.CT, ocInfo.optString(DeviceKeyContacts.OCInfo.CollectionType));
            cv.put(DBConfig.OCCount.Column.TI, Utils.getTimeTag(insertTime));
            cv.put(DBConfig.OCCount.Column.ST, ZERO);
            cv.put(DBConfig.OCCount.Column.RS, ONE);
        }
        return cv;
    }

    /**
     * 查询 正在运行的应用记录
     */
    public List<JSONObject> selectRunning() {
        List<JSONObject> list = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            list = new ArrayList<JSONObject>();
            Cursor cursor = db.query(DBConfig.OCCount.TABLE_NAME,
                    null,
                    DBConfig.OCCount.Column.RS + "=?", new String[]{ONE},
                    null, null, null);
            while (cursor.moveToNext()) {
                JSONObject job = new JSONObject();
                String insertTime = cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.IT));
                String appName = cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AN));
                job.put(DeviceKeyContacts.OCInfo.ApplicationPackageName, cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.APN)));
                job.put(DeviceKeyContacts.OCInfo.ApplicationName, Utils.decrypt(appName, Long.valueOf(insertTime)));
                job.put(DeviceKeyContacts.OCInfo.ApplicationOpenTime, cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AOT)));
                job.put(DeviceKeyContacts.OCInfo.ApplicationVersionCode, cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AVC)));
                job.put(DeviceKeyContacts.OCInfo.NetworkType, cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.NT)));
                job.put(DeviceKeyContacts.OCInfo.ApplicationType, cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AT)));
                job.put(DeviceKeyContacts.OCInfo.CollectionType, cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.CT)));
                list.add(job);
            }
        } catch (Throwable e) {
            ELOG.e(e);
        }
        return list;
    }

    /**
     * 更新缓存状态,从0到1
     */
    public void updateRunState(List<JSONObject> ocInfo) {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.OCCount.Column.RS, ONE);
            cv.put(DBConfig.OCCount.Column.ACT, String.valueOf(System.currentTimeMillis()));
            for (int i = 0; i < ocInfo.size(); i++) {
                String pkgName = ocInfo.get(i).optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                db.update(DBConfig.OCCount.TABLE_NAME, cv,
                        DBConfig.OCCount.Column.APN + "=? and "
                                + DBConfig.OCCount.Column.RS + "=?",
                        new String[]{pkgName, ZERO});
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            ELOG.e(e);
        } finally {
            db.endTransaction();
        }
        DBManager.getInstance(mContext).closeDB();
    }

    /**
     * 更新缓存状态，从1到0
     */
    public void updateStopState(List<JSONObject> ocInfo) {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.OCCount.Column.RS, ZERO);
            cv.put(DBConfig.OCCount.Column.ACT, String.valueOf(System.currentTimeMillis()));
            for (int i = 0; i < ocInfo.size(); i++) {
                String pkgName = ocInfo.get(i).optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                db.execSQL("update e_occ set occ_e = occ_e + 1 where occ_a = '" + pkgName + "'");
                db.update(DBConfig.OCCount.TABLE_NAME, cv,
                        DBConfig.OCCount.Column.APN + "=? and " + DBConfig.OCCount.Column.RS + "=?",
                        new String[]{pkgName, ONE});
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            ELOG.e(e);
        } finally {
            db.endTransaction();
        }
        DBManager.getInstance(mContext).closeDB();
    }

    /**
     * 判断当前时段，是否存在该应用的操作记录
     */
    public List<String> getIntervalApps() {
        List<String> list = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            String day = Utils.getDay();
            int timeInterval = Utils.getTimeTag(System.currentTimeMillis());
            Cursor cursor = db.query(DBConfig.OCCount.TABLE_NAME, new String[]{DBConfig.OCCount.Column.APN},
                    DBConfig.OCCount.Column.DY + "=? and "
                            + DBConfig.OCCount.Column.TI + "=? and "
                            + DBConfig.OCCount.Column.RS + "=?"
                    , new String[]{day, String.valueOf(timeInterval), ZERO},
                    null, null, null);
            list = new ArrayList<String>();
            while (cursor.moveToNext()) {
                String pkgName = cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.APN));
                if (!TextUtils.isEmpty(pkgName)) {
                    list.add(pkgName);
                }
            }
        } catch (Exception e) {
            ELOG.e(e);
        }
        DBManager.getInstance(mContext).closeDB();
        return list;
    }

    /**
     * 读取
     */
    public JSONArray select() {
        JSONArray ocCountJar = null;
        try {
            ocCountJar = new JSONArray();
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            Cursor cursor = db.query(DBConfig.OCCount.TABLE_NAME,
                    null, null, null,
                    null, null, null);
            while (cursor.moveToNext()) {
                JSONObject job = new JSONObject();
                String insertTime = cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.IT));
                String encryptAn = cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AN));
                String an = Utils.decrypt(encryptAn, Long.valueOf(insertTime));
                job.put(DeviceKeyContacts.OCInfo.ApplicationPackageName, cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.APN)));
                job.put(DeviceKeyContacts.OCInfo.ApplicationName, an);
                job.put(DeviceKeyContacts.OCInfo.CU, cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.CU)));
                job.put(DeviceKeyContacts.OCInfo.TI, cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.TI)));
                job.put(DeviceKeyContacts.OCInfo.DY, cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.DY)));
                ocCountJar.put(job);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        DBManager.getInstance(mContext).closeDB();
        return ocCountJar;
    }
}
