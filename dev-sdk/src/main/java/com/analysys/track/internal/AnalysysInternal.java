package com.analysys.track.internal;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.internal.work.CrashHandler;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.internal.work.ServiceHelper;
import com.analysys.track.utils.ActivityCallBack;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.NinjaUtils;
import com.analysys.track.utils.OAIDHelper;
import com.analysys.track.utils.ReceiverUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.DevStatusChecker;
import com.analysys.track.utils.reflectinon.PatchHelper;
import com.analysys.track.utils.sp.SPHelper;

import java.io.File;

public class AnalysysInternal {
    private static boolean hasInit = false;

    // 初始化反射模快
    private AnalysysInternal() {
    }

    public static AnalysysInternal getInstance(Context context) {
        try {
            // 初始化日志
            ELOG.init(EContextHelper.getContext());
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return Holder.instance;
    }

    public static boolean isInit() {
        return hasInit;
    }

    /**
     * 初始化函数,可能为耗时操作的，判断是否主线程，需要开子线程做
     *
     * @param key
     * @param channel
     * @param initType true 主动初始化 false 被动初始化
     */
    public synchronized void initEguan(final String key, final String channel, final boolean initType) {
        // 单进程内防止重复注册
        if (hasInit) {
            return;
        }
        hasInit = true;
        // 防止影响宿主线程中的任务
        EThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    init(key, channel, initType);
                } catch (Throwable e) {
                    if (BuildConfig.ENABLE_BUGLY) {
                        BuglyUtils.commitError(e);
                    }
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.e(e);
                    }
                }

            }
        });
    }

    /**
     * key支持参数设置、XML文件设置， 参数设置优先级大于XML设置
     *
     * @param key
     * @param channel
     */
    @SuppressWarnings("deprecation")
    private void init(String key, String channel, boolean initType) {


        // 0.首先检查是否有Context
        Context ctx = EContextHelper.getContext();
        if (ctx == null) {
            return;
        }
        SPHelper.setBooleanValue2SP(ctx, EGContext.KEY_INIT_TYPE, initType);
        NinjaUtils.checkOldFile(ctx);
        Application application = (Application) ctx;
        application.registerActivityLifecycleCallbacks(ActivityCallBack.getInstance());


        SPHelper.setIntValue2SP(ctx, EGContext.KEY_ACTION_SCREEN_ON_SIZE, EGContext.FLAG_START_COUNT + 1);
        SystemUtils.updateAppkeyAndChannel(ctx, key, channel);// updateSnapshot sp

        // 1. 设置错误回调
        CrashHandler.getInstance().setCallback(null);// 不依赖ctx
        // 2.初始化加密
        EncryptUtils.init(ctx);
        // 3.初始化多进程
        initSupportMultiProcess(ctx);
        // 4. 只能注册一次，不能注册多次
        ReceiverUtils.getInstance().registAllReceiver(ctx);
        // 5. 启动工作机制
        if (MessageDispatcher.getInstance(ctx).jobStartLogic(false)) {
            return;
        }
        MessageDispatcher.getInstance(ctx).initModule();

        ServiceHelper.getInstance(EContextHelper.getContext()).startSelfService();
        // 6. 根据屏幕调整工作状态
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            boolean isScreenOn = pm.isScreenOn();
            // 如果为true，则表示屏幕正在使用，false则屏幕关闭。
            if (!isScreenOn) {
                ReceiverUtils.getInstance().setWork(false);
            }
        }
        // 7.检查加密模块是否正常，false重新初始化
        if (!EncryptUtils.checkEncryptKey(ctx)) {
            EncryptUtils.reInitKey(ctx);
        }

        Log.i(EGContext.LOGTAG_USER, String.format("[%s] init SDK (%s) success! ", SystemUtils.getCurrentProcessName(EContextHelper.getContext()), EGContext.SDK_VERSION));
        // 8.是否启动工作
