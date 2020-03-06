package com.analysys.track.utils;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.receiver.AnalysysReceiver;

public class ReceiverUtils {
    private AnalysysReceiver mReceiver = null;
    @SuppressWarnings("unused")
    private boolean sWorkStatus = false;

    private ReceiverUtils() {
    }

    public static ReceiverUtils getInstance() {
        return Holder.INSTANCE;
    }

    @SuppressWarnings("deprecation")
    public void registAllReceiver(Context context) {
        try {
            // L.i("[%s]----registAllReceiver...begin....",
            // SystemUtils.getCurrentProcessName(mContext));
            setWork(true);
            if (mReceiver == null) {
                mReceiver = new AnalysysReceiver();
                // net checkMultiProcessForWork
                IntentFilter intentFilter = new IntentFilter();
                if (Build.VERSION.SDK_INT < 24) {
                    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                    intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                    context.registerReceiver(mReceiver, intentFilter);
                }
                // 启动
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
                intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                context.registerReceiver(mReceiver, intentFilter);

                // 连接、断开电源
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
                intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
                intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                context.registerReceiver(mReceiver, intentFilter);


                //解锁唤醒
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_USER_PRESENT);
                intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                context.registerReceiver(mReceiver, intentFilter);

                // battery
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
                intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                context.registerReceiver(mReceiver, intentFilter);

                // IUUinfo
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
                intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
                intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
                intentFilter.addDataScheme("package");
                context.registerReceiver(mReceiver, intentFilter);

                // 关屏、锁屏
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
                intentFilter.addAction(Intent.ACTION_SCREEN_ON);
                intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                context.registerReceiver(mReceiver, intentFilter);
                //高版本兼容  时间修改
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
                intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                context.registerReceiver(mReceiver, intentFilter);
                //高版本兼容  时区修改
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
                intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                context.registerReceiver(mReceiver, intentFilter);
                //高版本兼容  location修改
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
                intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                context.registerReceiver(mReceiver, intentFilter);
                //高版本兼容  闹铃修改
                intentFilter = new IntentFilter();
                intentFilter.addAction(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED);
                intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                context.registerReceiver(mReceiver, intentFilter);



                // thread
                intentFilter = new IntentFilter();
                intentFilter.addAction(EGContext.ACTION_MTC_LOCK);
                intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                context.registerReceiver(mReceiver, intentFilter);
                // 策略
                intentFilter = new IntentFilter();
                intentFilter.addAction(EGContext.ACTION_UPDATE_POLICY);
                intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                context.registerReceiver(mReceiver, intentFilter);
                // 清数据的
                intentFilter = new IntentFilter();
                intentFilter.addAction(EGContext.ACTION_NOTIFY_CLEAR);
                intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                context.registerReceiver(mReceiver, intentFilter);

            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(e);
            }
        }
    }

    public void unRegistAllReceiver(Context context) {
        try {
            setWork(false);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(e);
            }
        }
    }

    /**
     * @param isWorking
     */
    public void setWork(boolean isWorking) {
        sWorkStatus = isWorking;
    }

    private static class Holder {
        private static final ReceiverUtils INSTANCE = new ReceiverUtils();
    }
}
