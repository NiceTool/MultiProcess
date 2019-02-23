package com.analysys.dev.internal.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.analysys.dev.database.TableOC;
import com.analysys.dev.database.TableOCCount;
import com.analysys.dev.database.TableXXXInfo;
import com.analysys.dev.internal.Content.DeviceKeyContacts;
import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.internal.impl.proc.AppProcess;
import com.analysys.dev.internal.impl.proc.ProcParser;
import com.analysys.dev.internal.impl.proc.Process;
import com.analysys.dev.internal.impl.proc.ProcessManager;
import com.analysys.dev.model.AppSnapshotInfo;
import com.analysys.dev.service.AnalysysAccessibilityService;
import com.analysys.dev.utils.AccessibilityHelper;
import com.analysys.dev.utils.ELOG;
import com.analysys.dev.utils.EThreadPool;
import com.analysys.dev.utils.NetworkUtils;
import com.analysys.dev.utils.PermissionUtils;
import com.analysys.dev.utils.Utils;
import com.analysys.dev.utils.reflectinon.EContextHelper;
import com.analysys.dev.internal.work.MessageDispatcher;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

public class OCImpl {

    Context mContext;
    private final String SHELL_PM_LIST_PACKAGES = "pm list packages";//all
    private final String SHELL_SYSTEM_LIST_PACKAGES = "pm list packages -s";//system
    private final String SHELL_THIRD_PARTY_LIST_PACKAGES = "pm list packages -3";//third party


    private static class Holder {
        private static final OCImpl INSTANCE = new OCImpl();
    }

