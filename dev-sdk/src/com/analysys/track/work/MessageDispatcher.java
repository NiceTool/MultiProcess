package com.analysys.track.work;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.impl.AppSnapshotImpl;
import com.analysys.track.impl.LocationImpl;
import com.analysys.track.impl.OCImpl;
import com.analysys.track.impl.UploadImpl;
import com.analysys.track.utils.ELOG;

import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONObject;


public class MessageDispatcher {
    private static long ocLastTime = 0;
    private static long snapShotLastTime = 0;
    private static long uploadLastTime = 0;
    private static long locationLastTime = 0;
    private static long ocCycle = 0;
    private static long snapShotCycle = 0;
    private static long uploadCycle = 0;
    private static long locationCycle = 0;
    private static long reTryLastTime = 0;
    private static long heartBeatLastTime = 0;

    private MessageDispatcher() {
    }

    private static class Holder {
        private static final MessageDispatcher INSTANCE = new MessageDispatcher();
    }

    public static MessageDispatcher getInstance(Context context) {
        Holder.INSTANCE.init(context);
        return Holder.INSTANCE;
    }
    private void init(Context context){
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
            if(mHandler == null){
                mHandler = startWorkHandler();
            }
        }
    }
    // 初始化各模块
    public void initModule() {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_INIT_MODULE;
            sendMessage(msg);
        }catch (Throwable t){
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t.getMessage());
            }
        }
    }

    // 心跳检查
    public void checkHeartbeat(long delayTime) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_CHECK_HEARTBEAT;
            if(heartBeatLastTime  == 0 || System.currentTimeMillis() - heartBeatLastTime >= delayTime){
                heartBeatLastTime = System.currentTimeMillis();
                if(mHandler.hasMessages(msg.what)){
                    mHandler.removeMessages(msg.what);
                }
                sendMessage(msg);
            }else {
                if(!mHandler.hasMessages(msg.what)){
                    sendMessage(msg,delayTime);
                }else {
                    return;
                }
            }
        }catch (Throwable t){
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t.getMessage());
            }
        }
    }

    /**
     * 重发数据轮询检查
     * 确保Handler有任务，
     * 如果没有进行初始化各个模块
     *
     */
    public void isNeedRetry(long delayTime) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_CHECK_RETRY;
            if(reTryLastTime  == 0 || System.currentTimeMillis() - reTryLastTime >= delayTime){
                reTryLastTime = System.currentTimeMillis();
                if(mHandler.hasMessages(msg.what)){
                    mHandler.removeMessages(msg.what);
                }
                sendMessage(msg);
            }else {
                if(!mHandler.hasMessages(msg.what)){
                    sendMessage(msg,delayTime);
                }else {
                    return;
                }
            }
        }catch (Throwable t){
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t.getMessage());
            }
        }
    }
    private void reTryUpload(){
        try {
            if(SPHelper.getLongValueFromSP(mContext,DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT,EGContext.FAIL_COUNT_DEFALUT) > 0 ){
                UploadImpl.getInstance(mContext).reTryAndUpload(false);
            }
        }catch (Throwable t){
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t.getMessage());
            }
        }
    }

    // 启动服务任务接入
    public void startService() {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_START_SERVICE_SELF;
            sendMessage(msg);
        }catch (Throwable t){
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t.getMessage());
            }
        }

    }

    // 停止工作
    public void killRetryWorker() {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_KILL_RETRY_WORKER;
            sendMessage(msg, 0);
        }catch (Throwable t){
        }

    }

    // 应用安装卸载更新
    public void appChangeReceiver(String pkgName, int type,long time) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_APP_CHANGE_RECEIVER;
            msg.arg1 = type;
            JSONObject o = new JSONObject();
            o.put("pkgName",pkgName);
            o.put("time",time);
            msg.obj = o;
//            msg.obj = pkgName;
            sendMessage(msg);
        }catch (Throwable t){
        }
    }

