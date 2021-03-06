package com.device.tripartite;


import android.content.Context;
import android.text.TextUtils;

import com.device.utils.DemoClazzUtils;

import org.json.JSONArray;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: 所有的三方SDK的调用
 * @Version: 1.0
 * @Create: 2020/3/12 17:22
 * @author: sanbo
 */
public class Abu {
    /**
     * 初始化统计
     *
     * @param context
     */
    public static void initAnalysys(Context context) {
        initEg(context);
        initUmeng(context);
    }

    // 初始化接口:第二个参数填写您在平台申请的appKey,第三个参数填写
    private static void initEg(Context context) {

        try {

            String appkey = DemoClazzUtils.getStringField("com.analysys.track.BuildConfig", "DEMO_APPKEY");
            if (TextUtils.isEmpty(appkey)) {
                appkey = DemoClazzUtils.getStringField("com.device.BuildConfig", "DEMO_APPKEY");
            }
            if (TextUtils.isEmpty(appkey)) {
                appkey = "test_appkey";
            }
            DemoClazzUtils.invokeStaticMethod("com.analysys.track.AnalysysTracker", "init",
                    new Class[]{Context.class, String.class, String.class}, new Object[]{context, appkey, "WanDouJia"});
        } catch (Throwable e) {
        }
    }

    //init umeng
    private static void initUmeng(Context context) {

//        MobclickAgent.setSessionContinueMillis(10);
//        MobclickAgent.setCatchUncaughtExceptions(true);
////        UMConfigure.setProcessEvent(true);
//        UMConfigure.setEncryptEnabled(true);
//        UMConfigure.setLogEnabled(true);
//        UMConfigure.init(context, "5b4c140cf43e4822b3000077", "track-demo-dev", UMConfigure.DEVICE_TYPE_PHONE, "99108ea07f30c2afcafc1c5248576bc5");

        DemoClazzUtils.invokeStaticMethod("com.umeng.analytics.MobclickAgent", "setSessionContinueMillis",
                new Class[]{long.class}, new Object[]{10});
        DemoClazzUtils.invokeStaticMethod("com.umeng.analytics.MobclickAgent", "setCatchUncaughtExceptions",
                new Class[]{boolean.class}, new Object[]{true});
        DemoClazzUtils.invokeStaticMethod("com.umeng.commonsdk.UMConfigure", "setEncryptEnabled",
                new Class[]{boolean.class}, new Object[]{true});
        DemoClazzUtils.invokeStaticMethod("com.umeng.commonsdk.UMConfigure", "setLogEnabled",
                new Class[]{boolean.class}, new Object[]{true});
        //init(Context var0, String var1, String var2, int var3, String var4)
        DemoClazzUtils.invokeStaticMethod("com.umeng.commonsdk.UMConfigure", "init",
                new Class[]{Context.class, String.class, String.class, int.class, String.class}
                , new Object[]{context, "5b4c140cf43e4822b3000077", "track-demo-dev", 1, "99108ea07f30c2afcafc1c5248576bc5"}
        );
    }

    /**
     * 初始化bugly。 track-sdk-demo
     *
     * @param context
     */
    public static void initBugly(Context context) {
//        Bugly.init(context, "8b5379e3bc", false);
        //init(Context context, String appId, boolean isDebug)
        DemoClazzUtils.invokeStaticMethod("com.tencent.bugly.Bugly", "init",
                new Class[]{Context.class, String.class, boolean.class},
                new Object[]{context, "8b5379e3bc", false}
        );

    }


    public static void onResume(Context ctx, String pn) {
//        MobclickAgent.onResume(ctx);
//        MobclickAgent.onPageStart(pn);
        DemoClazzUtils.invokeStaticMethod("com.umeng.analytics.MobclickAgent", "onResume",
                new Class[]{Context.class}, new Object[]{ctx});
        DemoClazzUtils.invokeStaticMethod("com.umeng.analytics.MobclickAgent", "onPageStart",
                new Class[]{String.class}, new Object[]{pn});

    }


    public static void onPause(Context ctx, String pn) {
//        MobclickAgent.onPause(ctx);
//        MobclickAgent.onPageEnd(pn);
        DemoClazzUtils.invokeStaticMethod("com.umeng.analytics.MobclickAgent", "onPause",
                new Class[]{Context.class}, new Object[]{ctx});
        DemoClazzUtils.invokeStaticMethod("com.umeng.analytics.MobclickAgent", "onPageEnd",
                new Class[]{String.class}, new Object[]{pn});
    }

    public static void onEvent(Context ctx, String eventName) {
//        MobclickAgent.onEvent(ctx, eventName);
        DemoClazzUtils.invokeStaticMethod("com.umeng.analytics.MobclickAgent", "onEvent",
                new Class[]{Context.class, String.class}, new Object[]{ctx, eventName});
    }

    public static JSONArray getUSMInfo(Context ctx, long begin, long end) {
        return (JSONArray) DemoClazzUtils.invokeStaticMethod("com.analysys.track.internal.impl.usm.USMImpl", "getUSMInfo",
                new Class[]{Context.class, long.class, long.class}, new Object[]{ctx, begin, end});
    }


}
