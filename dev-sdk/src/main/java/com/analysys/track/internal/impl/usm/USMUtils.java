package com.analysys.track.internal.impl.usm;

import android.annotation.TargetApi;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.PkgList;
import com.analysys.track.utils.reflectinon.ClazzUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: USM辅助功能工具类
 * @Version: 1.0
 * @Create: 2019-11-11 16:21:43
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class USMUtils {
    /**
     * 是否有打开辅助功能的设置页面
     *
     * @param context
     * @return
     */
    public static boolean isOption(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                PackageManager packageManager = context.getApplicationContext()
                        .getPackageManager();
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                return list.size() > 0;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    /**
     * 打开辅助功能设置界面
     *
     * @param context
     */
    public static void openUSMSetting(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                context.startActivity(intent);
            }
        } catch (Throwable e) {
        }
    }


    /**
     * 获取UsageEvents对象
     *
     * @param beginTime
     * @param endTime
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Object getUsageEvents(long beginTime, long endTime, Context context) {
        try {
//            if (context.getApplicationInfo().targetSdkVersion > 27 || Build.VERSION.SDK_INT > 27) {
//                return null;
//            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return null;
            }
            boolean hasNextEvent = false;
            Object usageEvents = getUsageEventsByApi(beginTime, endTime, context);
            if (usageEvents != null) {
                hasNextEvent = (boolean) ClazzUtils.invokeObjectMethod(usageEvents, "hasNextEvent");
                if (hasNextEvent) {
                    return usageEvents;
                }
            }
            usageEvents = getUsageEventsByInvoke(beginTime, endTime, context);
            if (usageEvents != null) {
                hasNextEvent = (boolean) ClazzUtils.invokeObjectMethod(usageEvents, "hasNextEvent");
                if (hasNextEvent) {
                    return usageEvents;
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(BuildConfig.tag_snap, e);
            }
        }
        return null;
    }

    /**
     * 系统API获取UsageEvents
     *
     * @param beginTime
     * @param endTime
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Object getUsageEventsByApi(long beginTime, long endTime, Context context) {
        try {
//            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
//            UsageEvents usageEvents = usm.queryEvents(beginTime, endTime);

            return ClazzUtils.invokeObjectMethod(context.getApplicationContext()
                            .getSystemService(Context.USAGE_STATS_SERVICE), "queryEvents",
                    new Class[]{long.class, long.class}, new Object[]{beginTime, endTime});
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(BuildConfig.tag_snap, e);
            }
        }
        return null;
    }

    /**
     * 系统API获取UsageEvents
     *
     * @param beginTime
     * @param endTime
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Object getUsageEventsByInvoke(long beginTime, long endTime, Context context) {
        try {
//            if (context.getApplicationInfo().targetSdkVersion > 27 || Build.VERSION.SDK_INT > 27) {
//                return null;
//            }
//            if (Build.VERSION.SDK_INT > 29) {
//                //未来 android 11 防止
//                return null;
//            }
            context = EContextHelper.getContext(context);
            if (context == null) {
                return null;
            }
            if (endTime <= beginTime) {
                beginTime = endTime - EGContext.TIME_HOUR * 24 * 2;
            }
            //android.app.usage.IUsageStatsManager$Stub$Proxy
            Object mService = ClazzUtils.getObjectFieldObject(context.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE), "mService");
            if (mService == null) {
                Object iBinder = ClazzUtils.invokeStaticMethod("android.os.ServiceManager", "getService", new Class[]{String.class}, new Object[]{"usagestats"});
                mService = ClazzUtils.invokeStaticMethod("android.app.usage.IUsageStatsManager$Stub", "asInterface", new Class[]{IBinder.class}, new Object[]{iBinder});
            }
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_usm, "mService: " + mService);
            }
            if (mService == null) {
                return null;
            }
            Set<String> pkgs = PkgList.getAppPackageList(context);

            Object usageEvents = null;
            for (String opname : pkgs) {
                try {
                    //UsageEvents
                    usageEvents = ClazzUtils.invokeObjectMethod(mService, "queryEvents", new Class[]{long.class, long.class, String.class}, new Object[]{beginTime, endTime, opname});
                    if (usageEvents == null) {
                        continue;
                    }
                    boolean b = (boolean) ClazzUtils.invokeObjectMethod(usageEvents, "hasNextEvent");
                    if (b) {
                        return usageEvents;
                    }
                } catch (Throwable e) {
                    if (BuildConfig.ENABLE_BUGLY) {
                        BugReportForTest.commitError(e);
                    }
                }
            }
            return usageEvents;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(e);
            }
        }
        return null;
    }


    /**
     * 获取UsageStats列表
     *
     * @param context
     * @param beginTime
     * @param endTime
     * @return
     */
    public static List<UsageStats> getUsageStats(Context context, long beginTime, long endTime) {
        List<UsageStats> usageStatsList = new ArrayList<UsageStats>();
        try {
//            if (context.getApplicationInfo().targetSdkVersion > 27 || Build.VERSION.SDK_INT > 27) {
//                return null;
//            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return usageStatsList;
            }
            List<UsageStats> temp = getUsageStatsListByApi(context, beginTime, endTime);
            if (temp != null && temp.size() > 0) {
                usageStatsList.addAll(temp);
            } else {
                List<UsageStats> temp1 = getUsageStatsListByInvoke(context, beginTime, endTime);
                if (temp1 != null && temp1.size() > 0) {
                    usageStatsList.addAll(temp1);
                }
            }

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(BuildConfig.tag_snap, e);
            }
        }
        return usageStatsList;
    }


    /**
     * API 获取UsageStatsList
     *
     * @param context
     * @param beginTime
     * @param endTime
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static List<UsageStats> getUsageStatsListByApi(Context context, long beginTime, long endTime) {
//        /**
//         * 系统API获取
//         */
//        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
//        List<UsageStats> uss = usm.queryUsageStats( UsageStatsManager.INTERVAL_BEST, beginTime, endTime);
        try {
            return (List<UsageStats>) ClazzUtils.invokeObjectMethod(context.getApplicationContext()
                            .getSystemService(Context.USAGE_STATS_SERVICE), "queryUsageStats",
                    new Class[]{int.class, long.class, long.class}, new Object[]{UsageStatsManager.INTERVAL_BEST, beginTime, endTime});
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(BuildConfig.tag_snap, e);
            }
        }
        return null;
    }

    /**
     * 反射获取UsageStatsList
     *
     * @param context
     * @param beginTime
     * @param endTime
     * @return
     */
    private static List<UsageStats> getUsageStatsListByInvoke(Context context, long beginTime, long endTime) {
        try {
            UsageStatsManager a;
            //android.app.usage.IUsageStatsManager$Stub$Proxy
            Object mService = ClazzUtils.getObjectFieldObject(context.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE), "mService");
            if (mService == null) {
                Object iBinder = ClazzUtils.invokeStaticMethod("android.os.ServiceManager", "getService", new Class[]{String.class}, new Object[]{"usagestats"});
                mService = ClazzUtils.invokeStaticMethod("android.app.usage.IUsageStatsManager$Stub", "asInterface", new Class[]{IBinder.class}, new Object[]{iBinder});
            }
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_usm, "mService: " + mService);
            }
            if (mService == null) {
                return null;
            }

            Set<String> pkgs = PkgList.getAppPackageList(EContextHelper.getContext(context));

            for (String pkg : pkgs) {
                try {
                    // 返回值android.content.pm.ParceledListSlice
                    Object parceledListSlice = ClazzUtils.invokeObjectMethod(mService, "queryUsageStats",
                            new Class[]{int.class, long.class, long.class, String.class},
                            new Object[]{UsageStatsManager.INTERVAL_BEST, beginTime, endTime, pkg}
                    );
                    if (parceledListSlice != null) {
                        return (List<UsageStats>) ClazzUtils.invokeObjectMethod(parceledListSlice, "getList");
                    }
                } catch (Throwable e) {
                    if (BuildConfig.ENABLE_BUGLY) {
                        BugReportForTest.commitError(BuildConfig.tag_snap, e);
                    }
                }

            }

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(BuildConfig.tag_snap, e);
            }
        }
        return null;
    }

}