    public static OCImpl getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    /**
     * OC 信息采集
     */
    public void ocInfo() {
        EThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (!AccessibilityHelper.isAccessibilitySettingsOn(mContext,AnalysysAccessibilityService.class)) {
                    getInfoByVersion();
                }else{
                    //利用辅助功能获取当前app
                }
            }
        });
    }

    String pkgName = null;
    public void getInfoByVersion(){
        // 判断系统版本
        if (Build.VERSION.SDK_INT < 21) {
            if(PermissionUtils.checkPermission(mContext, Manifest.permission.GET_TASKS)){
                RunningApps(getRunningApp(), EGContext.OC_COLLECTION_TYPE_RUNNING_TASK);
            }
            MessageDispatcher.getInstance(mContext).ocInfo(EGContext.OC_CYCLE);
        }else if(Build.VERSION.SDK_INT > 20 && Build.VERSION.SDK_INT < 24 ){
            getProcApps();
            MessageDispatcher.getInstance(mContext).ocInfo(EGContext.OC_CYCLE_OVER_5);
        }else{
            //TODO 7.0以上待调研
        }
    }
    /**
     * getRunningTask、辅助功能 OC 信息采集
     */
    public void RunningApps(String pkgName, int collectionType) {
        try {
            this.pkgName = pkgName;
            List<JSONObject> cacheApps = TableOCCount.getInstance(mContext).selectRunning();
            if (cacheApps != null && !cacheApps.isEmpty()) {
                removeRepeat(cacheApps);
                if (!cacheApps.isEmpty()) {
                    // 完成一次闭合，存储到OC表
                    TableOC.getInstance(mContext).insert(cacheApps);
                    // 一次应用操作闭合，更新OCCunt表，打开次数、应用运行状态
                    TableOCCount.getInstance(mContext).updateStopState(cacheApps);
                }
                if (!TextUtils.isEmpty(pkgName)) {
                    updateCache(collectionType);
                }
            } else {
                updateCache(collectionType);
            }
        } catch (Throwable e) {
            ELOG.e(e);
        }
    }

    /**
     * 获取正在运行的应用包名
     */
    private String getRunningApp() {
        String pkgName = null;
        try {
            ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
            @SuppressWarnings("deprecation")
            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
            pkgName = tasks.get(0).topActivity.getPackageName();
        } catch (Throwable e) {
            ELOG.e(e);
        }
        return pkgName;
    }

    /**
     * 从Proc中读取数据
     */
    private void getProcApps() {
        List<JSONObject> cacheApps = TableOCCount.getInstance(mContext).selectRunning();
        JSONObject obj = ProcessManager.getRunningForegroundApps(mContext);
        List<Process> run = new ArrayList<Process>();
        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray xxxArray = new JSONArray(obj.get("XXXInfo").toString());
            TableXXXInfo.getInstance(mContext).insert(xxxArray);
            for (int i = 0;i<xxxArray.length();i++) {
                jsonObject = (JSONObject) xxxArray.get(i);
                String res = jsonObject.get(ProcParser.RUNNING_RESULT).toString().replace("[","").replace("]","");
                String[] strArray = res.split(",");
                Process ap = null;
                for (String pkgName : strArray) {
                    ap =  new Process(null,pkgName);
                    run.add(ap);
                }
            }
            if (cacheApps == null || cacheApps.isEmpty()) {
                List<JSONObject> ocList = new ArrayList<JSONObject>();
                for (int i = 0; i < run.size(); i++) {
                    String pkgName = run.get(i).getName();
                    if (!TextUtils.isEmpty(pkgName)) {
                        ocList.add(getOCInfo(pkgName.replaceAll(" ",""), EGContext.OC_COLLECTION_TYPE_PROC));
                    }
                }
                TableOCCount.getInstance(mContext).insertArray(ocList);
            } else {
                // 去重
                removeRepeat(cacheApps, run);
                // 更新缓存表
                updateCacheState(cacheApps);
                // 存储关闭信息到OC表
                TableOC.getInstance(mContext).insert(cacheApps);
                // 新增该时段缓存信息
                addCache(run);
            }
        }catch (Throwable t){
            ELOG.i("getProcApps has an exception :::"+ t.getMessage());
        }
    }

    /**
     * 缓存中应用列表与新获取应用列表去重
     */
    private void removeRepeat(List<JSONObject> cacheApps, List<Process> runApps) {
        JSONObject ocInfo = null;
        for (int i = cacheApps.size() - 1; i >= 0; i--) {
            ocInfo = cacheApps.get(i);
            String apn = ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
            for (int j = runApps.size() - 1; j >= 0; j--) {
                String pkgName = runApps.get(j).getName();
                if (!TextUtils.isEmpty(apn) && apn.equals(pkgName)) {
                    cacheApps.remove(i);
                    runApps.remove(j);
                    continue;
                }
            }
        }
    }

    /**
     * 更新缓存表
     */
    private void updateCacheState(List<JSONObject> cacheApps) {
        try {
            if (!cacheApps.isEmpty()) {
                // 缓存数据列表与新获取数据列表去重，缓存列表剩余为已经关闭的应用，需要转存储到OC表，并更新运行状态为0
                List<JSONObject> ocList = new ArrayList<JSONObject>();
                JSONObject oc = null;
                for (int i = 0; i < cacheApps.size(); i++) {
                    oc = cacheApps.get(i);
                    int numb = oc.optInt(DeviceKeyContacts.OCInfo.CU) + 1;
                    String apn = oc.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                    oc.remove(DeviceKeyContacts.OCInfo.CU);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationPackageName, apn);
                    jsonObject.put(DeviceKeyContacts.OCInfo.CU, numb);
                    ocList.add(jsonObject);
                }
                TableOCCount.getInstance(mContext).updateStopState(ocList);
            }
        } catch (Throwable e) {
            ELOG.e(e);
        }
    }

    /**
     * 新增缓存
     */
    private void addCache(List<Process> runApps) {
        if (!runApps.isEmpty()) {
            // 缓存数据列表与新获取数据列表去重，新获取列表剩余未新打开的应用，需要缓存到OCCount中，
            List<String> ocInfo = TableOCCount.getInstance(mContext).getIntervalApps();
            List<JSONObject> runList = getOCArray(runApps);
            List<JSONObject> updateOCInfo = new ArrayList<JSONObject>();
            // 将新增列表拆开，该时段有应用打开记录的修改更新记录，该时段没有应用打开记录的新增记录
            for (int i = runList.size() - 1; i >= 0; i--) {
                String pkgName = runList.get(i).optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                if (!TextUtils.isEmpty(pkgName) && ocInfo.contains(pkgName)) {
                    updateOCInfo.add(runList.get(i));
                    runList.remove(i);
                }
            }
            if (!updateOCInfo.isEmpty()) {
                // 更新该时段有记录的应用信息，则更新缓存表中的运行状态为1
                TableOCCount.getInstance(mContext).updateRunState(updateOCInfo);
            }
            if (runList != null && !runList.isEmpty()) {
                // 新增该时段没有记录的应用信息
                TableOCCount.getInstance(mContext).insertArray(runList);
            }
        }
    }

    /**
     * 根据读取出的包列表，获取应用信息并组成json格式添加到列表
     */
    private List<JSONObject> getOCArray(List<Process> runApps) {
        List<JSONObject> list = null;
        try {
            list = new ArrayList<JSONObject>();
            for (int i = 0; i < runApps.size(); i++) {
                String pkgName = runApps.get(i).getName();
                if (!TextUtils.isEmpty(pkgName)) {
                    JSONObject ocJson = getOCInfo(pkgName, EGContext.OC_COLLECTION_TYPE_PROC);
                    list.add(ocJson);
                }
            }
        } catch (Throwable e) {
            ELOG.e(e);
        }
        return list;
    }

    /**
     * 更新缓存，如果该时段有缓存就更新，没有就新增
     */
    private void updateCache(int collectionType) {
        if (!TextUtils.isEmpty(pkgName)) {
            // 根据包名和时段查询，判断当前时段是否已经启动过，如果有就更新，如果没有就新建
            List<String> ocInfo = TableOCCount.getInstance(mContext).getIntervalApps();
            JSONObject ocJson = getOCInfo(pkgName, EGContext.OC_COLLECTION_TYPE_RUNNING_TASK);
            if (ocInfo.contains(pkgName)) {
                // 该时段存在数据,使用已有记录的数据 更新开始时间结束时间
                TableOCCount.getInstance(mContext).update(ocJson);
            } else {
                // 该时段没有数据，存储该时段的记录
                TableOCCount.getInstance(mContext).insert(ocJson);
            }
        }
    }

    /**
     * 去除缓存中的重复，剩余为已经关闭的应用
     */
    private void removeRepeat(List<JSONObject> cacheApps) {
        try {
            JSONObject json = null;
            for (int i = cacheApps.size() - 1; i >= 0; i--) {
                json = cacheApps.get(i);
                String apn = json.getString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                ELOG.i(apn +" -------apn");
                if (!TextUtils.isEmpty(apn) && apn.equals(pkgName)) {
                    cacheApps.remove(i);
                    ELOG.i(" -------remove repeat ");
                    pkgName = null;
                    continue;
                }
                json.put(DeviceKeyContacts.OCInfo.ApplicationCloseTime, String.valueOf(System.currentTimeMillis()));
                json.put(DeviceKeyContacts.OCInfo.SwitchType, EGContext.SWITCH_TYPE_DEFAULT);
            }
        } catch (Throwable e) {
            ELOG.e(e);
        }
    }

    /**
     * 根据包名 获取应用信息并组成json格式
     */
    private JSONObject getOCInfo(String packageName, int collectionType) {
        JSONObject ocInfo = null;
        try {
            if (!TextUtils.isEmpty(packageName)) {
                PackageManager pm = null;
                ApplicationInfo appInfo = null;
                try {
                    pm = mContext.getPackageManager();
                    appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                }catch (Throwable t){
                }
                ocInfo = new JSONObject();
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationPackageName, packageName);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationOpenTime, String.valueOf(System.currentTimeMillis()));
                ocInfo.put(DeviceKeyContacts.OCInfo.NetworkType, NetworkUtils.getNetworkType(mContext));
                ocInfo.put(DeviceKeyContacts.OCInfo.CollectionType, collectionType);
                ocInfo.put(DeviceKeyContacts.OCInfo.SwitchType,EGContext.SWITCH_TYPE_DEFAULT);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationType, appType(packageName));
                try {
                    ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationVersionCode, pm.getPackageInfo(packageName, 0).versionName + "|"
                            + pm.getPackageInfo(packageName, 0).versionCode);
                }catch (Throwable t){
//                    ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationVersionCode, "");
                }
                try {
                    ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationName, appInfo.loadLabel(pm).toString());
                }catch (Throwable t){
//                    ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationName, "unknown");
                }

            }
        } catch (Throwable e) {
            ELOG.e(e+"    ::::::getOCInfo has an exception");
        }
        return ocInfo;
    }

    /**
     * 判断应用为系统应用还是第三方应用
     */
    private String appType(String pkgName) {
        String type = "";
        try {
            PackageInfo pkgInfo = mContext.getPackageManager().getPackageInfo(pkgName, 0);
            if ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                type = "OA";//sanfang
            } else {
                type = "SA";
            }
        } catch (Throwable e) {
            if(Utils.getCmdPkgName(SHELL_SYSTEM_LIST_PACKAGES).contains(pkgName)) type = "SA";
            if(Utils.getCmdPkgName(SHELL_THIRD_PARTY_LIST_PACKAGES).contains(pkgName)) type = "OA";
        }
        return type;
    }

