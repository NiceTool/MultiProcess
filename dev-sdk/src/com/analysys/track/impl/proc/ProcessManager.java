package com.analysys.track.impl.proc;

import org.json.JSONArray;
import org.json.JSONObject;

import com.analysys.track.database.TableXXXInfo;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.utils.ELOG;

import com.analysys.track.utils.sp.SPHelper;

import android.content.Context;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class ProcessManager {
    // 是否开启工作
    private volatile static boolean isCollected = true;

    public static void setIsCollected(boolean isCollected) {
        ProcessManager.isCollected = isCollected;
    }

    public static Set<String> getRunningForegroundApps(Context ctx) {
        Set<String> nameSet = null;
        JSONArray uploadArray = null;
        if (isCollected) {
            ELOG.i("start===>" + isCollected);
            try {
                uploadArray = new JSONArray();
                JSONObject obj = ProcParser.getRunningInfo(ctx);
                uploadArray.put(obj);

                TableXXXInfo.getInstance(ctx).insert(uploadArray);
                //获取正在运行的nameList
                String[] strArray = null;
                nameSet = new HashSet<String>();
                JSONObject jsonObject = null;
                for (int i = 0; i < uploadArray.length(); i++) {
                    jsonObject = (JSONObject)uploadArray.get(i);
                    String res = jsonObject.get(ProcParser.RUNNING_RESULT).toString().replace("[", "").replace("]", "");
                    strArray = res.split(",");
                }
                for (int i = 0; i < strArray.length; i++) {
                    nameSet.add(strArray[i]);
                }

            } catch (Exception e) {
                ELOG.i(e.getMessage() + "   getRunningForegroundApps()");
            }
        }
        return nameSet;
    }

    public static void saveSP(Context ctx, JSONObject ocInfo){
        SPHelper.setLastOpenPackgeName(ctx, ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName));
        SPHelper.setLastOpenTime(ctx, ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationOpenTime));
        SPHelper.setLastAppName(ctx, ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationName));
        SPHelper.setLastAppVerison(ctx, ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationVersionCode));
        SPHelper.setAppType(ctx,ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationType));
    }


}
