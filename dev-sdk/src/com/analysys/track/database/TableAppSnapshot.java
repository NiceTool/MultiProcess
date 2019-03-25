package com.analysys.track.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.analysys.track.internal.Content.DataController;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class TableAppSnapshot {

    Context mContext;

    private static class Holder {
        private static final TableAppSnapshot INSTANCE = new TableAppSnapshot();
    }

    public static TableAppSnapshot getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    /**
     * 覆盖新增数据，多条
     */
    public void coverInsert(JSONArray snapshots) {
        SQLiteDatabase db = null;
        try {
//            if(!DBUtils.isValidData(mContext,EGContext.FILES_SYNC_APPSNAPSHOT)){
//                return;
//            }
                db = DBManager.getInstance(mContext).openDB();
                db.beginTransaction();
                db.delete(DBConfig.AppSnapshot.TABLE_NAME,null,null);
                for (int i = 0; i < snapshots.length(); i++) {
                    JSONObject snapshot = (JSONObject) snapshots.get(i);
                    db.insert(DBConfig.AppSnapshot.TABLE_NAME, null,
                        getContentValues(snapshot));
                }
                db.setTransactionSuccessful();
        } catch (Throwable e) {
        } finally {
            if(db != null){
                db.endTransaction();
            }
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * 新增数据，用于安装广播
     */
    public void insert(JSONObject snapshots) {
        SQLiteDatabase db = null;
        try {
//            if(!DBUtils.isValidData(mContext,EGContext.FILES_SYNC_APPSNAPSHOT)){
//                return;
//            }
            db = DBManager.getInstance(mContext).openDB();
            if (db == null){
                return;
            }
            db.insert(DBConfig.AppSnapshot.TABLE_NAME, null, getContentValues(snapshots));
        } catch (Throwable e) {
        }finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    private ContentValues getContentValues(JSONObject snapshot) {
        ContentValues cv = new ContentValues();
        cv.put(DBConfig.AppSnapshot.Column.APN,
            snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ApplicationPackageName));
        cv.put(DBConfig.AppSnapshot.Column.AN,
            snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ApplicationName));
        cv.put(DBConfig.AppSnapshot.Column.AVC,
            snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ApplicationVersionCode));
        cv.put(DBConfig.AppSnapshot.Column.AT,
            snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ActionType));
        cv.put(DBConfig.AppSnapshot.Column.AHT,
            snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ActionHappenTime));
        return cv;
    }

    /**
     * 数据查询，格式：<pkgName,JSONObject>
     */
    public Map<String, String> snapShotSelect() {
        Map<String, String> map = null;
        Cursor cursor = null;
        int blankCount = 0;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if(db == null){
                return map;
            }
            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, null, null, null, null,
                null, null);
            map = new HashMap<String, String>();
            while (cursor.moveToNext()) {
                if(blankCount >= EGContext.BLANK_COUNT_MAX){
                    return map;
                }
                String apn = cursor
                        .getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN));
                if(!TextUtils.isEmpty(apn)){
                    map.put(apn, String.valueOf(getCursor(cursor)));
                }else {
                    blankCount += 1;
                }
            }
        } catch (Throwable e) {
        } finally {
            if (cursor != null)
                cursor.close();
            DBManager.getInstance(mContext).closeDB();
        }
        return map;
    }

    private JSONObject getCursor(Cursor cursor) {
        JSONObject jsonObj = null;
        String pkgName = "";
        try {
            jsonObj = new JSONObject();
            pkgName = cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN));

            JsonUtils.pushToJSON(mContext,jsonObj,DeviceKeyContacts.AppSnapshotInfo.ApplicationPackageName,pkgName,DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
            JsonUtils.pushToJSON(mContext,jsonObj,DeviceKeyContacts.AppSnapshotInfo.ApplicationName,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AN)),DataController.SWITCH_OF_APPLICATION_NAME);
            JsonUtils.pushToJSON(mContext,jsonObj,DeviceKeyContacts.AppSnapshotInfo.ApplicationVersionCode,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AVC)),DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
            JsonUtils.pushToJSON(mContext,jsonObj,DeviceKeyContacts.AppSnapshotInfo.ActionType,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AT)),DataController.SWITCH_OF_ACTION_TYPE);
            JsonUtils.pushToJSON(mContext,jsonObj,DeviceKeyContacts.AppSnapshotInfo.ActionHappenTime,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AHT)),DataController.SWITCH_OF_ACTION_HAPPEN_TIME);
        } catch (Throwable e) {
        }
        return jsonObj;
    }

    /**
     * 更新应用标识状态
     */
    public void update(String pkgName, String appTag) {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if(db == null){
                return;
            }
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.AppSnapshot.Column.AT, appTag);
            db.update(DBConfig.AppSnapshot.TABLE_NAME, cv,
                DBConfig.AppSnapshot.Column.APN + "= ? ", new String[] {pkgName});
        } catch (Throwable e) {
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public boolean isHasPkgName(String pkgName) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if(db == null){
                return false;
            }
            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME,null,DBConfig.AppSnapshot.Column.APN + "=?",
                    new String[] {pkgName},null,null,null);
//            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME,
//                new String[] {DBConfig.AppSnapshot.Column.APN},
//                DBConfig.AppSnapshot.Column.APN + "=?", new String[] {pkgName}, null,
//                null, null);
            if (cursor.getCount() == 0) {
                ELOG.i(cursor.getCount()+" cursor.getCount() ");
                return false;
            } else {
                return true;
            }
        } catch (Throwable e) {
        } finally {
            if (cursor != null)
                cursor.close();
            DBManager.getInstance(mContext).closeDB();
        }
        return false;
    }

    /**
     * 数据查询，格式：{JSONObject}
     */
    public JSONArray select() {
        JSONArray array = null;
        Cursor cursor = null;
        int blankCount = 0;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if(db == null){
                return array;
            }
            array = new JSONArray();
            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, null, null, null, null,
                null, null);
            if(cursor == null){
                return array;
            }
            while (cursor.moveToNext()) {
                if(blankCount >= EGContext.BLANK_COUNT_MAX){
                    return array;
                }
                String pkgName = cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN));
                if(!TextUtils.isEmpty(pkgName)){
                    array.put(getCursor(cursor));
                }else {
                    blankCount += 1;
                    continue;
                }

            }
        } catch (Throwable e) {
            ELOG.e(e+" select() ");
        } finally {
            if (cursor != null){
                cursor.close();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }

    public void delete() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            db.delete(DBConfig.AppSnapshot.TABLE_NAME, DBConfig.AppSnapshot.Column.AT + "=?", new String[] {EGContext.SNAP_SHOT_UNINSTALL});

        } catch (Throwable e) {
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }
    /**
     * 更新应用标识状态
     */
    public void update() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if(db == null){
                return;
            }
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.AppSnapshot.Column.AT, EGContext.SNAP_SHOT_INSTALL);
            db.update(DBConfig.AppSnapshot.TABLE_NAME, cv,
                    null, null);
        } catch (Throwable e) {
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }
}