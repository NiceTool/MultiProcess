package com.analysys.track.internal.impl.ftime;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.db.TableProcess;
import com.analysys.track.internal.content.DataController;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.work.ECallBack;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.NetworkUtils;
import com.analysys.track.utils.pkg.PkgList;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: last modify by file impl
 * @Version: 1.0
 * @Create: 2021/03/77 18:13:11
 * @author: sanbo
 */
public class LmFileImpl {
    public void tryGetFileTime(final ECallBack callback) {
        EThreadPool.runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                try {
                    realGetFlt(callback);
                } catch (Throwable e) {
                    if (BuildConfig.ENABLE_BUG_REPORT) {
                        BugReportForTest.commitError(BuildConfig.tag_finfo, e);
                    }
                    if (callback != null) {
                        callback.onProcessed();
                    }
                }
            }
        });
    }

    // 数据结构： [包名:上次活跃时间]
    private Map<String, Long> mMapAndTimes = new ConcurrentHashMap<String, Long>();

    public Map<String, Long> getMemDataForTest() {
        return mMapAndTimes;
    }

    /**
     * 工作逻辑:
     * 1. 内存: 从0到1
     * 行动:  加载数据库中数据可用数据到内存,获取活跃列表时间
     * 2. 内存: 从1更新2
     * 行动: 获取活跃列表时间-内存对比-保持最新，保存最新状态[后续工作->闭合数据]
     * </p>
     * 安全机制:
     * 1. 进程锁保护10秒只能进行一次操作
     * 2. 内存持有一份最新的数据,方便快速对比 / 数据库直接取
     *
     * @param callback
     */
    public void realGetFlt(ECallBack callback) {

        if (mMapAndTimes.size() == 0) {
            mMapAndTimes = new ConcurrentHashMap<String, Long>(TableProcess.getInstance(mContext).loadMemFinfo());
        }
        List<LmFileUitls.AppTime> ats = LmFileUitls.getLastAliveTimeInSD(mContext);
        Map<String, Long> willFlushData = new ConcurrentHashMap<String, Long>();
        Map<String, Long> uploadData = new ConcurrentHashMap<String, Long>();
        for (LmFileUitls.AppTime at : ats) {
            String pkg = at.getPackageName();
            long lastActiveTime = at.getLastActiveTime();
            if (!mMapAndTimes.containsKey(pkg)) {
                //首次
                mMapAndTimes.put(pkg, lastActiveTime);
                willFlushData.put(pkg, lastActiveTime);
                // TODO 一期活跃且未闭合数据
                uploadData.put(pkg, lastActiveTime);
            } else {
                long activtyInMemory = mMapAndTimes.get(pkg);
                //有变动
                if (lastActiveTime != activtyInMemory) {
                    mMapAndTimes.put(pkg, lastActiveTime);
                    willFlushData.put(pkg, lastActiveTime);
                    // TODO 一期活跃且未闭合数据
                    uploadData.put(pkg, lastActiveTime);
                }
            }
        }
        if (willFlushData.size() > 0) {
            TableProcess.getInstance(mContext).flushMemFInfo(willFlushData);
            willFlushData.clear();
        }
        if (uploadData.size() > 0) {
            prepareUplaodData(uploadData);
            uploadData.clear();
        }
        if (callback != null) {
            callback.onProcessed();
        }
    }

    private void prepareUplaodData(Map<String, Long> uploadData) {
        List<JSONObject> data = new CopyOnWriteArrayList<JSONObject>();
        PackageManager pm = mContext.getPackageManager();
        for (Map.Entry<String, Long> upinfo : uploadData.entrySet()) {
            try {
                String pkgName = upinfo.getKey();
                long lastActiveTime = upinfo.getValue();
                JSONObject obj = getAppInfo(pm, pkgName, lastActiveTime);
                if (obj != null && obj.length() > 0) {
                    data.add(obj);
                }

            } catch (Exception e) {
            }

        }

    }

    /**
     * 获取APP详情
     *
     * @param packageManager
     * @param pkgName
     * @param lastActiveTime
     * @return
     */
    @SuppressWarnings("deprecation")
    public JSONObject getAppInfo(PackageManager packageManager, String pkgName, long lastActiveTime) {
        JSONObject appInfo = new JSONObject();

        try {

            PackageInfo pi = packageManager.getPackageInfo(pkgName, 0);
            if (!TextUtils.isEmpty(pkgName) && pkgName.contains(".") && PkgList.hasLaunchIntentForPackage(packageManager, pkgName)) {
                JsonUtils.add(appInfo, UploadKey.FInfo.ApplicationCloseTime, String.valueOf(0));
                JsonUtils.add(appInfo, UploadKey.FInfo.ApplicationOpenTime, String.valueOf(lastActiveTime));
                JsonUtils.add(appInfo, UploadKey.FInfo.ApplicationPackageName, pkgName);
                try {
                    JsonUtils.add(appInfo, UploadKey.FInfo.ApplicationName,
                            String.valueOf(pi.applicationInfo.loadLabel(packageManager)));
                } catch (Throwable e) {
                }
                JsonUtils.add(appInfo, UploadKey.FInfo.ApplicationVersionCode,
                        pi.versionName + "|" + pi.versionCode);
                JsonUtils.add(appInfo, UploadKey.FInfo.NetworkType,
                        NetworkUtils.getNetworkType(mContext));

            }
        } catch (Throwable e) {
        }

        return appInfo;
    }

    /********************* get instance begin **************************/
    public static LmFileImpl getInstance(Context context) {
        return HLODER.INSTANCE.initContext(context);
    }

    private LmFileImpl initContext(Context context) {
        if (mContext == null && context != null) {
            mContext = context.getApplicationContext();
        }
        return HLODER.INSTANCE;
    }


    private static class HLODER {
        private static final LmFileImpl INSTANCE = new LmFileImpl();
    }

    private LmFileImpl() {
    }

    private Context mContext = null;
    /********************* get instance end **************************/


}
