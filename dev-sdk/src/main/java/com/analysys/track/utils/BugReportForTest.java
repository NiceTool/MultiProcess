package com.analysys.track.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.reflectinon.ClazzUtils;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 注意:这个类是内部测试用的,这个类帮助把SDK出现的一些错误,报到Bugly上去,正常发包情况下是不可以启用的,会误报错误到对接方的bugly上
 * @Version: 1.0
 * @Create: 2019-11-06 11:34:15
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class BugReportForTest {

    public static void commitError(Throwable throwable) {
        commitError(null, throwable);
    }

    public static void commitError(String tag, Throwable throwable) {
        try {
            if (BuildConfig.logcat) {
                if (!TextUtils.isEmpty(tag)) {
                    //使用log的原因是防止 ELOG 内部异常出现循环打印
                    Log.e(tag, Log.getStackTraceString(throwable));
                } else {
                    Log.e("analysys", Log.getStackTraceString(throwable));
                }
            }
            if (BuildConfig.ENABLE_BUG_REPORT) {
                reportToBugly(throwable);
                reportToUmeng(throwable);
            }
        } catch (Throwable e) {

        }
    }

    private static void reportToUmeng(Throwable throwable) {
        try {
            ClazzUtils.invokeStaticMethod("com.umeng.analytics.MobclickAgent", "reportError", new Class[]{Context.class, Throwable.class}, new Object[]{});
        } catch (Throwable e) {
        }
    }

    private static void reportToBugly(Throwable throwable) throws ClassNotFoundException {
        try {
            setTag(138534);
            postException(throwable);
        } catch (Throwable e) {
        }
    }

    private static void postException(Throwable throwable) {
        try {
            ClazzUtils.invokeStaticMethod("com.tencent.bugly.crashreport.CrashReport", "postCatchedException",
                    new Class[]{Throwable.class}, new Object[]{throwable}
            );
        } catch (Throwable e) {

        }
    }

    private static void setTag(int tag) {
        try {
            ClazzUtils.invokeStaticMethod("com.tencent.bugly.crashreport.CrashReport", "setUserSceneTag",
                    new Class[]{Context.class, int.class}, new Object[]{EContextHelper.getContext(), tag}
            );
        } catch (Throwable e) {

        }
    }
}
