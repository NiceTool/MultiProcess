package com.analysys.track.internal.impl;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.SystemUtils;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 广播处理类
 * @Version: 1.0
 * @Create: 2019-08-07 17:45:32
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class ReceiverImpl {

    /**
     * 处理接收到的广播
     *
     * @param context
     * @param intent
     */
    public void process(final Context context, final Intent intent) {
        if (intent == null) {
            return;
        }
        long currentTime = System.currentTimeMillis();

        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            final String packageName = getPkgName(intent);
            if (TextUtils.isEmpty(packageName)) {
                return;
            }

            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_SNAP_ADD_BROADCAST,
                    EGContext.TIME_SECOND * 2, currentTime)) {
//                AppSnapshotImpl.getInstance(context)
//                        .processAppModifyMsg(packageName,
//                                Integer.parseInt(EGContext.SNAP_SHOT_INSTALL),
//                                EGContext.FILES_SYNC_SNAP_ADD_BROADCAST);
                if (BuildConfig.logcat) {
                    ELOG.d(BuildConfig.tag_snap, " 处理广播接收到的信息 包:" + packageName + "----type: " + EGContext.SNAP_SHOT_INSTALL);
                }
                // 数据库操作修改包名和类型
                AppSnapshotImpl.getInstance(context).realProcessInThread(EGContext.SNAP_SHOT_INSTALL,
                        packageName, EGContext.FILES_SYNC_SNAP_ADD_BROADCAST);

            } else {
//                if (BuildConfig.logcat) {
//                    ELOG.v(BuildConfig.tag_snap, "安装app:" + packageName + "---->多进程中断");
//                }
                return;
            }


        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            final String packageName = getPkgName(intent);

            if (TextUtils.isEmpty(packageName)) {
                return;
            }
            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_SNAP_DELETE_BROADCAST,
                    EGContext.TIME_SECOND * 2, currentTime)) {
//                AppSnapshotImpl.getInstance(context)
//                        .processAppModifyMsg(packageName,
//                                Integer.parseInt(EGContext.SNAP_SHOT_UNINSTALL),
//                                EGContext.FILES_SYNC_SNAP_DELETE_BROADCAST);
                if (BuildConfig.logcat) {
                    ELOG.d(BuildConfig.tag_snap, " 处理广播接收到的信息 包:" + packageName + "----type: " + EGContext.SNAP_SHOT_UNINSTALL);
                }
                // 数据库操作修改包名和类型
                        AppSnapshotImpl.getInstance(context).realProcessInThread(EGContext.SNAP_SHOT_UNINSTALL,
                                packageName, EGContext.FILES_SYNC_SNAP_DELETE_BROADCAST);

            } else {
//                if (BuildConfig.logcat) {
//                    ELOG.v(BuildConfig.tag_snap, "卸载app:" + packageName + "---->多进程中断");
//                }
                return;
            }

        } else if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            final String packageName = getPkgName(intent);

            if (TextUtils.isEmpty(packageName)) {
                return;
            }
            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_SNAP_UPDATE_BROADCAST,
                    EGContext.TIME_SECOND * 2, currentTime)) {
//                AppSnapshotImpl.getInstance(context)
//                        .processAppModifyMsg(packageName,
//                                Integer.parseInt(EGContext.SNAP_SHOT_UPDATE),
//                                EGContext.FILES_SYNC_SNAP_UPDATE_BROADCAST);

                if (BuildConfig.logcat) {
                    ELOG.d(BuildConfig.tag_snap, " 处理广播接收到的信息 包:" + packageName + "----type: " + EGContext.SNAP_SHOT_UPDATE);
                }
                // 数据库操作修改包名和类型
                AppSnapshotImpl.getInstance(context).realProcessInThread(EGContext.SNAP_SHOT_UPDATE,
                        packageName, EGContext.FILES_SYNC_SNAP_UPDATE_BROADCAST);
            } else {
//                if (BuildConfig.logcat) {
//                    ELOG.v(BuildConfig.tag_snap, "更新app:" + packageName + "---->多进程中断");
//                }
                return;
            }

        } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            OCImpl.getInstance(context).processOCWhenScreenChange(true);

        } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            //  7.x以上版本 不操作
            if (Build.VERSION.SDK_INT >= 24) {
                return;
            }
            OCImpl.getInstance(context).processOCWhenScreenChange(false);
        } else if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            SystemUtils.runOnWorkThread(new Runnable() {
                @Override
                public void run() {
                    DeviceImpl.getInstance(context).processBattery(intent);
                }
            });
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            MessageDispatcher.getInstance(context).initModule();
        } else if (EGContext.ACTION_MTC_LOCK.equals(intent.getAction())) {
            EGContext.snap_complete = true;
        } else if (EGContext.ACTION_UPDATE_POLICY.equals(intent.getAction())) {
            PolicyImpl.getInstance(EContextHelper.getContext(context)).updatePolicyForReceiver(intent);
        } else if (EGContext.ACTION_NOTIFY_CLEAR.equals(intent.getAction())) {
//            makesureRunOnce(context, intent);
        }
    }

//    /**
//     * 确保3秒内执行执行一次
//     */
//    private void makesureRunOnce(Context context, Intent intent) {
//
//        if (mHandler == null) {
//            mHandler = new MyHandler(mContext);
//        }
//        if (!mHandler.hasMessages(1)) {
//            mHandler.removeMessages(1);
//        }
//        String pkg = intent.getStringExtra(EGContext.NOTIFY_PKG);
//        int type = intent.getIntExtra(EGContext.NOTIFY_TYPE, EGContext.NotifyStatus.NOTIFY_DEBUG);
//        if (!TextUtils.isEmpty(pkg) && context.getPackageName().equals(pkg)) {
//            Message msg = mHandler.obtainMessage();
//            msg.what = 1;
//            msg.arg1 = type;
//            mHandler.sendEmptyMessageDelayed(1, 3 * 1000);
//        }
//    }
//
//    private static Handler mHandler = null;
//
//    static class MyHandler extends Handler {
//        private Context mContext;
//
//        MyHandler(Context context) {
//            mContext = EContextHelper.getContext(context);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            if (msg.what == 1) {
//                if (msg.arg1 == EGContext.NotifyStatus.NOTIFY_NO_DEBUG) {
//                    PatchHelper.loadsIfExit(mContext);
//                } else if (msg.arg1 == EGContext.NotifyStatus.NOTIFY_DEBUG) {
//                    EThreadPool.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            PatchHelper.clearPatch(mContext);
//                        }
//                    }, 5 * 1000);
//                }
//            }
//        }
//    }


    /**
     * 当收到安装、卸载、更新的广播时.会收到dat内容如下: <code>package:com.sollyu.xposed.hook.model</code>
     */
    private String getPkgName(Intent intent) {
        String packageName = "";
        if (intent == null) {
            return packageName;
        }
        String data = intent.getDataString();
        if (!TextUtils.isEmpty(data) && data.startsWith(DATA_APK_STATUS_UPDATE)) {
            packageName = data.replace(DATA_APK_STATUS_UPDATE, "");
        }
        return packageName;
    }

    private static class HOLDER {
        private static ReceiverImpl INSTANCE = new ReceiverImpl();
    }

    private ReceiverImpl() {
    }

    public static ReceiverImpl getInstance() {
        return HOLDER.INSTANCE;
    }

    // 当收到安装、卸载、更新的广播时的data前缀
    private final String DATA_APK_STATUS_UPDATE = "package:";
}
