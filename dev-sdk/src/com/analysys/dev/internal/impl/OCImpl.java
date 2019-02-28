package com.analysys.dev.internal.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import com.analysys.dev.R;
import com.analysys.dev.database.DBConfig;
import com.analysys.dev.database.TableOCCount;
import com.analysys.dev.database.TableOCTemp;
import com.analysys.dev.database.TableXXXInfo;
import com.analysys.dev.internal.Content.DeviceKeyContacts;
import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.internal.impl.proc.ProcParser;
import com.analysys.dev.internal.impl.proc.Process;
import com.analysys.dev.internal.impl.proc.ProcessManager;
import com.analysys.dev.service.AnalysysAccessibilityService;
import com.analysys.dev.utils.AccessibilityHelper;
import com.analysys.dev.utils.ELOG;
import com.analysys.dev.utils.EThreadPool;
import com.analysys.dev.utils.NetworkUtils;
import com.analysys.dev.utils.PermissionUtils;
import com.analysys.dev.utils.Utils;
import com.analysys.dev.utils.reflectinon.EContextHelper;
import com.analysys.dev.internal.work.MessageDispatcher;
import com.analysys.dev.utils.sp.SPHelper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;


public class OCImpl {

    Context mContext;
//    private final String SHELL_PM_LIST_PACKAGES = "pm list packages";//all
    private final String SHELL_SYSTEM_LIST_PACKAGES = "pm list packages -s";//system
    private final String SHELL_THIRD_PARTY_LIST_PACKAGES = "pm list packages -3";//third party
//    private boolean isSaveForScreenOff = false;
//    private List<String> nowPackageNames = new ArrayList<String>();
//    private List<String> prePackageNames = new ArrayList<String>();
//    private List<OCInfo> ocList = new ArrayList<OCInfo>();
//    private List<OCInfo> ocCache = new ArrayList<OCInfo>();
//    private String endTime = null;

    private static class Holder {
        private static final OCImpl INSTANCE = new OCImpl();
    }