//    private List<AppSnapshotInfo> getAppsFromShell() {
//        List<AppSnapshotInfo> appList = new ArrayList<AppSnapshotInfo>();
//        try {
//            AppSnapshotInfo info;
//            PackageManager pm = mContext.getPackageManager();
//            String result = Utils.shell(SHELL_PM_LIST_PACKAGES);
//            if (!TextUtils.isEmpty(result) && result.contains("\n")) {
//                String[] lines = result.split("\n");
//                if (lines.length > 0) {
//                    for (int i = 0; i < lines.length; i++) {
//                        try {
//                            String[] split = lines[i].split(":");
//                            if (split.length >= 1) {
//                                String packageName = split[1];
//                                if (!TextUtils.isEmpty(packageName)
//                                        && pm.getLaunchIntentForPackage(packageName) != null) {
//                                    info = new AppSnapshotInfo();
//                                    info.setApplicationPackageName(packageName);
//                                    appList.add(info);
//                                }
//                            }
//                        } catch (Throwable e) {
//                        }
//                    }
//                }
//            }
//        } catch (Throwable e) {
//            // igone
//        }
//        return appList;
//    }

//    public class OC {
//        // 应用包名
//        public static final String APN = "APN";
//        // 应用名称
//        public static final String AN = "AN";
//        // 开始时间
//        public static final String AOT = "AOT";
//        // 结束时间
//        public static final String ACT = "ACT";
//        // 应用打开关闭次数
//        public static final String CU = "CU";
//        // 应用版本信息
//        public static final String AVC = "AVC";
//        // 网络类型
//        public static final String NT = "NT";
//        // 应用切换类型，1-正常使用，2-开关屏幕切换，3-服务重启
//        public static final String AST = "AST";
//        // 应用类型
//        public static final String AT = "AT";
//        // OC采集来源，1-getRunningTask，2-读取proc，3-辅助功能，4-系统统计
//        public static final String CT = "CT";
//        // 快照次数所属的时段，1表示0～6小时，2表示6～12小时，3表示12～18小时，4表示18～24小时
//        public static final String TI = "TI";
//        // 发生日期
//        public static final String DY = "DY";
//    }
}
