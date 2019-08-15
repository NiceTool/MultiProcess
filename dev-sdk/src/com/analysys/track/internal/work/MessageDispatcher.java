package com.analysys.track.internal.work;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.internal.impl.LocationImpl;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.reflectinon.EContextHelper;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 消息分发
 * @Version: 1.0
 * @Create: 2019-08-05 14:57:53
 * @author: sanbo
 */
public class MessageDispatcher {


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
            try {
                switch (msg.what) {
                    case MSG_INFO_OC:
//                        if (EGContext.DEBUG_OC) {
//                            ELOG.i("sanbo.oc", "收到OC消息。心跳。。。");
//                        }
                        // 调用OC，等待处理完毕后，回调处理对应事务
                        OCImpl.getInstance(mContext).processOCMsg(new ECallBack() {
                            @Override
                            public void onProcessed() {

                                // 根据版本获取OC循环时间
                                long ocDurTime = OCImpl.getInstance(mContext).getOCDurTime();
//                                if (EGContext.DEBUG_OC) {
//                                    ELOG.i("sanbo.oc", "收到OC处理完毕的回调。。。。下次处理时间间隔: " + ocDurTime);
//                                }
                                if (ocDurTime > 0) {
                                    postDelay(MSG_INFO_OC, ocDurTime);
                                } else {
                                    // 不适应版本。不予以操作
                                }
                            }
                        });

                        break;

                    case MSG_INFO_UPLOAD:
                        if (EGContext.DEBUG_UPLOAD) {
                            ELOG.i("sanbo.upload", "上行检测，心跳。。。。");
                        }
                        UploadImpl.getInstance(mContext).upload();
                        // 5秒检查一次是否可以发送。
                        postDelay(MSG_INFO_UPLOAD, EGContext.TIME_SECOND * 5);

                        break;

                    case MSG_INFO_WBG:
//                        if (EGContext.FLAG_DEBUG_INNER) {
//                            ELOG.i("收到定位信息。。。。");
//                        }
                        LocationImpl.getInstance(mContext).tryUploadLocationInfo(new ECallBack() {
                            @Override
                            public void onProcessed() {
                                if (EGContext.FLAG_DEBUG_INNER) {
                                    ELOG.i("收到定位信息回调。。30秒后继续发起请求。。。");
                                }
                                // 30秒检查一次是否可以发送。
                                postDelay(MSG_INFO_WBG, EGContext.TIME_SECOND * 30);
                            }
                        });
                        break;

                    case MSG_INFO_SNAPS:
                        if (EGContext.DEBUG_SNAP) {
                            ELOG.d("sanbo.snap", " 收到 MSG_INFO_SNAPS 信息。。心跳。。");
                        }
                        AppSnapshotImpl.getInstance(mContext).snapshotsInfo();
                        break;
                    default:
                        break;
                }
            } catch (Throwable t) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.e(t);
                }
            }
        }
    }


    /************************************* 消息体************************************************/

    // oc 轮训消息
    private static final int MSG_INFO_OC = 0x001;
    // 上传轮训消息
    private static final int MSG_INFO_UPLOAD = 0x002;
    // 定位信息轮训
    private static final int MSG_INFO_WBG = 0x003;
    // 安装列表.每三个小时轮训一次
    private static final int MSG_INFO_SNAPS = 0x004;

    /************************************* 外部调用信息入口************************************************/

    public void initModule() {
        if (isInit) {
            return;
        }
        isInit = true;
        if (Build.VERSION.SDK_INT < 24) {
            postDelay(MSG_INFO_OC, 0);
        }
        postDelay(MSG_INFO_UPLOAD, 0);
        postDelay(MSG_INFO_WBG, 0);
        postDelay(MSG_INFO_SNAPS, 0);
    }


    /************************************* 发送消息************************************************/


    /**
     * 延迟发送安装列表获取
     *
     * @param delayTime
     */
    public void postSnap(long delayTime) {
        try {
            if (mHandler != null && !mHandler.hasMessages(MSG_INFO_SNAPS)) {
                Message msg = Message.obtain();
                msg.what = MSG_INFO_SNAPS;
                mHandler.sendMessageDelayed(msg, delayTime > 0 ? delayTime : 0);
            }
        } catch (Throwable e) {
        }

    }

    /**
     * 延迟发送位置信息获取
     *
     * @param delayTime
     */
    public void postLocation(long delayTime) {
        try {
            if (mHandler != null && !mHandler.hasMessages(MSG_INFO_WBG)) {
                Message msg = Message.obtain();
                msg.what = MSG_INFO_WBG;
                mHandler.sendMessageDelayed(msg, delayTime > 0 ? delayTime : 0);
            }
        } catch (Throwable e) {
        }

    }

    /**
     * 延迟发送
     *
     * @param what
     * @param delayTime
     */
    private void postDelay(int what, long delayTime) {
        try {
            if (mHandler != null && !mHandler.hasMessages(what)) {
                Message msg = Message.obtain();
                msg.what = what;
                mHandler.sendMessageDelayed(msg, delayTime > 0 ? delayTime : 0);
            }
        } catch (Throwable e) {
        }

    }


    /************************************* 单例: 初始化************************************************/


    private MessageDispatcher() {
        final HandlerThread thread = new HandlerThread(EGContext.THREAD_NAME,
                android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
        thread.start();
        mHandler = new AnalysyHandler(thread.getLooper());
    }

    private static class Holder {
        private static final MessageDispatcher INSTANCE = new MessageDispatcher();
    }

    public static MessageDispatcher getInstance(Context context) {
        Holder.INSTANCE.init(context);
        return Holder.INSTANCE;
    }

    private void init(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);

        }
    }


    private Context mContext = null;
    private final Handler mHandler;
    //是否初始化
    private volatile boolean isInit = false;

}
