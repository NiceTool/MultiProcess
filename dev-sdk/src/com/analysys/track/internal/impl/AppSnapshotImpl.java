package com.analysys.track.internal.impl;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.analysys.track.db.TableAppSnapshot;
import com.analysys.track.internal.content.DataController;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 安装列表获取
 * @Version: 1.0
 * @Create: 2019-08-06 19:13:40
 * @author: ly
 */
public class AppSnapshotImpl {


    /**
     * 应用列表
     */
    public void snapshotsInfo() {
        try {

            // 策略不允许安装列表模快工作, 则停止工作。
            if (!PolicyImpl.getInstance(mContext)
                    .getValueFromSp(UploadKey.Response.RES_POLICY_MODULE_CL_SNAPSHOT, true)) {
                return;
            }
            long now = System.currentTimeMillis();
            // 获取下发的间隔时间
            long durByPolicy = PolicyImpl.getInstance(mContext).getSP().getLong(EGContext.SP_SNAPSHOT_CYCLE, EGContext.TIME_HOUR * 3);

            // 3小时内只能操作一次
            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(mContext, EGContext.FILES_SYNC_APPSNAPSHOT, durByPolicy, now)) {
                long time = SPHelper.getLongValueFromSP(mContext, EGContext.SP_APP_SNAP, 0);
                long dur = now - time;


                //大于三个小时才可以工作
                if (dur > durByPolicy) {
                    if (EGContext.DEBUG_SNAP) {
                        ELOG.i("sanbo.snap", " 大于3小时可以开始工作 ");
                    }
                    SPHelper.setLongValue2SP(mContext, EGContext.SP_APP_SNAP, now);
                    if (SystemUtils.isMainThread()) {
                        EThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                getSnapShotInfo();
                            }
                        });
                    } else {
                        getSnapShotInfo();
                    }
                } else {
                    if (EGContext.DEBUG_SNAP) {
                        ELOG.i("sanbo.snap", " 大于小于3小时 time：" + time);
                    }
                    //同步调整时间
                    MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_APPSNAPSHOT, time);
                    MessageDispatcher.getInstance(mContext).postLocation(dur);
                }

            } else {
                return;
            }

        } catch (Throwable t) {
        }
    }

    private void getSnapShotInfo() {
        try {
            // 1. 获取现在的安装列表
            List<JSONObject> currentSnapshotsList = getCurrentSnapshots();
            if (EGContext.DEBUG_SNAP) {
                ELOG.i("sanbo.snap", " 获取安装列表: " + currentSnapshotsList.size());
            }
            if (currentSnapshotsList != null && currentSnapshotsList.size() > 0) {
                // 2. 获取DB中缓存的列表
                Map<String, String> dbSnapshotsMap = TableAppSnapshot.getInstance(mContext).snapShotSelect();
                if (dbSnapshotsMap != null && !dbSnapshotsMap.isEmpty()) {
                    // 3. 对比处理当前快照和db数据
                    currentSnapshotsList = getDifference(currentSnapshotsList, dbSnapshotsMap);
                }
                // 4. 最新的数据存储本地
                TableAppSnapshot.getInstance(mContext).coverInsert(currentSnapshotsList);
            }

            // 5. 解锁多进程
            long now = System.currentTimeMillis();
            MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_APPSNAPSHOT, now);
            MessageDispatcher.getInstance(mContext).postSnap(now);
            // 6. 异步消息
            MessageDispatcher.getInstance(mContext).postSnap(EGContext.TIME_HOUR * 3);
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }

        }
    }

    /**
     * 数据库与新获取的当前列表list做对比合并成新的list 存储
     *
     * @param currentSnapshotsList
     * @param dbSnapshotsMap
     */
    private List<JSONObject> getDifference(List<JSONObject> currentSnapshotsList, Map<String, String> dbSnapshotsMap) {
        try {
            if (currentSnapshotsList == null) {
                currentSnapshotsList = new ArrayList<JSONObject>();
            }
            for (int i = 0; i < currentSnapshotsList.size(); i++) {
                JSONObject item = (JSONObject) currentSnapshotsList.get(i);
                String apn = item.getString(UploadKey.AppSnapshotInfo.ApplicationPackageName);
                if (dbSnapshotsMap.containsKey(apn)) {
                    JSONObject dbitem = new JSONObject(dbSnapshotsMap.get(apn));
                    String avc = item.optString(UploadKey.AppSnapshotInfo.ApplicationVersionCode);
                    String dbAvc = dbitem.optString(UploadKey.AppSnapshotInfo.ApplicationVersionCode);
                    if (!TextUtils.isEmpty(avc) && !avc.equals(dbAvc)) {
                        item.put(UploadKey.AppSnapshotInfo.ActionType, EGContext.SNAP_SHOT_UPDATE);
                    }
                    dbSnapshotsMap.remove(apn);
                    continue;
                }
                item.put(UploadKey.AppSnapshotInfo.ActionType, EGContext.SNAP_SHOT_INSTALL);
            }
            Set<String> set = dbSnapshotsMap.keySet();
            for (String json : set) {
                JSONObject j = new JSONObject(dbSnapshotsMap.get(json));
                j.put(UploadKey.AppSnapshotInfo.ActionType, EGContext.SNAP_SHOT_UNINSTALL);
                currentSnapshotsList.add(j);
            }
        } catch (Throwable e) {
            return currentSnapshotsList;
        }
        return currentSnapshotsList;
    }

    /**
     * 获取应用列表快照
     */
    private List<JSONObject> getCurrentSnapshots() {
        List<JSONObject> list = null;
        try {
            PackageManager packageManager = mContext.getPackageManager();
            List<PackageInfo> packageInfo = packageManager.getInstalledPackages(0);
            if (packageInfo != null && packageInfo.size() > 0) {
                list = new ArrayList<JSONObject>();
                JSONObject jsonObject = null;
                PackageInfo pi = null;
                for (int i = 0; i < packageInfo.size(); i++) {
                    try {
                        pi = packageInfo.get(i);
                        if (pi != null) {
                            jsonObject = null;
                            jsonObject = getAppInfo(pi, mContext.getPackageManager(), EGContext.SNAP_SHOT_INSTALL);
                            if (jsonObject != null) {
                                list.add(jsonObject);
                            }
                        }
                    } catch (Throwable t) {
                    }
                }
                if (list.size() < 5) {
                    list = getAppInfosFromShell(mContext, EGContext.SNAP_SHOT_INSTALL, list);
                }
            } else {
                // 如果上面的方法不能获取，改用shell命令
                if (list == null) {
                    list = new ArrayList<JSONObject>();
                    if (list.size() < 5) {
                        list = getAppInfosFromShell(mContext, EGContext.SNAP_SHOT_INSTALL, list);
                    }
                }
            }

        } catch (Exception e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }

        }
        return list;
    }


    /**
     * 获取安装列表和对应的调试状态
     *
     * @return
     */
    public List<JSONObject> getAppDebugStatus() {
        List<JSONObject> list = new ArrayList<JSONObject>();
        try {
            PackageManager packageManager = mContext.getPackageManager();
            List<PackageInfo> packageInfo = packageManager.getInstalledPackages(0);
            if (packageInfo != null && packageInfo.size() > 0) {
                for (int i = 0; i < packageInfo.size(); i++) {
                    try {
                        JSONObject appInfo = new JSONObject();
                        String packageName = packageInfo.get(i).packageName;
                        appInfo.put(EGContext.TEXT_DEBUG_APP, packageName);
                        appInfo.put(EGContext.TEXT_DEBUG_STATUS, SystemUtils.isApkDebugable(mContext, packageName));
                        list.add(appInfo);
                    } catch (Throwable t) {
                    }
                }
                if (list.size() < 5) {
                    Set<String> result = new HashSet<String>();
                    result = getPkgNamesByShell(result, SHELL_PM_LIST_PACKAGES);

                    for (String packageName : result) {
                        JSONObject appInfo = new JSONObject();
                        appInfo.put(EGContext.TEXT_DEBUG_APP, packageName);
                        appInfo.put(EGContext.TEXT_DEBUG_STATUS, SystemUtils.isApkDebugable(mContext, packageName));
                        list.add(appInfo);
                    }
                }
            } else {
                // 如果上面的方法不能获取，改用shell命令
                Set<String> result = new HashSet<String>();
                result = getPkgNamesByShell(result, SHELL_PM_LIST_PACKAGES);

                for (String packageName : result) {
                    JSONObject appInfo = new JSONObject();
                    appInfo.put("packageName", packageName);
                    appInfo.put("debug", SystemUtils.isApkDebugable(mContext, packageName));
                    list.add(appInfo);
                }
            }

        } catch (Exception e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }

        }
        return list;
    }

    /**
     * shell方式获取文件名字，然后解析出app详情
     *
     * @param mContext
     * @param tag
     * @param appList
     * @return
     */
    private List<JSONObject> getAppInfosFromShell(Context mContext, String tag, List<JSONObject> appList) {
        try {
            JSONObject appInfo;
            Set<String> result = new HashSet<>();
            PackageManager pm = mContext.getPackageManager();
            result = getPkgNamesByShell(result, SHELL_PM_LIST_PACKAGES);
            PackageInfo pi = null;
            for (String pkgName : result) {
                if (!TextUtils.isEmpty(pkgName) && pm.getLaunchIntentForPackage(pkgName) != null) {
                    pi = mContext.getPackageManager().getPackageInfo(pkgName, 0);
                    appInfo = AppSnapshotImpl.getInstance(mContext).getAppInfo(pi, pm, tag);
                    if (!appList.contains(appInfo)) {
                        appList.add(appInfo);
                    }

                }
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
        return appList;
    }

    /**
     * 通过shell获取安装列表
     *
     * @param appSet
     * @param shell
     * @return
     */
    private Set<String> getPkgNamesByShell(Set<String> appSet, String shell) {
        // Set<String> set = new HashSet<String>();
        String result = ShellUtils.shell(shell);
        if (!TextUtils.isEmpty(result) && result.contains("\n")) {
            String[] lines = result.split("\n");
            if (lines.length > 0) {
                String line = null;
                for (int i = 0; i < lines.length; i++) {
                    line = lines[i];
                    // 单行条件: 非空&&有点&&有冒号
                    if (!TextUtils.isEmpty(line) && line.contains(".") && line.contains(":")) {
                        // 分割. 样例数据:<code>package:com.android.launcher3</code>
                        String[] split = line.split(":");
                        if (split != null && split.length > 1) {
                            String packageName = split[1];
                            appSet.add(packageName);
                        }
                    }
                }
            }
        }
        return appSet;
    }


    public String getAppType(String pkg) {
        return isSystemApps(pkg) ? UploadKey.OCInfo.APPLICATIONTYPE_SYSTEM_APP : UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP;
    }

    /**
     * 是否为系统应用:
     * <p>
     * 1. shell获取到三方列表判断
     * <p>
     * 2. 获取异常的使用其他方式判断
     *
     * @param pkg
     * @return
     */
    public boolean isSystemApps(String pkg) {

        // 1. 没有获取应用列表则获取
        if (mSystemAppSet.size() < 1) {
            getPkgNamesByShell(mSystemAppSet, APP_LIST_SYSTEM);
        }
        // 2. 根据列表内容判断
        if (mSystemAppSet.size() > 0) {
            if (mSystemAppSet.contains(pkg)) {
                return true;
            } else {
                return false;
            }
        } else {
            try {
                // 3. 使用系统方法判断
                mContext = EContextHelper.getContext(mContext);
                if (mContext == null) {
                    return false;
                }
                PackageManager pm = mContext.getPackageManager();
                if (pm == null) {
                    return false;
                }
                PackageInfo pInfo = pm.getPackageInfo(pkg, 0);
                if ((pInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 1) {
                    return true;
                }
            } catch (Throwable e) {
            }

        }
        return false;
    }


    /**
     * 获取APP详情
     *
     * @param pkgInfo
     * @param packageManager
     * @param tag
     * @return
     */
    @SuppressWarnings("deprecation")
    private JSONObject getAppInfo(PackageInfo pkgInfo, PackageManager packageManager, String tag) {
        JSONObject appInfo = new JSONObject();
        JsonUtils.pushToJSON(mContext, appInfo, UploadKey.AppSnapshotInfo.ApplicationPackageName,
                pkgInfo.packageName, DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
        JsonUtils.pushToJSON(mContext, appInfo, UploadKey.AppSnapshotInfo.ApplicationName,
                String.valueOf(pkgInfo.applicationInfo.loadLabel(packageManager)),
                DataController.SWITCH_OF_APPLICATION_NAME);
        JsonUtils.pushToJSON(mContext, appInfo, UploadKey.AppSnapshotInfo.ApplicationVersionCode,
                pkgInfo.versionName + "|" + pkgInfo.versionCode, DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
        JsonUtils.pushToJSON(mContext, appInfo, UploadKey.AppSnapshotInfo.ActionType, tag,
                DataController.SWITCH_OF_ACTION_TYPE);
        JsonUtils.pushToJSON(mContext, appInfo, UploadKey.AppSnapshotInfo.ActionHappenTime,
                String.valueOf(System.currentTimeMillis()), DataController.SWITCH_OF_ACTION_HAPPEN_TIME);
        return appInfo;
    }

    /**************************************  处理广播消息 ******************************************************/

    /**
     * 处理消息中的应用安装、卸载、更新广播
     *
     * @param pkgName
     * @param type
     * @param time
     */
    public void processAppModifyMsg(final String pkgName, final int type, final long time) {
        if (TextUtils.isEmpty(pkgName)) {
            return;
        }
        if (EGContext.DEBUG_SNAP) {
            ELOG.d("sanbo.snap", " 处理广播接收到的信息 包:" + pkgName + "----type: " + type);
        }
        if (SystemUtils.isMainThread()) {
            // 数据库操作修改包名和类型
            EThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    realProcessInThread(type, pkgName, time);
                }
            });
        } else {
            realProcessInThread(type, pkgName, time);
        }


    }

    private void realProcessInThread(int type, String pkgName, long time) {
        try {
            if (type == 0) {
                // SNAP_SHOT_INSTALL 解锁
                PackageManager pm = mContext.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(pkgName, 0);
                if (pi != null) {
                    JSONObject jsonObject = getAppInfo(pi, pm, EGContext.SNAP_SHOT_INSTALL);
                    if (jsonObject != null) {
                        // 判断数据表中是否有该应用的存在，如果有标识此次安装是应用更新所导致
                        boolean isHas = TableAppSnapshot.getInstance(mContext).isHasPkgName(pkgName);
                        if (!isHas) {
                            TableAppSnapshot.getInstance(mContext).insert(jsonObject);
                        }
                    }
                }
                MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_SNAP_ADD_BROADCAST, System.currentTimeMillis());

            } else if (type == 1) {
                TableAppSnapshot.getInstance(mContext).update(pkgName, EGContext.SNAP_SHOT_UNINSTALL, time);
                // SNAP_SHOT_UNINSTALL 解锁
                MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_SNAP_DELETE_BROADCAST,
                        System.currentTimeMillis());
            } else if (type == 2) {
                TableAppSnapshot.getInstance(mContext).update(pkgName, EGContext.SNAP_SHOT_UPDATE, time);
                // SNAP_SHOT_UPDATE 解锁
                MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_SNAP_UPDATE_BROADCAST,
                        System.currentTimeMillis());
            }
        } catch (Throwable e) {
        }
    }

    /**************************************  单例和变量 ******************************************************/

    private AppSnapshotImpl() {
    }


    private static class Holder {
        private static final AppSnapshotImpl INSTANCE = new AppSnapshotImpl();
    }


    public static AppSnapshotImpl getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    private final String SHELL_PM_LIST_PACKAGES = "pm list packages";// all
    private final String APP_LIST_SYSTEM = "pm list packages -s";// system
    // private final String APP_LIST_USER = "pm list packages -3";// third party
    // 获取系统应用列表
    private final Set<String> mSystemAppSet = new HashSet<String>();
    //    private boolean isSnapShotBlockRunning = false;
    private Context mContext;


}
