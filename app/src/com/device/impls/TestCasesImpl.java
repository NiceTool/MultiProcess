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
import com.umeng.analytics.MobclickAgent;

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

    /**
     * 接收到方法
     *
     * @param context
     * @param caseNum
     */
    public static void runCase(Context context, int caseNum) {
        MobclickAgent.onEvent(context, "[" + ProcessUtils.getCurrentProcessName(context) + "]测试-case" + caseNum);

        EL.d(ProcessUtils.getCurrentProcessName(context) + "--- you click  btnCase" + caseNum);

        //多进程测试
        if (caseNum < 1000) {
            MultiProcessWorker.postMultiMessages(context, caseNum);
        } else {
            switch (caseNum) {
                case 1001:
                    runCaseP1(context);
                    break;
                case 1002:
                    runCaseP2(context);
                    break;
                case 1003:
                    runCaseP3(context);
                    break;
                case 1004:
                    runCaseP4(context);
                    break;
                case 1005:
                    runCaseP5(context);
                    break;
                case 1006:
                    runCaseP6(context);
                    break;
                case 1007:
                    runCaseP7(context);
                    break;
                case 1008:
                    runCaseP8(context);
                    break;
                default:
                    break;
            }
        }

    }


    /**************************************************************************************/
    /********************************** 功能测试区************************************/
    /**************************************************************************************/

    // 1. 测试发起请求，接收策略
    private static void runCaseP1(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EL.i("=================== 测试发起请求，接收策略===============");
                    PolicyImpl.getInstance(context).clear();
                    UploadImpl.getInstance(context).doUploadImpl();

                } catch (Throwable e) {
                    EL.i(e);
                }
            }
        });
    }

    // 2. 测试接收并处理策略
    private static void runCaseP2(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EL.i("=================== 测试接收并处理策略===============");
                    testSavePolicy(context);
                } catch (Throwable e) {
                    EL.i(e);
                }
            }
        });
    }

    // 3. OC测试
    private static void runCaseP3(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                EL.i("=================== OC测试 ===============");
                OCImpl.getInstance(context).processOC();
            }
        });
    }

    // 4.安装列表调试状态获取测试
    private static void runCaseP4(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                EL.i("=================== 安装列表调试状态获取测试 ===============");

                List<JSONObject> list = AppSnapshotImpl.getInstance(context).getAppDebugStatus();
                EL.i("列表:" + list);
            }
        });
    }

    // 5. 测试保存文件到本地,忽略调试设备状态加载
    private static void runCaseP5(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                EL.i("=================== 保存文件到本地,忽略调试设备状态直接加载 ===============");
                try {
                    JSONObject obj = new JSONObject(AssetsHelper.getFromAssetsToString(context, "policy_body.txt"));
                    JSONObject patch = obj.optJSONObject("patch");
                    String version = patch.optString("version");
                    String data = patch.optString("data");
                    EL.i("testParserPolicyA------version: " + version);
                    EL.i("testParserPolicyA------data: " + data);
                    PolicyImpl.getInstance(context).saveFileAndLoad(version, data);
                } catch (Throwable e) {
                    EL.e(e);
                }

            }
        });
    }

    //
    private static void runCaseP6(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private static void runCaseP7(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private static void runCaseP8(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    /********************************** 功能实现区 ************************************/

    /**
     * 测试解析策略 部分内容
     *
     * @param context
     * @throws JSONException
     */
    private static void testSavePolicy(Context context) throws JSONException {
        PolicyImpl.getInstance(context).clear();
        JSONObject obj = new JSONObject(AssetsHelper.getFromAssetsToString(context, "policy_body.txt"));
        PolicyImpl.getInstance(context).saveRespParams(obj);
    }


}
