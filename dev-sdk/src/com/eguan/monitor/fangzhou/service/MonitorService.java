package com.eguan.monitor.fangzhou.service;

import java.util.List;

import com.eguan.Constants;
import com.eguan.db.DBPorcesser;
import com.eguan.monitor.AccessibilityOCManager;
import com.eguan.monitor.InnerProcessCacheManager;
import com.eguan.imp.AppProcessManager;
import com.eguan.imp.InstalledAPPInfoManager;
import com.eguan.imp.InstalledAppInfo;
import com.eguan.imp.OCInfoManager;
import com.eguan.utils.commonutils.EgLog;
import com.eguan.utils.commonutils.MyThread;
import com.eguan.utils.commonutils.ReceiverUtils;
import com.eguan.utils.commonutils.SPHodler;
import com.eguan.utils.thread.EGQueue;
import com.eguan.utils.thread.SafeRunnable;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.PowerManager;

/**
 * 设备监测主服务程序
 */
public class MonitorService extends Service {

    Context context = MonitorService.this;

    // --------------地理位置信息--------------
    private SPHodler spUtil = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Constants.FLAG_DEBUG_INNER) {
            EgLog.v("MonitorService.onCreate");
        }
        EGQueue.execute(new SafeRunnable() {
            @Override
            public void safeRun() {
                try {
                    // 网络未发生变化前,获取NT信息
                    InnerProcessCacheManager.getInstance().dealAppNetworkType(context);
                    OCInfoManager.getInstance(context).filterInsertOCInfo(Constants.SERVCICE_RESTART, true);
                    AccessibilityOCManager.getInstance(context).updateServiceBootOCInfo();
                    // 处理5.0的proc数据
                    AppProcessManager.getInstance(context).dealRestartService();
                    DBPorcesser.getInstance(context).initDB();
                } catch (Throwable e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
                initInfo();
                // 同步应用的URL到设备，确保地址为最新
                Constants.setNormalUploadUrl();
                Constants.setRTLUploadUrl();
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getKeyAndChannel();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        EGQueue.execute(new SafeRunnable() {
            @Override
            public void safeRun() {
                try {
                    String str = spUtil.getDeviceTactics();
                    if (!str.equals(Constants.TACTICS_STATE)) {
                        ReceiverUtils.getInstance().unRegistAllReceiver(context, true);
                        MyThread.getInstance(context).stopThread();
                        startService(new Intent(context, MonitorService.class));
                    }
                } catch (Throwable e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
            }
        });
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    private void initInfo() {
        try {
            spUtil = SPHodler.getInstance(this);
            // ProcessTimeManager.getInstance().setProcessTime(context);
            /*------------------ 初始化OCInfo存储信息 -----------------*/
            InitializationOCSP();
            /*---------------缓存最新应用列表信息，对于对比卸载变化情况---------------------*/
            InstalledAPPInfoManager manager = new InstalledAPPInfoManager();
            List<InstalledAppInfo> list = InstalledAPPInfoManager.getAllApps(this);
            spUtil.setAllAppForUninstall(manager.getAppInfoToJson(list));
            /*---------------设置本次启动，允许进行网络请求---------------------*/
            spUtil.setRequestState(0);
            /*--------------- 注销所有存活着的广播 -----------------------*/
            // ReceiverUtils.getInstance().unRegistAllReceiver(context, true);
            /*--------------- 注册所有广播 -----------------------*/
            ReceiverUtils.getInstance().registerScreenReceiver(context);
            /*-----------------判断是否获取地理位置信息----------------*/
            LocationInfo();
            /*----- 五秒定时器启动，获取打开关闭数据 获取，判断是否需要上传数据----*/
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            // 如果为true，则表示屏幕正在使用，false则屏幕关闭。
            if (isScreenOn) {

                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.v("----------------ScreenOn will  registAllReceiver--------------");
                }
                /*--------------- 注册所有存活着的广播 -----------------------*/
                ReceiverUtils.getInstance().registAllReceiver(context);
                // GlobalTimer.getInstance(MonitorService.this).startAlarm();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    /**
     * 初始化OCInfo存储信息
     */
    private void InitializationOCSP() {
        spUtil.setLastOpenPackgeName("");
        spUtil.setLastOpenTime("");
        spUtil.setLastAppName("");
        spUtil.setLastAppVerison("");
        spUtil.setAppProcess("");
    }

    /**
     * 读取配置信息是否获取地理位置信息
     */
    private void LocationInfo() {
        ApplicationInfo appInfo = null;
        try {
            appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        SPHodler.getInstance(MonitorService.this).setLocation(appInfo.metaData.getString(Constants.LI));
    }

    private void getKeyAndChannel() {
        try {
            Constants.APP_KEY_VALUE = SPHodler.getInstance(context).getKey();
            Constants.APP_CHANNEL_VALUE = SPHodler.getInstance(context).getChannel();
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }
}
