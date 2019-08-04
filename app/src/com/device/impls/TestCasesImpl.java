package com.device.impls;

import android.content.Context;

import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.internal.net.UploadImpl;
import com.device.utils.AssetsHelper;
import com.device.utils.EL;
import com.device.utils.MyLooper;
import com.device.utils.ProcessUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 测试case实现类
 * @Version: 1.0
 * @Create: 2019-07-27 14:19:53
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class TestCasesImpl {

    public static void runCase(Context context, int caseNum) {

        EL.i(ProcessUtils.getCurrentProcessName(context) + "--- you click  btnCase" + caseNum);
        switch (caseNum) {
            case 1:
                runCase1(context);
                break;
            case 2:
                runCase2(context);
                break;
            case 3:
                runCase3(context);
                break;
            case 4:
                runCase4(context);
                break;
            case 5:
                runCase5(context);
                break;
            case 6:
                runCase6(context);
                break;
            case 7:
                runCase7(context);
                break;
            case 8:
                runCase8(context);
                break;
            case 9:
                runCase9(context);
                break;
            default:
                break;
        }
    }


    // 1. 测试发起请求，接收策略
    public static void runCase1(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    PolicyImpl.getInstance(context).clearSP();
                    UploadImpl.getInstance(context).doUploadImpl();

                } catch (Throwable e) {
                    EL.i(e);
                }
            }
        });
    }

    // 2. 测试接收并处理策略
    public static void runCase2(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    testSavePolicy(context);
                } catch (Throwable e) {
                    EL.i(e);
                }
            }
        });
    }


    // 3. 接收新策略
    public static void runCase3(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    testReceiverPolocy(context);
                } catch (Throwable e) {
                    EL.i(e);
                }
            }
        });
    }


    // OC测试
    private static void runCase4(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                OCImpl.getInstance(context).processOC();
            }
        });

    }

    //安装列表获取
    private static void runCase5(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {

                List<JSONObject> list = AppSnapshotImpl.getInstance(context).getAppDebugStatus();
                EL.i("getAppDebugStatus:" + list);
            }
        });
    }

    private static void runCase6(Context context) {
    }

    private static void runCase7(Context context) {
    }

    private static void runCase8(Context context) {
    }

    private static void runCase9(Context context) {
    }

    /**
     * 测试解析策略
     *
     * @param context
     * @throws JSONException
     */
    private static void testSavePolicy(Context context) throws JSONException {

        String policyBody = AssetsHelper.getFromAssetsToString(context, "policyBody.txt");
        PolicyImpl.getInstance(context).clearSP();
        JSONObject obj = new JSONObject(policyBody);
        PolicyImpl.getInstance(context).saveRespParams(obj);
    }

    /**
     * 测试接收到策略解析
     *
     * @param context
     */
    private static void testReceiverPolocy(Context context) {
        EL.i("testReceiverPolocy。。。。");

        String policy = AssetsHelper.getFromAssetsToString(context, "patchPolicy.txt");
        EL.i("testReceiverPolocy。。。。policy：" + policy);
        UploadImpl.getInstance(context).handleUpload("http://192.168.220.167:8089", policy);
        EL.i("testReceiverPolocy。。。。process over");
    }

}
