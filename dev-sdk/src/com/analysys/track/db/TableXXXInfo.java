package com.analysys.track.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Base64;

import com.analysys.track.internal.content.DataController;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.oc.ProcUtils;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.StreamerUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: xxxinfo写入DB
 * @Version: 1.0
 * @Create: 2019-08-12 19:04:48
 * @author: sanbo
 */
public class TableXXXInfo {

    /**
     * 存储数据
     */
    public void insert(JSONObject xxxInfo) {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            if (db == null || xxxInfo == null || xxxInfo.length() < 1) {
                return;
            }
            ContentValues cv = getContentValues(xxxInfo);
            // 防止因为传递控制导致的写入异常
            if (cv.size() > 1) {
                db.insert(DBConfig.XXXInfo.TABLE_NAME, null, cv);
            }

        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }

    }

    /**
     * json数据转成ContentValues
     */
    private ContentValues getContentValues(JSONObject xxxInfo) {
        ContentValues cv = new ContentValues();
        //样例数据: {"time":1563676428130,"ocr":["com.alipay.hulu","com.device"],"result":[{"pid":4815,"oomScore":41,"pkg":"com.device","cpuset":"\/foreground","cgroup":"3:cpuset:\/foreground\n2:cpu:\/\n1:cpuacct:\/uid_10219\/pid_4815","oomAdj":"0"},{"pid":3644,"oomScore":95,"pkg":"com.alipay.hulu","cpuset":"\/foreground","cgroup":"3:cpuset:\/foreground\n2:cpu:\/\n1:cpuacct:\/uid_10131\/pid_3644","oomAdj":"1"}]}
        if (xxxInfo.has(ProcUtils.RUNNING_RESULT)) {
            String result = xxxInfo.optString(ProcUtils.RUNNING_RESULT);
            if (!TextUtils.isEmpty(result)) {
                // PROC
                cv.put(DBConfig.XXXInfo.Column.PROC, EncryptUtils.encrypt(mContext, result));
                // time 不加密
                if (xxxInfo.has(ProcUtils.RUNNING_TIME)) {
                    long time = xxxInfo.optLong(ProcUtils.RUNNING_TIME);
                    cv.put(DBConfig.XXXInfo.Column.TIME, String.valueOf(time));
                } else {
                    cv.put(DBConfig.XXXInfo.Column.TIME, String.valueOf(System.currentTimeMillis()));
                }
            }
        }
        return cv;
    }

    public void delete() {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            db.delete(DBConfig.XXXInfo.TABLE_NAME, null, null);
        } catch (Throwable e) {
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * 按时间删除
     *
     * @param idList
     */
    public void deleteByID(List<String> idList) {
        SQLiteDatabase db = null;
        try {
            if (idList == null || idList.size() < 1) {
                return;
            }
            db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            for (int i = 0; i < idList.size(); i++) {
                String id = idList.get(i);
                if (TextUtils.isEmpty(id)) {
                    return;
                }
                db.delete(DBConfig.XXXInfo.TABLE_NAME, DBConfig.XXXInfo.Column.ID + "=?", new String[]{id});
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    // 连表查询
    public JSONArray select(long maxLength) {
        JSONArray array = null;
        Cursor cursor = null;
        int blankCount = 0, countNum = 0;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return array;
            }
            array = new JSONArray();
            cursor = db.query(DBConfig.XXXInfo.TABLE_NAME, null, null, null, null, null, null, "2000");
            JSONObject jsonObject = null;
            String proc = null;
            while (cursor.moveToNext()) {
                countNum++;
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
                    return array;
                }
                jsonObject = new JSONObject();
                String id = cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.ID));
                if (TextUtils.isEmpty(id)) {
                    blankCount += 1;
                }
                proc = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.PROC)));
                if (!TextUtils.isEmpty(proc) && !"null".equalsIgnoreCase(proc)) {
                    JsonUtils.pushToJSON(mContext, jsonObject, ProcUtils.RUNNING_RESULT, new JSONArray(proc),
                            DataController.SWITCH_OF_CL_MODULE_PROC);
                    if (jsonObject == null || jsonObject.length() < 1) {
                        return array;
                    } else {
                        // time 不加密
                        String time = cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.TIME));
                        JsonUtils.pushToJSON(mContext, jsonObject, ProcUtils.RUNNING_TIME, time,
                                DataController.SWITCH_OF_RUNNING_TIME);
                        if (countNum / 300 > 0) {
                            countNum = countNum % 300;
                            long size = String.valueOf(array).getBytes().length;
                            if (size >= maxLength * 9 / 10) {
//                                ELOG.e(" size值：："+size+" maxLength = "+maxLength);
                                UploadImpl.isChunkUpload = true;
                                break;
                            } else {
                                UploadImpl.idList.add(id);
                                array.put(new String(
                                        Base64.encode(String.valueOf(jsonObject).getBytes(), Base64.DEFAULT)));
                            }
                        } else {
                            UploadImpl.idList.add(id);
                            array.put(new String(Base64.encode(String.valueOf(jsonObject).getBytes(), Base64.DEFAULT)));
                        }

                    }
                } else {
                    return array;
                }

            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
            array = null;
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }

    private static class Holder {
        private static final TableXXXInfo INSTANCE = new TableXXXInfo();
    }

    private Context mContext;

    private TableXXXInfo() {
    }

    public static TableXXXInfo getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

}