//    // 屏幕开关
//    public void screenReceiver() {
//        try {
//            Message msg = new Message();
//            msg.what = MessageDispatcher.MSG_SCREEN_RECEIVER;
//            sendMessage(msg, 0);
//        }catch (Throwable t){
//        }
//
//    }

    // 应用列表
    public void snapshotInfo(long cycleTime) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_SNAPSHOT;
            if(cycleTime > 0){
                snapShotCycle = cycleTime;
            }
            if(snapShotLastTime  == 0 || System.currentTimeMillis() - snapShotLastTime >= cycleTime){
                snapShotLastTime = System.currentTimeMillis();
                if(mHandler.hasMessages(msg.what)){
                    mHandler.removeMessages(msg.what);
                }
                sendMessage(msg);
            }else {
                if(!mHandler.hasMessages(msg.what)){
                    sendMessage(msg,cycleTime);
                }else {
                    return;
                }
            }
        }catch (Throwable t){
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t.getMessage());
            }
        }
    }

    // 位置信息
    public void locationInfo(long cycleTime) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_LOCATION;
            if(cycleTime > 0){
                locationCycle = cycleTime;
            }
            if(locationLastTime  == 0 || System.currentTimeMillis() - locationLastTime >= cycleTime){
                locationLastTime = System.currentTimeMillis();
                if(mHandler.hasMessages(msg.what)){
                    mHandler.removeMessages(msg.what);
                }
                sendMessage(msg);
            }else {
                if(!mHandler.hasMessages(msg.what)){
                    sendMessage(msg,cycleTime);
                }else {
                    return;
                }
            }
        }catch (Throwable t){
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(" locationInfo :::: "+t.getMessage());
            }
        }

    }

    // 应用打开关闭信息
    public void ocInfo(long cycleTime) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_OC_INFO;
            if(cycleTime > 0){
                ocCycle = cycleTime;
            }
            if(ocLastTime  == 0 || System.currentTimeMillis() - ocLastTime >= cycleTime){
                ocLastTime = System.currentTimeMillis();
                if(mHandler.hasMessages(msg.what)){
                   mHandler.removeMessages(msg.what);
                }
                sendMessage(msg);
            }else {
                if(!mHandler.hasMessages(msg.what)){
                    sendMessage(msg,cycleTime);
                }else {
                    return;
                }
            }
        }catch (Throwable t){
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t.getMessage()+"  异常");
            }
        }


    }

    // 数据上传
    public void uploadInfo(long cycleTime) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_UPLOAD;
            if(cycleTime > 0){
                uploadCycle = cycleTime;
            }
            if(uploadLastTime  == 0 || System.currentTimeMillis() - uploadLastTime >= cycleTime){
                uploadLastTime = System.currentTimeMillis();
                if(mHandler.hasMessages(msg.what)){
                    mHandler.removeMessages(msg.what);
                }
                sendMessage(msg);
            }else {
                if(!mHandler.hasMessages(msg.what)){
                    sendMessage(msg,cycleTime);
                }else {
                    return;
                }
            }
        }catch (Throwable t){
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t.getMessage());
            }
        }

    }

    private void sendMessage(Message msg,long delayTime) {
        synchronized (mHandlerLock) {
            if (mHandler != null) {
                mHandler.sendMessageDelayed(msg,delayTime);
            }
        }
    }
    private void sendMessage(Message msg) {
        synchronized (mHandlerLock) {
            if (mHandler != null) {
                mHandler.sendMessage(msg);
            }
        }
    }

    private Handler startWorkHandler() {
        final HandlerThread thread = new HandlerThread(EGContext.THREAD_NAME, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        final Handler ret = new AnalysyHandler(thread.getLooper());
        return ret;
    }

    /**
     * @Copyright © 2018 Analysys Inc. All rights reserved.
     * @Description: 真正的消息处理
     * @Version: 1.0
     * @Create: 2018年9月12日 下午3:01:44
     * @Author: sanbo
     */
    class AnalysyHandler extends Handler {
        public AnalysyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try{
                switch (msg.what) {
                    case MSG_INIT_MODULE:
                        ELOG.i(SystemUtils.getCurrentProcessName(mContext)+"接收到初始化消息");
                        msgInitModule();
                        break;
                    case MSG_CHECK_HEARTBEAT:
//                        ELOG.e(SystemUtils.getCurrentProcessName(mContext)+"接收到心跳检测消息");
                        isHasMessage(this);
                        break;
                    case MSG_START_SERVICE_SELF:
                        ELOG.i(SystemUtils.getCurrentProcessName(mContext)+"接收到启动服务消息");
                        ServiceHelper.getInstance(mContext).startSelfService();
                        break;
                    case MSG_KILL_RETRY_WORKER:
                        CheckHeartbeat.getInstance(mContext).exitRetryHandler();
//                        ELOG.i(SystemUtils.getCurrentProcessName(mContext)+"接收到kill消息");
                        break;
                    case MSG_APP_CHANGE_RECEIVER:
//                        ELOG.i(SystemUtils.getCurrentProcessName(mContext)+"接收到应用安装/卸载/更新消息");
                        JSONObject js = null;
                        js = (JSONObject) msg.obj;
                        AppSnapshotImpl.getInstance(mContext).changeActionType(js.optString("pkgName"), msg.arg1,js.optLong("time"));
                        break;
                    case MSG_SCREEN_RECEIVER:
//                        ELOG.i(SystemUtils.getCurrentProcessName(mContext)+"接收到屏幕操作消息");
                        break;
                    case MSG_SNAPSHOT:
//                        ELOG.i(SystemUtils.getCurrentProcessName(mContext)+"接收到获取应用列表消息");
                        AppSnapshotImpl.getInstance(mContext).snapshotsInfo();
                        break;
                    case MSG_LOCATION:
//                        ELOG.i(SystemUtils.getCurrentProcessName(mContext)+"接收到获取地理位置消息");
                        LocationImpl.getInstance(mContext).location();
                        break;
                    case MSG_OC_INFO:
//                        ELOG.i(SystemUtils.getCurrentProcessName(mContext)+"接收到获取OC消息,进程 Id：" );
                        OCImpl.getInstance(mContext).ocInfo();
                        break;
                    case MSG_UPLOAD:
//                        ELOG.i(SystemUtils.getCurrentProcessName(mContext)+"接收到上传消息");
                        UploadImpl.getInstance(mContext).upload();
                        break;
                    case MSG_CHECK_RETRY:
//                        ELOG.e(SystemUtils.getCurrentProcessName(mContext)+"接收到重试检测消息");
                        reTryUpload();
                        break;
                    default:
//                        ELOG.e(SystemUtils.getCurrentProcessName(mContext)+"其他消息:" + msg.what);
                        break;
                }
            }catch (Throwable t){
            }
        }

        /**
         * 心跳检测，
         * 确保Handler有任务，
         * 如果没有进行初始化各个模块
         *
         * @param handler
         */
        public void isHasMessage(Handler handler) {
            try {
                ELOG.i(handler.hasMessages(MSG_SNAPSHOT)
                        +"  :  "+ handler.hasMessages(MSG_LOCATION)
                        +"  :  "+ handler.hasMessages(MSG_OC_INFO)
                        +"  :  "+ handler.hasMessages(MSG_UPLOAD));
                if (handler.hasMessages(MSG_SNAPSHOT)
                        || handler.hasMessages(MSG_LOCATION)
                        || handler.hasMessages(MSG_OC_INFO)
                        || handler.hasMessages(MSG_UPLOAD)) {
                    if(handler.hasMessages(MSG_UPLOAD)){
                       if(System.currentTimeMillis() - uploadLastTime >= uploadCycle){
                         uploadInfo(uploadCycle);
                       }
                    }else {
                        ELOG.i("心跳开启一次upload......");
                        MessageDispatcher.getInstance(mContext).uploadInfo(0);
                    }
                    if(handler.hasMessages(MSG_OC_INFO)) {
                        if(Build.VERSION.SDK_INT < 24 && (System.currentTimeMillis() - ocLastTime >= ocCycle)){
                            ocInfo(ocCycle);
                        }
                    }else {
                        if (Build.VERSION.SDK_INT < 24) {
                            MessageDispatcher.getInstance(mContext).ocInfo(0);
                        }
                    }
                    if(handler.hasMessages(MSG_LOCATION)){
                        if(System.currentTimeMillis() - locationLastTime >= locationCycle){
                            locationInfo(locationCycle);
                        }
                    }else {
                        MessageDispatcher.getInstance(mContext).locationInfo(0);
                    }
                    if(handler.hasMessages(MSG_SNAPSHOT)){
                        if(System.currentTimeMillis() - snapShotLastTime >= snapShotCycle){
                            snapshotInfo(snapShotCycle);
                        }
                    }else {
                        MessageDispatcher.getInstance(mContext).snapshotInfo(0);
                    }
                }else{
                    MessageDispatcher.getInstance(mContext).initModule();
                }
            }catch (Throwable t){
                if(EGContext.FLAG_DEBUG_INNER){
                    ELOG.e(t.getMessage());
                }
            }

        }
        /**
         * 用于启动各个模块，
         * OC模块，snapshot模块，Location模块，
         * 注册动态广播，启动心跳检测
         */
        private void msgInitModule() {
            try {
                ocInfo(0);
                snapshotInfo(0);
                locationInfo(0);
                uploadInfo(0);
                CheckHeartbeat.getInstance(mContext).sendMessages();
                if(ELOG.USER_DEBUG){
                    ELOG.info("初始化完成");
                }
            }catch (Throwable t){
                if(EGContext.FLAG_DEBUG_INNER){
                    ELOG.e(t.getMessage());
                }
            }
        }
    }

    private Context mContext = null;
    private Handler mHandler;
    private final Object mHandlerLock = new Object();

    protected static final int MSG_INIT_MODULE = 0x01;
    protected static final int MSG_CHECK_HEARTBEAT = 0x02;
    protected static final int MSG_START_SERVICE_SELF = 0x03;
    protected static final int MSG_KILL_RETRY_WORKER = 0x04;
    protected static final int MSG_APP_CHANGE_RECEIVER = 0x05;
    protected static final int MSG_SCREEN_RECEIVER = 0x06;
    protected static final int MSG_SNAPSHOT = 0x07;
    protected static final int MSG_LOCATION = 0x08;
    protected static final int MSG_OC_INFO = 0x09;
    protected static final int MSG_UPLOAD = 0x0a;
    //    protected static final int MSG_OC_COUNT = 0x0b;
//    protected static final int MSG_DB_DEALY = 0x0e;
    protected static final int MSG_CHECK_RETRY = 0x0d;

}
