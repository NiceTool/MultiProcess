package com.analysys.track.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.analysys.track.internal.content.DataController;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.StreamerUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: APP SNAPSHOT 操作
 * @Version: 1.0
 * @Create: 2019-08-12 14:25:43
 * @author: LY
 */
public class TableAppSnapshot {


    /**
     * 覆盖新增数据，多条
     */
    public void coverInsert(List<JSONObject> snapshots) {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            db.beginTransaction();
            db.delete(DBConfig.AppSnapshot.TABLE_NAME, null, null);
            JSONObject snapshot = null;
            for (int i = 0; i < snapshots.size(); i++) {
                snapshot = null;
                snapshot = (JSONObject) snapshots.get(i);
                db.insert(DBConfig.AppSnapshot.TABLE_NAME, null, getContentValues(snapshot));
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
        } finally {
            if (db != null && db.inTransaction()) {
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
            db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
//            if (!db.isOpen()) {
//                db = DBManager.getInstance(mContext).openDB();
//            }
            if (EGContext.DEBUG_SNAP) {
                ELOG.d("sanbo.snap", " 写入数据库: " + snapshots.toString());
            }
            db.insert(DBConfig.AppSnapshot.TABLE_NAME, null, getContentValues(snapshots));
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    private ContentValues getContentValues(JSONObject snapshot) {
        ContentValues cv = new ContentValues();

//        ELOG.i(an+ " getContentValues  an");
        // APN 加密
        cv.put(DBConfig.AppSnapshot.Column.APN, EncryptUtils.encrypt(mContext,
                snapshot.optString(UploadKey.AppSnapshotInfo.ApplicationPackageName)));
        //AN 加密
        String an = EncryptUtils.encrypt(mContext,
                snapshot.optString(UploadKey.AppSnapshotInfo.ApplicationName));
        cv.put(DBConfig.AppSnapshot.Column.AN, an);
        //AVC 加密
        cv.put(DBConfig.AppSnapshot.Column.AVC, EncryptUtils.encrypt(mContext,
                snapshot.optString(UploadKey.AppSnapshotInfo.ApplicationVersionCode)));
        // AT 加密
        cv.put(DBConfig.AppSnapshot.Column.AT,
                EncryptUtils.encrypt(mContext, snapshot.optString(UploadKey.AppSnapshotInfo.ActionType)));
        cv.put(DBConfig.AppSnapshot.Column.AHT, snapshot.optString(UploadKey.AppSnapshotInfo.ActionHappenTime));
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
            if (db == null) {
                return map;
            }
            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, null, null, null, null, null, null);
            map = new HashMap<String, String>();
            while (cursor.moveToNext()) {
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
                    return map;
                }
                String apn = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN)));
                if (!TextUtils.isEmpty(apn)) {
                    map.put(apn, String.valueOf(getCursor(cursor)));
                } else {
                    blankCount += 1;
                }
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return map;
    }

    private JSONObject getCursor(Cursor cursor) {
        JSONObject jsonObj = null;
        String pkgName = "";
        try {
            jsonObj = new JSONObject();

            //APN 加密
            pkgName = EncryptUtils.decrypt(mContext,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN)));
            JsonUtils.pushToJSON(mContext, jsonObj, UploadKey.AppSnapshotInfo.ApplicationPackageName, pkgName,
                    DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
            //AN 加密
            String an = EncryptUtils.decrypt(mContext,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AN)));
            JsonUtils.pushToJSON(mContext, jsonObj, UploadKey.AppSnapshotInfo.ApplicationName, an,
                    DataController.SWITCH_OF_APPLICATION_NAME);

            //AVC 加密
            JsonUtils.pushToJSON(mContext, jsonObj, UploadKey.AppSnapshotInfo.ApplicationVersionCode,
                    EncryptUtils.decrypt(mContext,
                            cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AVC))),
                    DataController.SWITCH_OF_APPLICATION_VERSION_CODE);

            //AT 加密
            JsonUtils.pushToJSON(mContext, jsonObj, UploadKey.AppSnapshotInfo.ActionType,
                    EncryptUtils.decrypt(mContext,
                            cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AT))),
                    DataController.SWITCH_OF_ACTION_TYPE);

            //AHT 不加密
            JsonUtils.pushToJSON(mContext, jsonObj, UploadKey.AppSnapshotInfo.ActionHappenTime,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AHT)),
                    DataController.SWITCH_OF_ACTION_HAPPEN_TIME);
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
        return jsonObj;
    }

    /**
     * 更新应用标识状态
     */
    public void update(String pkgName, String appTag, long time) {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            ContentValues cv = new ContentValues();
            // AT 加密
            cv.put(DBConfig.AppSnapshot.Column.AT, EncryptUtils.encrypt(mContext, appTag));
            cv.put(DBConfig.AppSnapshot.Column.AHT, time);
            // APN 加密
            db.update(DBConfig.AppSnapshot.TABLE_NAME, cv, DBConfig.AppSnapshot.Column.APN + "= ? ",
                    new String[]{EncryptUtils.encrypt(mContext, pkgName)});

            if (EGContext.DEBUG_SNAP) {
                ELOG.d("sanbo.snap", " 更新信息: " + cv.toString());
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }

        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public boolean isHasPkgName(String pkgName) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return false;
            }
            //APN 加密
            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, null, DBConfig.AppSnapshot.Column.APN + "=?",
                    new String[]{EncryptUtils.encrypt(mContext, pkgName)}, null, null, null);
            if (cursor.getCount() == 0) {
                return false;
            } else {
                return true;
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return false;
    }

    /**
     * 数据查询，格式：{JSONObject}
     */
    public JSONArray select(long maxLength) {
        JSONArray array = null;
        Cursor cursor = null;
        int blankCount = 0, countNum = 0;
        JSONObject jsonObject = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return array;
            }
            array = new JSONArray();
            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, null, null, null, null, null, null, "4000");
            if (cursor == null) {
                return array;
            }
            while (cursor.moveToNext()) {
                countNum++;
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
                    return array;
                }
                //APN 加密
                String pkgName = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN)));
                if (!TextUtils.isEmpty(pkgName)) {
                    jsonObject = getCursor(cursor);
                } else {
                    blankCount += 1;
                    continue;
                }
                if (countNum / 300 > 0) {
                    countNum = countNum % 300;
                    long size = String.valueOf(array).getBytes().length;
                    if (size >= maxLength * 9 / 10) {
//                        ELOG.e(" size值：："+size+" maxLength = "+maxLength);
                        UploadImpl.isChunkUpload = true;
                        break;
                    } else {
                        array.put(jsonObject);
                    }
                } else {
                    array.put(jsonObject);
                }
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
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
            // AT 加密
            db.delete(DBConfig.AppSnapshot.TABLE_NAME, DBConfig.AppSnapshot.Column.AT + "=?",
                    new String[]{EncryptUtils.encrypt(mContext, EGContext.SNAP_SHOT_UNINSTALL)});
//            ELOG.e("AppSnapshot 删除行数：：："+co);
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
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
            if (db == null) {
                return;
            }
            ContentValues cv = new ContentValues();
            // AT 加密
            cv.put(DBConfig.AppSnapshot.Column.AT, EncryptUtils.encrypt(mContext, EGContext.SNAP_SHOT_INSTALL));
            db.update(DBConfig.AppSnapshot.TABLE_NAME, cv, null, null);
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public void deleteAll() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            db.delete(DBConfig.AppSnapshot.TABLE_NAME, null, null);
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    private static class Holder {
        private static final TableAppSnapshot INSTANCE = new TableAppSnapshot();
    }

    public static TableAppSnapshot getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    private TableAppSnapshot() {
    }

    private Context mContext;

}