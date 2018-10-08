package com.analysys.dev.internal.work;

import com.analysys.dev.internal.utils.EContextHelper;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 消息分发包装类和消息分发Handler
 * @Version: 1.0
 * @Create: 2018年9月12日 下午3:01:58
 * @Author: sanbo
 */
public class MessageDispatcher {

    private MessageDispatcher() {
        mHandler = startWorkHandler();
    }

    private static class Holder {
        private static MessageDispatcher instance = new MessageDispatcher();
    }

    public static MessageDispatcher getInstance(Context context) {
        if (Holder.instance.mContext == null) {
            if (context != null) {
                Holder.instance.mContext = context;
            } else {
                Holder.instance.mContext = EContextHelper.getContext();
            }
        }
        return Holder.instance;
    }

    protected boolean testIsDead() {
        synchronized (mHandlerLock) {
            return mHandler == null;
        }
    }

    protected void sendMessage(Message msg, int delay) {
        synchronized (mHandlerLock) {
            if (mHandler != null) {
                if (delay > 0) {
                    mHandler.sendMessageDelayed(msg, delay);
                } else {
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    private Handler startWorkHandler() {
        final HandlerThread thread = new HandlerThread("com.eguan", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        final Handler ret = new AnalysyHandler(thread.getLooper());
        return ret;
    }

    private void exitHandler() {
        synchronized (mHandlerLock) {
            mHandler = null;
            Looper.myLooper().quit();
        }
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

            switch (msg.what) {
                case MSG_KILL_WORKER:
                    exitHandler();
                    break;
                case MSG_START_SERVICE_SELF:
                    ServiceHelper.getInstance(mContext).startSelfService();
                    break;
                default:
                    break;
            }
        }
    }

    private Context mContext = null;
    private Handler mHandler;
    private final Object mHandlerLock = new Object();
    protected static final int MSG_KILL_WORKER = 0x01;
    protected static final int MSG_START_SERVICE_SELF = 0x02;
    protected static final int MSG_WORK = 0x03;
}
