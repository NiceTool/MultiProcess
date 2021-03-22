package com.analysys.track.internal.impl.ftime;

import android.content.Context;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.MDate;
import com.analysys.track.utils.pkg.PkgList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * @Copyright © 2020 analsys Inc. All rights reserved.
 * @Description: last modify by file utils
 * @Version: 1.0
 * @Create: 2020-11-17 11:58:02
 * @author: Administrator
 */
public class LmFileUitls {

    public static class AppTime {
        private String sPackageName;
        private long lastActiveTime;

        public String getPackageName() {
            return sPackageName;
        }

        public long getLastActiveTime() {
            return lastActiveTime;
        }

        public AppTime(String __pkg, long __time) {
            this.sPackageName = __pkg;
            this.lastActiveTime = __time;
        }

        @Override
        public String toString() {
            return String.format("[%s]---->%s ", sPackageName, MDate.getDateFromTimestamp(lastActiveTime));
        }
    }

//    public static List<AppTime> getLastAliveTimeInBaseDir(Context context) {
//
//        List<String> pkgs = PkgList.getInstance(context).getAppPackageList();
//        List<AppTime> list = new ArrayList<AppTime>();
//        for (String pkg : pkgs) {
//            long filesTime = getTime(new File("/sdcard/Android/data/" + pkg + "/files"));
//            long cacheTime = getTime(new File("/sdcard/Android/data/" + pkg + "/cache"));
//            long time = Math.max(filesTime, cacheTime);
//            time = Math.max(time, getTime(new File("/sdcard/Android/data/" + pkg + "/MicroMsg")));
//            filesTime = getTime(new File("/data/data/" + pkg + "/files"));
//            time = Math.max(filesTime, time);
//            cacheTime = getTime(new File("/data/data/" + pkg + "/cache"));
//            time = Math.max(cacheTime, time);
//            if (time == 0) {
//                continue;
//            }
//            list.add(new AppTime(pkg, time));
//        }
//
//        Collections.sort(list, new Comparator<AppTime>() {
//            @Override
//            public int compare(AppTime at1, AppTime at2) {
//                return (int) (at2.lastActiveTime / 1000 - at1.lastActiveTime / 1000);
//            }
//        });
//
//        return list;
//    }

    public static List<AppTime> getLastAliveTimeInSD(Context context, boolean isAll) {
        List<AppTime> list = new ArrayList<AppTime>();
        for (String pkg : PkgList.getInstance(context).getAppPackageList()) {
            try {
                File f = new File("/sdcard/Android/data/" + pkg);
                File fd = new File("/data/data/" + pkg);
                long time = getTime(new File(f, "files"));
                time = Math.max(time, getTime(new File(f, "cache")));
                time = Math.max(time, getTime(new File(f, "MicroMsg")));
                time = Math.max(getTime(new File(fd, "files")), time);
                time = Math.max(getTime(new File(fd, "cache")), time);
                if (isAll) {
                    time = Math.max(iteratorFiles(f, 0, false), time);
                    time = Math.max(iteratorFiles(fd, 0, false), time);
                }

                if (time == 0) {
                    continue;
                }
                list.add(new AppTime(pkg, time));

            } catch (Throwable e) {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_finfo, e);
                }
            }

        }

        Collections.sort(list, new Comparator<AppTime>() {
            @Override
            public int compare(AppTime at1, AppTime at2) {
                return (int) (at2.lastActiveTime / 1000 - at1.lastActiveTime / 1000);
            }
        });

        return list;
    }

    private static long getTime(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        return file.lastModified();
    }

    /**
     * 遍历获取末次访问时间，如果target版本为29或以上(android 10以上)或出现没权限获取问题
     * context.getApplicationInfo().targetSdkVersion
     *
     * @param file
     * @param time
     * @param isLog
     * @return
     */
    public static long iteratorFiles(File file, long time, boolean isLog) {
        File[] fs = file.listFiles();
        if (fs != null) {
            for (File f : fs) {
                try {
                    // 支持宏编译,不打印日志可直接隐藏
                    if (BuildConfig.logcat) {
                        if (isLog) {
                            Log.d("sanbo", "上次时间:" + time + "-----" + getTime(f) + "[" + f.getAbsolutePath() + "] 文件时间: " + MDate.getDateFromTimestamp(f.lastModified()));
                        }
                    }
                    time = Math.max(getTime(f), time);
                    if (f.isDirectory()) {
                        iteratorFiles(f, time, isLog);
                    }
                } catch (Throwable e) {
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_finfo, e);
                    }
                }
            }
        }
        return time;
    }

//    private static void logi(File f) {
//        StringBuffer sb = new StringBuffer();
//        String path = f.getPath();
//        sb.append("============[" + path + "]访问情况================\n")
//                .append("[").append(path).append("] exists: ").append(f.exists()).append("\n")
//                .append("[").append(path).append("] canRead: ").append(f.canRead()).append("\n")
//                .append("[").append(path).append("] canExecute: ").append(f.canExecute()).append("\n")
//                .append("[").append(path).append("] canWrite: ").append(f.canWrite()).append("\n")
//                .append("[").append(path).append("] getFreeSpace: ").append(f.getFreeSpace()).append("\n")
//                .append("[").append(path).append("] list: ").append(f.list()).append("\n")
//                .append("[").append(path).append("] listFiles: ").append(f.listFiles()).append("\n")
//        ;
//        Log.i("sanbo", sb.toString());
//    }
}
