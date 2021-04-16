package me.hhhaiai.mptils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Copyright 2019 sanbo Inc. All rights reserved.
 * @Description: 多进程保护
 * @Version: 1.0
 * @Create: 2019-08-04 17:26:07
 * @author: sanbo
 */
public class MpSyncer {

    /**
     * 获取当前进程的名称
     *
     * @param context
     * @return
     */
    public static String getCurrentProcessName(Context context) {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                    if (info.pid == pid) {
                        return info.processName;
                    }
                }
            }
        } catch (Throwable e) {
        }
        return "";
    }

    public static boolean isMainProcess(Context context) {
        try {
            if (context == null) {
                return false;
            }
            ActivityManager activityManager =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningApps = null;
            if (activityManager != null) {
                runningApps = activityManager.getRunningAppProcesses();
            }
            if (runningApps == null) {
                return false;
            }
            String process = "";
            for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
                if (proInfo.pid == android.os.Process.myPid()) {
                    if (proInfo.processName != null) {
                        process = proInfo.processName;
                    }
                }
            }
            String mainProcessName = null;
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            if (applicationInfo != null) {
                mainProcessName = context.getApplicationInfo().processName;
            }
            if (mainProcessName == null) {
                mainProcessName = context.getPackageName();
            }
            return mainProcessName.equals(process);
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 创建锁文件
     *
     * @param cxt
     * @param fileName 锁文件名称
     * @param time     锁使用间隔，为了不影响首次使用,时间前移一秒
     * @returnc
     */
    public boolean createLockFile(Context cxt, String fileName, long time) {
        try {
            cxt = EContext.getContext(cxt);
            if (cxt == null) {
                return false;
            }
            File dev = new File(cxt.getFilesDir(), fileName);
            // check parent Dir
            File parentDir = new File(dev.getParent());
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            if (!dev.exists()) {
                boolean rs = dev.createNewFile();
                if (rs) {
                    dev.setExecutable(true);
                    dev.setWritable(true);
                    dev.setReadable(true);
                    dev.setLastModified(System.currentTimeMillis() - (time + 1000));
                }
            }
            if (dev.exists()) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }


    /**
     * 获取锁文件的最后修改时间
     *
     * @param cxt
     * @param fileName
     * @return
     */
    private long getLockFileLastModifyTime(Context cxt, String fileName) {
        try {
            cxt = EContext.getContext(cxt);
            if (cxt != null) {
                File dev = new File(cxt.getFilesDir(), fileName);
                if (!dev.exists()) {
                    createLockFile(cxt, fileName, 0);
                }
                return dev.lastModified();
            }

        } catch (Throwable e) {
        }
        return -1;
    }

    /**
     * 设置锁文件的修改时间
     *
     * @param cxt
     * @param fileName
     * @param time
     * @return
     */
    public synchronized boolean setLockLastModifyTime(Context cxt, String fileName, long time) {
        try {
            cxt = EContext.getContext(cxt);
            if (cxt != null) {
                File dev = new File(cxt.getFilesDir(), fileName);
                if (!dev.exists()) {
                    dev.createNewFile();
                    dev.setExecutable(true);
                    dev.setWritable(true);
                    dev.setReadable(true);
                }
                dev.setLastModified(time);

//                if (EGContext.FLAG_DEBUG_INNER) {
//                    ELOG.i(SystemUtils.getCurrentProcessName(cxt) + "-----setLockLastModifyTime-----set  success-----");
//                }

//                if (dev.lastModified() == time) {
////                    if (EGContext.FLAG_DEBUG_INNER) {
////                        ELOG.i(SystemUtils.getCurrentProcessName(cxt) + "-----setLockLastModifyTime-----haskey: " + mFilenameAndLocks.containsKey(fileName));
////                    }
//                    if (mFilenameAndLocks.containsKey(fileName)) {
//
//                        Locks locks = mFilenameAndLocks.get(fileName);
////                        if (EGContext.FLAG_DEBUG_INNER) {
////                            ELOG.i(SystemUtils.getCurrentProcessName(cxt) + "-----setLockLastModifyTime-----locks: " + locks);
////                        }
//                        if (locks != null) {
//                            locks.safeClose();
//                        }
//                    }
//                    return true;
//                }
            }
        } catch (Throwable e) {
        }
        return false;
    }

    private Map<String, Locks> mFilenameAndLocks = new HashMap<String, Locks>();


    /**
     * 根据锁文件时间，判断是否达到触发时间
     *
     * @param cxt  上下文
     * @param lock 文件名
     * @param time 轮询间隔
     * @param now  本次时间
     * @return
     */
    public synchronized boolean isNeedWorkByLockFile(Context cxt, String lock, long time, long now) {
        try {
            cxt = EContext.getContext(cxt);
            if (cxt == null) {
                return false;
            }

            long lastModifyTime = getLockFileLastModifyTime(cxt, lock);
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.i("-----isNeedWorkByLockFile----time dur: " + Math.abs(lastModifyTime - now));
//            }
            if (Math.abs(lastModifyTime - now) > time) {
                // 文件同步
                File f = new File(cxt.getFilesDir(), lock);
                if (!f.exists()) {
                    f.createNewFile();
                }
                RandomAccessFile randomFile = null;
                FileChannel fileChannel = null;
                FileLock fl = null;
                try {
                    // 持有锁
                    if (mFilenameAndLocks.containsKey(lock)) {
//                        if (EGContext.FLAG_DEBUG_INNER) {
//                            ELOG.i(SystemUtils.getCurrentProcessName(cxt) + "-----getLockFileLastModifyTime-----has-----");
//                        }
                        return true;
                    } else {
                        randomFile = new RandomAccessFile(f, "rw");
                        fileChannel = randomFile.getChannel();
                        fl = fileChannel.tryLock();
                        if (fl != null) {
                            mFilenameAndLocks.put(lock, new Locks(fl, randomFile, fileChannel));
//                            if (EGContext.FLAG_DEBUG_INNER) {
//                                ELOG.i(SystemUtils.getCurrentProcessName(cxt) + "-----getLockFileLastModifyTime-----new-----");
//                            }
                            return true;
                        } else {
                            return false;
                        }

                    }
                } catch (Throwable e) {
                }
            } else {
                return false;
            }

        } catch (Throwable t) {
        }
        return false;
    }

    private static class HOLDER {
        private static MpSyncer INSTANCE = new MpSyncer();
    }

    private MpSyncer() {
    }

    public static MpSyncer getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * @Copyright © 2019 sanbo Inc. All rights reserved.
     * @Description: 同步文件锁
     * @Version: 1.0
     * @Create: 2019-08-05 18:43:31
     * @author: sanbo
     * @mail: xueyongfu@analysys.com.cn
     */
    private class Locks {
        private FileLock mLock = null;
        private RandomAccessFile mRandomFile = null;
        private FileChannel mFileChannel = null;


        public Locks(FileLock lock, RandomAccessFile randomFile, FileChannel fileChannel) {
            this.mLock = lock;
            this.mRandomFile = randomFile;
            this.mFileChannel = fileChannel;
        }


        public void safeClose() {
            if (mLock != null) {
                Reflect.invokeObjectMethod(mLock, "close");
            }
            if (mRandomFile != null) {
                try {
                    mRandomFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mFileChannel != null) {
                try {
                    mFileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
