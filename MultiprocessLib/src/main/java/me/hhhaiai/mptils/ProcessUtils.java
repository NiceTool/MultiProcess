package me.hhhaiai.mptils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;


public class ProcessUtils {
    private static String mAppVersionName;
    private static Bundle mConfigBundle;

    /**
     * 获取应用名称
     *
     * @param context Context
     * @return 应用名称
     */
    public static CharSequence getAppName(Context context) {
        if (context == null) return "";
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            return appInfo.loadLabel(packageManager);
        } catch (Exception e) {
        }
        return "";
    }



    /**
     * 杀死自己所在进程
     */
    public static void killSelfProcess() {
        Process.killProcess(Process.myPid());
    }

    public static void killProcess(int pid) {
        Process.killProcess(pid);
    }

    public static void killAllProcess(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.uid == Process.myUid() && appProcess.pid != Process.myPid()) {
                killProcess(appProcess.pid);
            }
        }
        killSelfProcess();
    }

    /**
     * 获取 App 版本号
     *
     * @param context Context
     * @return App 的版本号
     */
    public static String getAppVersionName(Context context) {
        if (context == null) return "";
        if (!TextUtils.isEmpty(mAppVersionName)) {
            return mAppVersionName;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            mAppVersionName = packageInfo.versionName;
        } catch (Exception e) {

        }
        return mAppVersionName;
    }

    /**
     * 获取主进程的名称
     *
     * @param context Context
     * @return 主进程名称
     */
    public static String getMainProcessName(Context context) {
        if (context == null) {
            return "";
        }
        try {
            return context.getApplicationContext().getApplicationInfo().processName;
        } catch (Exception ex) {
        }
        return "";
    }

    /**
     * 判断当前进程名称是否为主进程
     *
     * @param context Context
     * @return 是否主进程
     */
    public static boolean isMainProcess(Context context) {
        if (context == null) {
            return false;
        }

        String mainProcessName = getMainProcessName(context);

        if (TextUtils.isEmpty(mainProcessName)) {
            return true;
        }

        String currentProcess = getCurrentProcessName();
        return TextUtils.isEmpty(currentProcess) || mainProcessName.equals(currentProcess);
    }

    /**
     * 获取 Application 标签的 Bundle 对象
     *
     * @param context Context
     * @return Bundle
     */
    public static Bundle getAppInfoBundle(Context context) {
        if (mConfigBundle == null) {
            try {
                final ApplicationInfo appInfo = context.getApplicationContext().getPackageManager()
                        .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                mConfigBundle = appInfo.metaData;
            } catch (final PackageManager.NameNotFoundException e) {

            }
        }

        if (mConfigBundle == null) {
            return new Bundle();
        }
        return mConfigBundle;
    }

  private static String processName = null;
    /**
     * 获得当前进程的名字
     *
     * @return 进程名称
     */
    public static String getCurrentProcessName() {
        try {
            if (processName != null) {
                return processName;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                processName = Application.getProcessName();
                return processName;
            }

            String currentProcess = getCurrentProcessNameByCmd();
            if (TextUtils.isEmpty(currentProcess)) {
                processName = getCurrentProcessNameByAT();
            }
            return processName;
        } catch (Exception e) {

        }
        return null;
    }

    private static String getCurrentProcessNameByAT() {
        String processName = null;
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThread = Class.forName("android.app.ActivityThread", false, Application.class.getClassLoader());
            Method declaredMethod = activityThread.getDeclaredMethod("currentProcessName", (Class<?>[]) new Class[0]);
            declaredMethod.setAccessible(true);
            Object processInvoke = declaredMethod.invoke(null);
            if (processInvoke instanceof String) {
                processName = (String) processInvoke;
            }
        } catch (Throwable e) {
            //ignore
        }
        return processName;
    }

    private static String getCurrentProcessNameByCmd() {
        FileInputStream in = null;
        try {
            String fn = "/proc/self/cmdline";
            in = new FileInputStream(fn);
            byte[] buffer = new byte[256];
            int len = 0;
            int b;
            while ((b = in.read()) > 0 && len < buffer.length) {
                buffer[len++] = (byte) b;
            }
            if (len > 0) {
                return new String(buffer, 0, len, "UTF-8");
            }
        } catch (Throwable e) {
            // ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {

                }
            }
        }
        return null;
    }
}


