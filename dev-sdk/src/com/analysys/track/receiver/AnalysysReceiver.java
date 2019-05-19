package com.analysys.track.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.analysys.track.impl.DeviceImpl;
import com.analysys.track.utils.FileUtils;
import com.analysys.track.work.MessageDispatcher;
import com.analysys.track.utils.ELOG;

import com.analysys.track.internal.Content.EGContext;

public class AnalysysReceiver extends BroadcastReceiver {
    Context mContext;
    String PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED";
    String PACKAGE_REMOVED = "android.intent.action.PACKAGE_REMOVED";
    String PACKAGE_REPLACED = "android.intent.action.PACKAGE_REPLACED";

    String SCREEN_ON = "android.intent.action.SCREEN_ON";
    String SCREEN_OFF = "android.intent.action.SCREEN_OFF";
    String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    String BATTERY_CHANGED = "android.intent.action.BATTERY_CHANGED";
    String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    //上次结束时间
    public static long mLastCloseTime = 0;
    public static boolean isScreenOnOffBroadCastHandled = false;
    private static boolean isSnapShotAddBroadCastHandled = false;
    private static boolean isSnapShotDeleteBroadCastHandled = false;
    private static boolean isSnapShotUpdateBroadCastHandled = false;
    private static boolean isBatteryBroadCastHandled = false;
    private static boolean isBootBroadCastHandled = false;
    public static AnalysysReceiver getInstance() {
        return AnalysysReceiver.Holder.INSTANCE;
    }
    private static class Holder {
        private static final AnalysysReceiver INSTANCE = new AnalysysReceiver();
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent == null){
                return;
            }
            String data = intent.getDataString();
            String packageName = "";
            if(!TextUtils.isEmpty(data)){
                packageName = data.substring(8);
            }
            mContext = context.getApplicationContext();
            long currentTime = System.currentTimeMillis();
            if (PACKAGE_ADDED.equals(intent.getAction())) {
                try {
                    if(FileUtils.isNeedWorkByLockFile(mContext,EGContext.FILES_SYNC_SNAP_ADD_BROADCAST,EGContext.TIME_SYNC_DEFAULT,currentTime)){
                        FileUtils.setLockLastModifyTime(mContext,EGContext.FILES_SYNC_SNAP_ADD_BROADCAST,currentTime);
                    }else {
                        return;
                    }
                    if(!isSnapShotAddBroadCastHandled){
                        isSnapShotAddBroadCastHandled = true;
                    }else {
                        return;
                    }
                    MessageDispatcher.getInstance(mContext).appChangeReceiver(packageName, Integer.parseInt(EGContext.SNAP_SHOT_INSTALL),currentTime);
                }catch (Throwable t){
                }finally {
                    isSnapShotAddBroadCastHandled = false;
                }

            }
            if (PACKAGE_REMOVED.equals(intent.getAction())) {
                try {
                    if(FileUtils.isNeedWorkByLockFile(mContext,EGContext.FILES_SYNC_SNAP_DELETE_BROADCAST,EGContext.TIME_SYNC_DEFAULT,currentTime)){
                        FileUtils.setLockLastModifyTime(mContext,EGContext.FILES_SYNC_SNAP_DELETE_BROADCAST,currentTime);
                    }else {
                        return;
                    }
                    if(!isSnapShotDeleteBroadCastHandled){
                        isSnapShotDeleteBroadCastHandled = true;
                    }else {
                        return;
                    }
                    MessageDispatcher.getInstance(mContext).appChangeReceiver(packageName, Integer.parseInt(EGContext.SNAP_SHOT_UNINSTALL),currentTime);
                }catch (Throwable t){
                }finally {
                    isSnapShotDeleteBroadCastHandled = false;
                }
            }
            if (PACKAGE_REPLACED.equals(intent.getAction())) {
                try {
                    if(FileUtils.isNeedWorkByLockFile(mContext,EGContext.FILES_SYNC_SNAP_UPDATE_BROADCAST,EGContext.TIME_SYNC_DEFAULT,currentTime)){
                        FileUtils.setLockLastModifyTime(mContext,EGContext.FILES_SYNC_SNAP_UPDATE_BROADCAST,currentTime);
                    }else {
                        return;
                    }
                    if(!isSnapShotUpdateBroadCastHandled){
                        isSnapShotUpdateBroadCastHandled = true;
                    }else {
                        return;
                    }
                    MessageDispatcher.getInstance(mContext).appChangeReceiver(packageName, Integer.parseInt(EGContext.SNAP_SHOT_UPDATE),currentTime);
                }catch (Throwable t){
                }finally {
                    isSnapShotUpdateBroadCastHandled = false;
                }
            }
            if (SCREEN_ON.equals(intent.getAction())) {
                if(Build.VERSION.SDK_INT >= 24){
                    MessageDispatcher.getInstance(mContext).sendMessages();
                    return;
                }
                //设置开锁屏的flag 用于补数逻辑
                EGContext.SCREEN_ON = true;
                MessageDispatcher.getInstance(mContext).screenStatusHandle(true);
            }
            if (SCREEN_OFF.equals(intent.getAction())) {
                if(Build.VERSION.SDK_INT >= 24){
                    return;
                }
                EGContext.SCREEN_ON = false;
                MessageDispatcher.getInstance(mContext).screenStatusHandle(false);
            }
            if (BATTERY_CHANGED.equals(intent.getAction())) {
                try {
                    if(FileUtils.isNeedWorkByLockFile(mContext,EGContext.FILES_SYNC_BATTERY_BROADCAST,EGContext.TIME_SYNC_DEFAULT,currentTime)){
                        FileUtils.setLockLastModifyTime(mContext,EGContext.FILES_SYNC_BATTERY_BROADCAST,currentTime);
                    }else {
                        return;
                    }
                    if(!isBatteryBroadCastHandled){
                        isBatteryBroadCastHandled = true;
                    }else {
                        return;
                    }
                    DeviceImpl.getInstance(mContext).processBattery(intent);
                }catch (Throwable t){
                }finally {
                    isBatteryBroadCastHandled = false;
                }
            }
            if (BOOT_COMPLETED.equals(intent.getAction())) {
                try {
                    if(FileUtils.isNeedWorkByLockFile(mContext,EGContext.FILES_SYNC_BOOT_BROADCAST,EGContext.TIME_SYNC_DEFAULT,currentTime)){
                        FileUtils.setLockLastModifyTime(mContext,EGContext.FILES_SYNC_BOOT_BROADCAST,currentTime);
                    }else {
                        return;
                    }
                    if(!isBootBroadCastHandled){
                        isBootBroadCastHandled = true;
                    }else {
                        return;
                    }
                    MessageDispatcher.getInstance(mContext).startService();
                }catch (Throwable t){
                }finally {
                    isBootBroadCastHandled = false;
                }
            }
        }catch (Throwable t){
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t);
            }
        }
    }

}
