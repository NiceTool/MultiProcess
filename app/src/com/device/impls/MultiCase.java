package com.device.impls;


import android.content.Context;

import com.analysys.track.internal.impl.LocationImpl;
import com.analysys.track.internal.net.DataPackaging;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.service.AnalysysAccessibilityService;
import com.analysys.track.service.AnalysysJobService;
import com.analysys.track.service.AnalysysService;
import com.analysys.track.utils.AccessibilityHelper;
import com.analysys.track.utils.AndroidManifestHelper;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.reflectinon.DevStatusChecker;
import com.analysys.track.utils.reflectinon.DoubleCardSupport;
import com.device.utils.EL;

import java.lang.reflect.Method;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 多进程case类
 * @Version: 1.0
 * @Create: 2019-07-30 18:23:59
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class MultiCase {

    /**
     * 运行具体的case
     *
     * @param context
     * @param caseNum
     */
    public static void runCase(Context context, int caseNum) {

//        EL.d("--- MultiCase  you click  btnCase" + caseNum);
        try {
            Class<?> testCase = MultiCase.class;
            Method runCaseA = testCase.getDeclaredMethod("runCase" + caseNum, Context.class);
            runCaseA.invoke(null, context);
        } catch (Throwable e) {
            EL.v(e);
        }

    }

    /**
     * 多进程上传测试，并发强锁，测试是否可以上传成功
     *
     * @param context
     */
    public static void runCase1(final Context context) {
        EL.i("----上传测试----");
        UploadImpl.getInstance(context).upload();
    }

    /**
     * 多进程开关屏处理测试
     *
     * @param context
     */
    public static void runCase2(final Context context) {
//        EL.i("-----多进程开关屏处理测试----处理打开屏幕");
//        MessageDispatcher.getInstance(context).processScreenOnOff(true);
//        EL.i("-----多进程开关屏处理测试----处理关闭屏幕");
//        MessageDispatcher.getInstance(context).processScreenOnOff(false);
    }

    /**
     * 多进程服务声明探测
     *
     * @param context
     */
    public static void runCase3(final Context context) {

        StringBuilder sb = new StringBuilder();
        sb
                .append("\n")
                .append("=================== ")
                .append("服务声明检测 ")
                .append(" ===================")
                .append("\n")
                .append("声明 AnalysysService 结果")
                .append(AndroidManifestHelper.isServiceDefineInManifest(context, AnalysysService.class))
                .append("\n")
                .append("声明 AnalysysAccessibilityService 结果")
                .append(AccessibilityHelper.isAccessibilitySettingsOn(context, AnalysysAccessibilityService.class))
                .append("\n")
                .append("声明 AnalysysJobService 结果")
                .append(AndroidManifestHelper.isJobServiceDefineInManifest(context, AnalysysJobService.class))
        ;
        EL.i(sb.toString());
    }

    /**
     * 测试思想： 同一个文件1秒一次抢锁操作
     *
     * @param context
     */
    public static void runCase4(final Context context) {

        StringBuilder sb = new StringBuilder();
        sb
                .append("\n")
                .append("=================== ")
                .append("多进程测试 100 次 ")
                .append(" ===================")
        ;
        EL.i(sb.toString());
        for (int i = 0; i < 100; i++) {
            try {
                long now = System.currentTimeMillis();
                boolean is = MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, "test", 1000, now);
                if (is) {
                    EL.i("----" + (i + 1) + "----多进程测试文件:" + is);
                    Thread.sleep(200);
                    MultiProcessChecker.getInstance().setLockLastModifyTime(context, "test", System.currentTimeMillis());
                } else {
                    EL.d("-----" + (i + 1) + "----多进程测试文件:" + is);
                }
                Thread.sleep(50);
            } catch (Throwable e) {
                EL.i(e);
            }
        }
    }

    public static void runCase5(final Context context) {

        StringBuilder sb = new StringBuilder();
        sb
                .append("\n")
                .append("=================== ")
                .append("LocationImpl 位置信息测试 ")
                .append(" ===================")
        ;
        EL.i(sb.toString());
        LocationImpl.getInstance(context).tryUploadLocationInfo(null);
    }

    public static void runCase6(final Context context) {

        StringBuilder sb = new StringBuilder();
        sb
                .append("\n")
                .append("=================== ")
                .append("测试获取设备信息")
                .append(" ===================")
                .append("\n")
                .append("设备信息： ")
                .append(DataPackaging.getInstance().getDevInfo(context).toString())
        ;
        EL.i(sb.toString());
    }

    // 多卡获取信息
    public static void runCase7(final Context context) {

        StringBuilder sb = new StringBuilder();
        sb
                .append("\n")
                .append("=================== ")
                .append("测试多卡获取信息")
                .append(" ===================")
                .append("\n")
                .append("多卡IMEI： ")
                .append(DoubleCardSupport.getInstance().getIMEIS(context))
                .append("\n")
                .append("多卡IMSI： ")
                .append(DoubleCardSupport.getInstance().getIMSIS(context))
        ;
        EL.i(sb.toString());
    }

    public static void runCase8(final Context context) {

        StringBuilder sb = new StringBuilder();
        sb
                .append("\n")
                .append("=================== ")
                .append("测试调试设备")
                .append(" ===================")
                .append("\n")
                .append("调试设备： ")
                .append(DevStatusChecker.getInstance().isDebugDevice(context))
                .append("\n")
                .append("是否有解锁密码:")
                .append(DevStatusChecker.getInstance().isLockP(context))
        ;
        EL.i(sb.toString());
    }


    public static void runCase9(final Context context) {
    }

    public static void runCase10(final Context context) {
    }

    public static void runCase11(final Context context) {
    }

    public static void runCase12(final Context context) {
    }

    public static void runCase13(final Context context) {
    }

    public static void runCase14(final Context context) {
    }

    public static void runCase15(final Context context) {
    }

    public static void runCase16(final Context context) {
    }

    public static void runCase17(final Context context) {
    }

    public static void runCase18(final Context context) {
    }

}