    public static OCImpl getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    private long mProcessTime = 0L;
    /**
     * OC 信息采集
     */
    public void ocInfo() {
        EThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //亮屏幕工作
                    if (Utils.isScreenOn(mContext)) {
                        // 锁屏.保存数据
                        if (Utils.isScreenLocked(mContext)) {
                            String openApp = SPHelper.getLastAppName(mContext);
                            if (!TextUtils.isEmpty(openApp)) {
                                //补充时间
                                SPHelper.setEndTime(mContext, System.currentTimeMillis()-new Random(25).nextInt(1000));
                                filterInsertOCInfo(EGContext.CLOSE_SCREEN, false);
                            }
                        } else {
//                    L.e("开始判断OC..........");
                            if (!AccessibilityHelper.isAccessibilitySettingsOn(mContext,AnalysysAccessibilityService.class)) {
                                getInfoByVersion();
                            }else{
                                //利用辅助功能获取当前app
                            }
                        }
                    }
                }catch (Throwable t){

                }
            }
        });
    }

    String pkgName = null;
    public void getInfoByVersion(){
        // 判断系统版本
        if (Build.VERSION.SDK_INT < 21) {
            if(PermissionUtils.checkPermission(mContext, Manifest.permission.GET_TASKS)){
//                RunningApps(getRunningApp(), EGContext.OC_COLLECTION_TYPE_RUNNING_TASK);
                getRunningApp();
            }else{
                getProcApps();
            }
            SPHelper.getDefault(mContext).edit().putLong(EGContext.OC_LAST_TIME,System.currentTimeMillis()).commit();
            MessageDispatcher.getInstance(mContext).ocInfo(EGContext.OC_CYCLE);
        }else if(Build.VERSION.SDK_INT > 20 && Build.VERSION.SDK_INT < 24 ){
        //确定5.0和以上版本30秒处理一次
            if (isDurLThanThri()) {
                //L.i("满足30秒间隔。。。");
                if (Utils.canUseUsageStatsManager(mContext)) {
                // L.i("开启了。UsageStatsManager功能");
                    processOCByUsageStatsManager();
                } else {
                //  L.i(" proc... 方式采集。。。");
                    getProcApps();
                }
            } else {
                //  L.d("不到30秒。。。");
            }
            SPHelper.getDefault(mContext).edit().putLong(EGContext.OC_LAST_TIME_OVER_5,System.currentTimeMillis()).commit();
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
            JSONArray cacheApps = TableOCCount.getInstance(mContext).selectRunning();
            ELOG.i("cacheApps::::::   "+cacheApps);
            if (cacheApps != null && cacheApps.length()>0) {
                removeRepeat(cacheApps);
                if (cacheApps != null && cacheApps.length()>0) {
                    // 完成一次闭合，存储到OC表
                    TableOCTemp.getInstance(mContext).insert(cacheApps);
                    ELOG.i("RunningApps:::::::"+cacheApps);
                    TableOCCount.getInstance(mContext).insertArray(cacheApps);
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
    private void getRunningApp() {
        String pkgName = "";
        ActivityManager am = null;
        try {
            am =  (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
            if(tasks == null || tasks.size()<=0 ) return;
            pkgName = tasks.get(0).topActivity.getPackageName();
            processPkgName(pkgName);
        } catch (Throwable e) {
            try {
                List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
                if (processInfos == null || processInfos.size() <= 0) {
                    return ;
                }
                for (ActivityManager.RunningAppProcessInfo appProcess : processInfos) {
                    if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        processPkgName(appProcess.processName);
                    }
                }
            } catch (Throwable tw) {
            }
        }
    }
    /**
     * 根据包名进行一系列的处理
     *
     * @param packageName
     */
    private void processPkgName(String packageName) {
//        L.i( "processPkgName:" + packageName);
        String lastPkgName = SPHelper.getLastOpenPackgeName(mContext);

        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        PackageManager pm = mContext.getPackageManager();
        // 是否首次打开
        if (TextUtils.isEmpty(lastPkgName)) {
            SPHelper.setEndTime(mContext,System.currentTimeMillis()-new Random(25).nextInt(1000));
            insertShared(pm, packageName);
        } else {
            // 如果打开的包名与缓存的包名不一致，存储数据并将包名做缓存
            if (!packageName.equals(lastPkgName)) {
//                L.i("=======切换包名。即将保存");
                SPHelper.setEndTime(mContext,System.currentTimeMillis()-new Random(25).nextInt(1000));
                filterInsertOCInfo(EGContext.APP_SWITCH, false);
                insertShared(pm, packageName);
            }
        }
    }
    /**
     * 缓存数据
     *
     * @param pm
     * @param pkgName
     */
    private void insertShared(PackageManager pm, String pkgName) {

        String appName = "", versionName = "", versionCode = "";
        try {

            if (pm == null || TextUtils.isEmpty(pkgName)) {
                return;
            }
            appName = pm.getApplicationLabel(pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA)) + "";
            versionName = pm.getPackageInfo(pkgName, 0).versionName;
            versionCode = String.valueOf(pm.getPackageInfo(pkgName, 0).versionCode);

        } catch (Throwable e) {
        }
        String nowTime = String.valueOf(System.currentTimeMillis());
        SPHelper.setLastOpenPackgeName(mContext,pkgName);
        SPHelper.setLastOpenTime(mContext,nowTime);
        SPHelper.setLastAppName(mContext,appName);
        SPHelper.setLastAppVerison(mContext,versionName == null || "null".equals(versionName) ? "1.0" : versionName + "|" + versionCode);
        SPHelper.setAppType(mContext, appType(pkgName));
    }

    /**
     * 从Proc中读取数据
     */
    private void getProcApps() {
        JSONArray cacheApps = TableOCCount.getInstance(mContext).selectRunning();
        ELOG.i(cacheApps+"   :::::::: cacheApps");
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
            if (cacheApps == null || cacheApps.length()<1) {
                JSONArray ocArray = new JSONArray();
                JSONArray tempArray = new JSONArray();
                JSONObject temp = new JSONObject();

                for (int i = 0; i < run.size(); i++) {
                    String pkgName = run.get(i).getName();
                    if (!TextUtils.isEmpty(pkgName)) {
                        temp.put(DeviceKeyContacts.OCInfo.ApplicationPackageName,pkgName);
                        temp.put(DeviceKeyContacts.OCInfo.ApplicationOpenTime,System.currentTimeMillis());
                        tempArray.put(temp);
                        ocArray.put(getOCInfo(pkgName.replaceAll(" ",""), EGContext.OC_COLLECTION_TYPE_PROC));
                    }
                }
                TableOCTemp.getInstance(mContext).insert(tempArray);
                ELOG.i("getProcApps 280:::::"+ocArray);
                TableOCCount.getInstance(mContext).insertArray(ocArray);
            } else {
                // 去重
                JSONObject res = removeRepeat(cacheApps, run);
                if(res != null && res.length()>0){
                    try {
                        cacheApps = new JSONArray(res.get("cache").toString());
                        ELOG.i(cacheApps +"   ::::::::: cacheApps:::::");
                    }catch (Throwable t){
                        ELOG.i("   ::::::::: cacheApps 异常:::::");
                        cacheApps = null;
                    }
                }
                if(cacheApps != null && cacheApps.length()>0) {
                    // 更新缓存表
                    updateCacheState(cacheApps);
                    TableOCTemp.getInstance(mContext).insert(cacheApps);
                    // 存储关闭信息到OC表
                    ELOG.i("getProcApps 289:::::" + cacheApps);
                    TableOCCount.getInstance(mContext).insertArray(cacheApps);
                }
                try {
                    run =(List<Process>)(res.get("run"));
                }catch (Throwable t){
                    ELOG.i("   ::::::::: run 异常:::::");
                   run = null;
                }
                if(run != null && run.size()>0){
                    // 新增该时段缓存信息
                    addCache(run);
                    ELOG.i("RUN   :"+run);
                }
            }
        }catch (Throwable t){
            ELOG.i("getProcApps has an exception :::"+ t.getMessage());
        }
    }

    /**
     * 缓存中应用列表与新获取应用列表去重
     */
    private JSONObject removeRepeat(JSONArray cacheApps, List<Process> runApps) {

        JSONObject ocInfo = null,result= new JSONObject();
        try {
            List list = Utils.getDiffNO(cacheApps.length());
            int random ;
            List oc = new ArrayList();
            String apn ;
            for (int i = 0;i < cacheApps.length() - 1; i++ ) {
                random = (Integer) list.get(i);
                ocInfo = (JSONObject) cacheApps.get(i);
                if(ocInfo == null || ocInfo.length()<1) continue;
                ELOG.i(i+"ocInfoocInfoocInfo  :::: "+ocInfo);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationCloseTime, String.valueOf(System.currentTimeMillis()-random));
                oc.add(ocInfo);
                apn = ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName).replaceAll(" ","");
                for (int j = 0;j < runApps.size();j++) {
                    ELOG.i(runApps.size()+"   :::runApps.size()  "+j);
                    String pkgName = runApps.get(j).getName().replaceAll(" ","");
                    ELOG.i(pkgName +"::::::::pkgName   ::"+apn+":::::"+ apn.equals(pkgName));
                    if (!TextUtils.isEmpty(apn) && apn.equals(pkgName)) {
                        oc.remove(oc.size()-1);
                        runApps.remove(j);
                        break;
                    }
                }
//                ocInfo.put(DeviceKeyContacts.OCInfo.SwitchType, EGContext.SWITCH_TYPE_DEFAULT);
            }
//            ELOG.i(oc+"   :::::: oc");
            if(cacheApps != null && cacheApps.length()>0){
                cacheApps = new JSONArray(oc);
                result.put("cache",cacheApps);
                ELOG.i(cacheApps+"   :::::: cacheApps");
            }
            if(runApps != null && runApps.size()>0) {
                result.put("run",runApps);
                ELOG.i(runApps+"   :::::: runApps");
            }
        }catch (Throwable t){
            ELOG.e(t.getMessage()+"tttttttttttttttttttt");
        }
        return result;
    }

    /**
     * 更新缓存表
     */
    private void updateCacheState(JSONArray cacheApps) {
        try {
            if (cacheApps != null && cacheApps.length()>0) {
                // 缓存数据列表与新获取数据列表去重，缓存列表剩余为已经关闭的应用，需要转存储到OC表，并更新运行状态为0
                JSONArray ocList = new JSONArray();
                JSONObject oc = null;
                for (int i = 0; i < cacheApps.length(); i++) {
                    oc = (JSONObject) cacheApps.get(i);
                    int numb = oc.optInt(DeviceKeyContacts.OCInfo.CU) + 1;
                    String apn = oc.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                    oc.remove(DeviceKeyContacts.OCInfo.CU);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationPackageName, apn);
                    jsonObject.put(DeviceKeyContacts.OCInfo.CU, numb);
                    ocList.put(jsonObject);
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
        try {
            List oc = new ArrayList();
            if (!runApps.isEmpty()) {
                // 缓存数据列表与新获取数据列表去重，新获取列表剩余未新打开的应用，需要缓存到OCCount中，
                List<String> ocInfo = TableOCCount.getInstance(mContext).getIntervalApps();
                JSONArray runList = getOCArray(runApps);

                JSONArray updateOCInfo = new JSONArray();
                // 将新增列表拆开，该时段有应用打开记录的修改更新记录，该时段没有应用打开记录的新增记录
                for (int i = runList.length() - 1; i >= 0; i--) {
                    oc.add(runList.get(i));
                    String pkgName = new JSONObject(runList.get(i).toString()).optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                    if (!TextUtils.isEmpty(pkgName) && ocInfo.contains(pkgName)) {
                        updateOCInfo.put(runList.get(i));
//                        runList.remove(i);
                        oc.remove(oc.size()-1);
                    }
                }
                runList = new JSONArray(oc);
                if (updateOCInfo != null && updateOCInfo.length() >0) {
                    // 更新该时段有记录的应用信息，则更新缓存表中的运行状态为1
                    TableOCCount.getInstance(mContext).updateRunState(updateOCInfo);
                }
                if (runList != null && runList.length()>0) {
                    // 新增该时段没有记录的应用信息
                    ELOG.i("addCache:::::"+runList);
                    TableOCCount.getInstance(mContext).insertArray(runList);
                }
            }
        }catch (Throwable t){

        }

    }

    /**
     * 根据读取出的包列表，获取应用信息并组成json格式添加到列表
     */
    private JSONArray getOCArray(List<Process> runApps) {
        JSONArray list = null;
        try {
            list = new JSONArray();
            for (int i = 0; i < runApps.size(); i++) {
                String pkgName = runApps.get(i).getName();
                if (!TextUtils.isEmpty(pkgName)) {
                    JSONObject ocJson = getOCInfo(pkgName, EGContext.OC_COLLECTION_TYPE_PROC);
                    list.put(ocJson);
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
    private void removeRepeat(JSONArray cacheApps) {
        try {
            JSONObject json = null;
            List list = Utils.getDiffNO(cacheApps.length()-1);
            int random ;
            for (int i = cacheApps.length() - 1; i >= 0; i--) {
                json = (JSONObject) cacheApps.get(i);
                random = (Integer) list.get(i);

                String apn = json.getString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                ELOG.i(apn +" -------apn"+ "    random ::::::"+random);
                if (!TextUtils.isEmpty(apn) && apn.equals(pkgName)) {
                    cacheApps.remove(i);
                    ELOG.i(" -------remove repeat ");
                    pkgName = null;
                    continue;
                }
                json.put(DeviceKeyContacts.OCInfo.ApplicationCloseTime, String.valueOf(System.currentTimeMillis()-random));
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
    public String appType(String pkgName) {
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
    public void filterInsertOCInfo(String switchType, boolean createThread) {
//        L.i("-------filterInsertOCInfo()----" + switchType);
        String OldPkgName = SPHelper.getLastOpenPackgeName(mContext);
        String appName = SPHelper.getLastAppName(mContext);
        String appVersion = SPHelper.getLastAppVerison(mContext);
        String openTime = SPHelper.getLastOpenTime(mContext);
        Long closeTime = SPHelper.getEndTime(mContext);
        String appType = SPHelper.getAppType(mContext);
//        L.i("filterInsertOCInfo..... 1111");
        if (TextUtils.isEmpty(OldPkgName) || TextUtils.isEmpty(appName) || TextUtils.isEmpty(appVersion)
                || TextUtils.isEmpty(openTime)) {
            return;
        }
        long time = closeTime;
        long minDuration = SPHelper.getMinDuration(mContext);
        long maxDuration = SPHelper.getMaxDuration(mContext);

        if (minDuration <= 0) {
            minDuration = EGContext.SHORT_TIME;
        }
        if (maxDuration <= 0) {
            maxDuration = EGContext.LONGEST_TIME;
        }
//        L.i("filterInsertOCInfo..... 2222  time:" +time);
        JSONObject ocInfo  = null ;
        if (minDuration / 2 <= time && time <= maxDuration) {
//            L.i("filterInsertOCInfo..... 33333");
            try {
                ocInfo = new JSONObject();
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationPackageName, OldPkgName);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationOpenTime, openTime);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationCloseTime, closeTime);

                ocInfo.put(DeviceKeyContacts.OCInfo.NetworkType, NetworkUtils.getNetworkType(mContext));
                ocInfo.put(DeviceKeyContacts.OCInfo.CollectionType, "1");
                ocInfo.put(DeviceKeyContacts.OCInfo.SwitchType,switchType);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationType,appType);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationVersionCode, appVersion);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationName, appName);
                if (ocInfo != null && !"".equals(openTime) && !"".equals(closeTime)) {
                    TableOCCount.getInstance(mContext).insert(ocInfo);// 保存上一个打开关闭记录信息
                }
            }catch (Throwable t){
            }
        }
        SPHelper.setLastOpenPackgeName(mContext,"");
        SPHelper.setLastOpenTime(mContext,"");
        SPHelper.setLastAppName(mContext,"");
        SPHelper.setLastAppVerison(mContext,"");
    }
    /**
     * android 5/6需要间隔大于30秒
     *
     * @return
     */
    private boolean isDurLThanThri() {
        long now = System.currentTimeMillis();
        if (mProcessTime == 0 || (now - mProcessTime) >= 30 * 1000) {
            mProcessTime = now;
            return true;
        }
        return false;
    }
    /**
     * android 5以上，有UsageStatsManager权限可以使用的
     */
    public void processOCByUsageStatsManager() {
        class RecentUseComparator implements Comparator<UsageStats> {
            @Override
            public int compare(UsageStats lhs, UsageStats rhs) {
                return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
            }
        }
        try {

            @SuppressLint("WrongConstant")
            UsageStatsManager usm = (UsageStatsManager) mContext.getApplicationContext().getSystemService("usagestats");
            if (usm == null) {
                return;
            }
            long ts = System.currentTimeMillis();
            List<UsageStats> usageStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 1000 * 10, ts);
            if (usageStats == null || usageStats.size() == 0) {
                return;
            }
            Collections.sort(usageStats, new RecentUseComparator());
            String usmPkg = usageStats.get(0).getPackageName();
            if (!TextUtils.isEmpty(usmPkg)) {
                processPkgName(usmPkg);
            } else {
                getProcApps();
            }
        } catch (Throwable e) {
            getProcApps();
        }
    }


}
