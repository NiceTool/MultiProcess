package com.analysys.track.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.Base64Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class TableLocation {

    Context mContext;
    String INSERT_STATUS_DEFAULT = "0";
    String INSERT_STATUS_READ_OVER = "1";

    private TableLocation(){}
    private static class Holder {
        private static final TableLocation INSTANCE = new TableLocation();
    }

    public static TableLocation getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    public void insert(JSONObject locationInfo) {
        try {
            ContentValues cv = null;
            String locationTime = null;
            long time = -1;
            String encryptLocation = null;
            if (locationInfo != null && locationInfo.length() > 0) {
                locationTime = null;
                locationTime = locationInfo.optString(DeviceKeyContacts.LocationInfo.CollectionTime);
                time = 0;
                if(!TextUtils.isEmpty(locationTime)){
                   time = Long.parseLong(locationTime);
                }
                encryptLocation = Base64Utils.encrypt(String.valueOf(locationInfo), time);
                if (!TextUtils.isEmpty(encryptLocation)) {
                    cv = new ContentValues();
                    cv.put(DBConfig.Location.Column.LI, EncryptUtils.encrypt(mContext,encryptLocation));
                    cv.put(DBConfig.Location.Column.IT, locationTime);
                    cv.put(DBConfig.Location.Column.ST, INSERT_STATUS_DEFAULT);
                    cv.put(DBConfig.Location.Column.L_RA, EGContext.SDK_VERSION);
                    SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
                    if(db == null){
                        return;
                    }
                    db.insert(DBConfig.Location.TABLE_NAME, null, cv);
                }
            }
        } catch (Throwable e) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(e);
            }
        }finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public JSONArray select() {
        JSONArray array = null;
        int blankCount = 0;
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            array = new JSONArray();
            db = DBManager.getInstance(mContext).openDB();
            if(db == null){
               return array;
            }
            db.beginTransaction();
            cursor = db.query(DBConfig.Location.TABLE_NAME, null,
                    null, null, null, null, null);
            String id = "",encryptLocation = "",time = "",version = "";
            long timeStamp = 0;
            JSONObject jsonObject = null;
            while (cursor.moveToNext()) {
                if(blankCount >= EGContext.BLANK_COUNT_MAX){
                    return array;
                }
                id = cursor.getString(cursor.getColumnIndex(DBConfig.Location.Column.ID));
                encryptLocation = cursor.getString(cursor.getColumnIndex(DBConfig.Location.Column.LI));
                time = cursor.getString(cursor.getColumnIndex(DBConfig.Location.Column.IT));
                version = cursor.getString(cursor.getColumnIndex(DBConfig.Location.Column.L_RA));
                ContentValues cv = new ContentValues();
                cv.put(DBConfig.Location.Column.ST, INSERT_STATUS_READ_OVER);
                db.update(DBConfig.Location.TABLE_NAME, cv, DBConfig.Location.Column.ID + "=?", new String[]{id});
                if(!TextUtils.isEmpty(time)){
                    timeStamp = Long.parseLong(time);
                }
                String decryptLocation = Base64Utils.decrypt(EncryptUtils.decrypt(mContext,encryptLocation), timeStamp);
                if(!TextUtils.isEmpty(decryptLocation)){
                    jsonObject = new JSONObject();
                    jsonObject.put(EGContext.LOCATION_INFO,decryptLocation);
                    jsonObject.put(EGContext.VERSION,version);
                    array.put(jsonObject);
                } else {
                    blankCount += 1;
                }
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(e);
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
            if(db != null && db.inTransaction()){
                db.endTransaction();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }

    public void delete() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if(db == null) {
                return;
            }
            db.delete(DBConfig.Location.TABLE_NAME, DBConfig.Location.Column.ST + "=?", new String[]{INSERT_STATUS_READ_OVER});
        } catch (Throwable e) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(e);
            }
        }finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }
    public void deleteAll() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if(db == null) {
                return;
            }
            db.delete(DBConfig.Location.TABLE_NAME, null, null);
        } catch (Throwable e) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(e);
            }
        }finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }
}