//        if (!DevStatusChecker.getInstance().isDebugDevice(EContextHelper.getContext())) {
//            String version = SPHelper.getStringValueFromSP(EContextHelper.getContext(), UploadKey.Response.PatchResp.PATCH_VERSION, "");
//            if (!TextUtils.isEmpty(version)) {
//                File file = new File(EContextHelper.getContext().getFilesDir(), version + ".jar");
//                if (file.exists()) {
//                    PatchHelper.loads(EContextHelper.getContext(), file);
//                } else {
//                    PolicyImpl.getInstance(EContextHelper.getContext()).clear();
//                    // 清除本地缓存
//                    SPHelper.setStringValue2SP(EContextHelper.getContext(), UploadKey.Response.PatchResp.PATCH_VERSION, "");
//                    SPHelper.setStringValue2SP(EContextHelper.getContext(), UploadKey.Response.PatchResp.PATCH_SIGN, "");
//                    SPHelper.setStringValue2SP(EContextHelper.getContext(), UploadKey.Response.PatchResp.PATCH_METHODS, "");
//                    clear();
//                }
//            } else {
//                // 没缓存文件名. 检查策略是否存在策略
//                String policy = SPHelper.getStringValueFromSP(EContextHelper.getContext(), UploadKey.Response.RES_POLICY_VERSION, "");
//                //存在策略清所有策略
//                if (!TextUtils.isEmpty(policy)) {
//                    PolicyImpl.getInstance(EContextHelper.getContext()).clear();
//                }
//            }
//        } else {
//            // 清除老版本缓存文件
//            String oldVersion = SPHelper.getStringValueFromSP(EContextHelper.getContext(), UploadKey.Response.PatchResp.PATCH_VERSION, "");
//            if (!TextUtils.isEmpty(oldVersion)) {
//                new File(EContextHelper.getContext().getFilesDir(), oldVersion + ".jar").delete();
//            }
//            // 清除本地缓存
//            SPHelper.setStringValue2SP(EContextHelper.getContext(), UploadKey.Response.PatchResp.PATCH_VERSION, "");
//            SPHelper.setStringValue2SP(EContextHelper.getContext(), UploadKey.Response.PatchResp.PATCH_SIGN, "");
//            SPHelper.setStringValue2SP(EContextHelper.getContext(), UploadKey.Response.PatchResp.PATCH_METHODS, "");
//            clear();
//        }

        if (Build.VERSION.SDK_INT >= 29) {
            OAIDHelper.tryGetOaidAndSave(ctx);
        }

        try {
            // 9. 清除以前的SP和DB
            SPHelper.removeSPFiles(EContextHelper.getContext(), EGContext.SP_NAME);

            File file = SPHelper.getNewSharedPrefsFile(EContextHelper.getContext(), "ana_sp_xml");
            if (file.exists() && file.isFile()) {
                file.delete();
            }

            file = EContextHelper.getContext().getDatabasePath("e.data");
            if (file.exists() && file.isFile()) {
                file.delete();
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }

        }

    }

    private void clear() {
        File dir = EContextHelper.getContext().getFilesDir();
        String[] ss = dir.list();
        for (String fn : ss) {
            if (!TextUtils.isEmpty(fn) && fn.endsWith(".jar")) {
                new File(dir, fn).delete();
            }
        }
    }

    /**
     * 初始化支持多进程
     *
     * @param cxt
     */
    private void initSupportMultiProcess(Context cxt) {
        try {
            if (cxt == null) {
                return;
            }
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_UPLOAD, EGContext.TIME_SYNC_UPLOAD);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_APPSNAPSHOT, EGContext.TIME_HOUR * 3);
            if (Build.VERSION.SDK_INT < 26) {
                MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_OC, EGContext.TIME_SECOND * 5);
            } else {
                MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_OC, EGContext.TIME_SYNC_OC_OVER_5);
            }
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_LOCATION, EGContext.TIME_SYNC_LOCATION);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_HOTFIX, EGContext.TIME_SECOND * 5);
//            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SP_WRITER, EGContext.TIME_SYNC_SP);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SCREEN_OFF_BROADCAST, EGContext.TIME_SYNC_BROADCAST);
//            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SCREEN_ON_BROADCAST, EGContext.TIME_SYNC_BROADCAST);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SNAP_ADD_BROADCAST, EGContext.TIME_SECOND * 5);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SNAP_DELETE_BROADCAST, EGContext.TIME_SECOND * 5);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SNAP_UPDATE_BROADCAST, EGContext.TIME_SECOND * 5);
//            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_BOOT_BROADCAST, EGContext.TIME_SYNC_DEFAULT);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_BATTERY_BROADCAST, EGContext.TIME_SECOND * 5);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_NET, EGContext.TIME_SECOND * 5);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
    }

    private static class Holder {
        private static AnalysysInternal instance = new AnalysysInternal();
    }
}
