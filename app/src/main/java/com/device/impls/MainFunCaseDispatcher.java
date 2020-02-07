package com.device.impls;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.analysys.track.db.TableProcess;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.internal.impl.DeviceImpl;
import com.analysys.track.internal.impl.LocationImpl;
import com.analysys.track.internal.impl.net.NetImpl;
import com.analysys.track.internal.impl.net.NetInfo;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.impl.usm.USMUtils;
import com.analysys.track.internal.model.BatteryModuleNameInfo;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.reflectinon.DevStatusChecker;
import com.analysys.track.utils.reflectinon.DoubleCardSupport;
import com.analysys.track.utils.reflectinon.PatchHelper;
import com.analysys.track.utils.sp.SPHelper;
import com.device.impls.cases.PolicTestY;
import com.device.utils.AssetsHelper;
import com.device.utils.EContextHelper;
import com.device.utils.EL;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 单进程功能测试类
 * @Version: 1.0
 * @Create: 2019-07-27 14:19:53
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class MainFunCaseDispatcher {

    /**
     * 接收到方法
     *
     * @param context
     * @param x
     */
    public static void runCase(final Context context, final String x) {
        try {
            EL.d("--- you click  btnCase" + x);
            Method runCaseA = MainFunCaseDispatcher.class.getDeclaredMethod("runCaseP" + x, Context.class);
            runCaseA.invoke(null, context);
        } catch (Throwable e) {
            EL.e(e);
        }

    }


    // 1. 测试发起请求，接收策略
    private static void runCaseP1(final Context context) {
        try {
            EL.i("=================== 测试发起请求，接收策略===============");

            SPHelper.setIntValue2SP(context, EGContext.FAILEDNUMBER, 0);
            PolicyImpl.getInstance(context).clear();
            UploadImpl.getInstance(context).doUploadImpl();
        } catch (Throwable e) {
            EL.i(e);
        }

    }

    // 2. 测试接收并处理策略
    private static void runCaseP2(final Context context) {
        try {
            EL.i("=================== 测试接收并处理策略===============");
            PolicTestY.testSavePolicy(context);
        } catch (Throwable e) {
            EL.i(e);
        }
    }

    // 3. OC测试
    private static void runCaseP3(final Context context) {
        EL.i("=================== OC测试 ===============");
        OCImpl.getInstance(context).processOC();
    }

    // 4.安装列表调试状态获取测试
    private static void runCaseP4(final Context context) {
        EL.i("=================== 安装列表调试状态获取测试 ===============");

        List<JSONObject> list = AppSnapshotImpl.getInstance(context).getAppDebugStatus();
        EL.i("列表:" + list);
    }

    // 5. 测试保存文件到本地,忽略调试设备状态加载
    private static void runCaseP5(final Context context) {
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

    // 6. 根据手机APP情况，随机抽5个进行OC逻辑验证
    private static void runCaseP6(final Context context) {
        EL.i("=================== 根据手机APP情况，随机抽5个进行OC逻辑验证 ===============");

        // 获取安装列表
        List<JSONObject> list = AppSnapshotImpl.getInstance(context).getAppDebugStatus();

        //获取有界面的安装列表
        PackageManager pm = context.getPackageManager();
        List<String> ll = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {

            JSONObject o = list.get(i);

            if (o != null && o.has(EGContext.TEXT_DEBUG_APP)) {
                String pkg = o.optString(EGContext.TEXT_DEBUG_APP);
                if (SystemUtils.hasLaunchIntentForPackage(pm, pkg) && !ll.contains(pkg)) {
                    ll.add(pkg);
                }
            }
        }


        //获取前5个，然后三个作为老列表，2个作为新列表进行测试
        if (ll.size() > 5) {
            //proc方式获取
            OCImpl.getInstance(context).cacheDataToMemory(ll.get(0), "2");
            OCImpl.getInstance(context).cacheDataToMemory(ll.get(1), "2");
            OCImpl.getInstance(context).cacheDataToMemory(ll.get(2), "2");

            JSONArray arr = new JSONArray();
            arr.put(ll.get(2));
            arr.put(ll.get(3));
            arr.put(ll.get(4));
            // 进行新旧对比，内部打印日志和详情
            OCImpl.getInstance(context).getAliveAppByProc(arr);
        } else {
            EL.e("应用列表还没有5个。。无法正常测试");
        }

    }

    // 7. OC逻辑验证
    private static void runCaseP7(final Context context) {
        EL.i("=================== OC逻辑验证 ===============");
        OCImpl.getInstance(context).processOC();
    }

    //8. OC部分数据入库测试
    private static void runCaseP8(final Context context) {
        EL.i("=================== 插入OC数据到数据库测试 ===============");
        // 获取安装列表
        List<JSONObject> list = AppSnapshotImpl.getInstance(context).getAppDebugStatus();

        //获取有界面的安装列表
        PackageManager pm = context.getPackageManager();
        List<String> ll = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {

            JSONObject o = list.get(i);

            if (o != null && o.has(EGContext.TEXT_DEBUG_APP)) {
                String pkg = o.optString(EGContext.TEXT_DEBUG_APP);
                if (SystemUtils.hasLaunchIntentForPackage(pm, pkg) && !ll.contains(pkg)) {
                    ll.add(pkg);
                }
            }
        }

        if (ll.size() == 0) {
            throw new RuntimeException("安装列表获取是空");
        }
        // 2.放内存里
        for (int i = 0; i < ll.size(); i++) {
            OCImpl.getInstance(context).cacheDataToMemory(ll.get(i), "2");
        }
        //写入库
        OCImpl.getInstance(context).processScreenOff();

        EL.i("=================== 从数据库取出OC数据 ===============");

        JSONArray oc = TableProcess.getInstance(context).selectOC(EGContext.LEN_MAX_UPDATE_SIZE);

        if (oc != null) {
            EL.i("获取OC数据:" + oc.toString());
        }
    }

    // 9. 忽略进程直接发起网络请求
    private static void runCaseP9(final Context context) {
        EL.i("----忽略进程直接发起网络请求-----");
        UploadImpl.getInstance(context).doUploadImpl();
    }

    // 10.【安装列表】检查并更新数据库数据
    private static void runCaseP10(final Context context) {
        EL.i("----【安装列表】检查并更新数据库数据-----");
        AppSnapshotImpl.getInstance(context).getSnapShotInfo();

    }


    // 11. 【安装列表】查询数据库
    private static void runCaseP11(final Context context) {
        EL.i("----【安装列表】数据库-----");
        JSONArray ins = TableProcess.getInstance(context).selectSnapshot(EGContext.LEN_MAX_UPDATE_SIZE);
        EL.i(ins);
    }

    // 12.【定位信息】直接获取。。。忽略多进程
    private static void runCaseP12(final Context context) {
        EL.i("----【定位信息】直接获取。。。忽略多进程-----");
        LocationImpl.getInstance(context).getLocationInfoInThread();
    }

    // 13. 测试加密数据
    private static void runCaseP13(final Context context) {
        EL.i("----测试加密数据-----");
        UploadImpl.getInstance(context).messageEncrypt("测试加密数据");
    }

    // 14.测试双卡
    private static void runCaseP14(final Context context) {
        EL.i("----测试双卡-----");
        String imeis = DoubleCardSupport.getInstance().getIMEIS(context);
        EL.i("----测试双卡IMEI: " + imeis);
        String imsis = DoubleCardSupport.getInstance().getIMSIS(context);
        EL.i("----测试双卡IMSI: " + imsis);
    }

    private static void runCaseP15(final Context mContext) {
        FileObserver fileObserver = new FileObserver("/proc/net/tcp", FileObserver.ALL_EVENTS) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                Log.v(path, event + "");
            }
        };
        fileObserver.startWatching();
    }

    private static void runCaseP16(final Context mContext) {
        case16Impl(mContext);
    }


    private static void runCaseP17(final Context mContext) {

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone rt = RingtoneManager.getRingtone(mContext.getApplicationContext(), uri);
        rt.stop();

        try {
            FileInputStream inputStream = new FileInputStream(mContext.getCacheDir().getAbsoluteFile() + "/netimpl.log");
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            StringBuilder builder = new StringBuilder();
            while (true) {
                String str = bufferedReader.readLine();
                if (str != null) {
                    builder.append(str).append("\n");
                } else {
                    break;
                }
            }
            EL.i(builder.toString());
            bufferedReader.close();
        } catch (Exception e) {
            EL.e(e);
        }
    }

    private static void runCaseP18(final Context mContext) {
        NetImpl.getInstance(mContext).getNetInfo();
    }

    private static void runCaseP19(final Context mContext) {
        EL.i("----测试灰名单-----");
        try {
            TelephonyManager mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            List<NeighboringCellInfo> list = (List<NeighboringCellInfo>) ClazzUtils.invokeObjectMethod(mTelephonyManager, "getNeighboringCellInfo");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            DeviceImpl.getInstance(mContext).getBluetoothAddress(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            DevStatusChecker.getInstance().isDebugRom();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            USMUtils.getUsageEventsByInvoke(0, System.currentTimeMillis(), mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            EContextHelper.getContext(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runCaseP20(final Context mContext) {
        TelephonyManager mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mTelephonyManager.getAllCellInfo();
        }
    }

    private static void runCaseP21(final Context mContext) {
        try {


//            loadStatic(mContext, new File("/data/local/tmp/temp_20200108-180351.jar"),
//                  "com.analysys.Ab", "init",
//                    new Class[]{Context.class}, new Object[]{mContext});
            PatchHelper.loadStatic(mContext,
                    new File("/data/local/tmp/temp_20200108-180351.jar"),
                    "com.analysys.Ab", "init",
                    new Class[]{Context.class}, new Object[]{mContext});
        } catch (Throwable e) {
            EL.e(e);
        }
    }

    private static void runCaseP22(final Context context) {
        String pkgName = context.getPackageName();

        EL.i("容器运行检测, 包名：  "+ pkgName);

        //1. 安装列表不包含自己,肯定不行
        if (!SystemUtils.hasPackageNameInstalled(context, pkgName)) {
            EL.i("容器运行检测, 安装列表不存在自己安装的app   ------》 容器运行");
            return;
        }else{
            EL.i("容器运行检测, 安装列表包含自己的app");
        }
        // 2. /data/data/pkg/files
        //   /data/user/0/pkg/files
        // 下面代码兼容性文件比较严重，小米双开无法识别
//        String fPath = context.getFilesDir().getAbsolutePath();
//        L.i("file path:" + fPath);
//        if (!fPath.startsWith("/data/data/" + pkgName + "/")
//                && !fPath.startsWith("/data/user/0/" + pkgName + "/")
//        ) {
//            return true;
//        }
        // 3. 遍历文件夹
        try {
            File dir = new File("/data/data/" + pkgName + "/files");
            if (dir.exists()) {
                EL.i("容器运行检测: " + dir.exists() + "----文件个数:" + dir.list().length + "-------->" + Arrays.asList(dir.list()));
            } else {
                EL.i("容器运行检测, files文件夹不存在，创建文件夹");
                dir.mkdirs();
            }
            if (!dir.exists()) {
                EL.i("容器运行检测, files文件夹创建失败    ------》 容器运行 ");
                return;
            }
            File temp = new File(dir, "test");
            if (temp.exists()) {
                EL.i("容器运行检测, test文件存在，删除重新操作.");
                temp.delete();
            }
            EL.i("容器运行检测, test文件不存在，尝试创建");

            boolean result = temp.createNewFile();
            if (!result) {
                EL.i("容器运行检测, test创建失败...   ------》 容器运行");
                return;
            } else {
                EL.i("容器运行检测, test创建成功...");
            }
        } catch (Throwable e) {
            EL.e(e);
        }

        // 4. 通过shell ps获取对应进程信息，理论上只有自己的包名和和子进程的。 必须包含自己包名
//        try {
//            String psInfo = ShellUtils.shell("ps");
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.i("容器运行检测 shell ps: " + psInfo);
//            }
//            if (!TextUtils.isEmpty(psInfo) && !psInfo.contains(pkgName)) {
//                return true;
//            }
//        } catch (Throwable e) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.e(e);
//            }
//        }


//        // 5. pid check /proc/pid/cmdline
//        int pid = android.os.Process.myPid();
//        L.e("pid:" + pid);
        // 6. classloader name check failed
//        L.i("----------->" + getClass().getClassLoader().getClass().getName());
//        L.i("---+++++++-->" + context.getClassLoader().getClass().getName());
    }


    private static void case16Impl(Context mContext) {
        int test_size = 5000;

        System.gc();

        List<Throwable> throwables = new ArrayList<>();
        //最大分配内存
        float maxMemory = (float) (Runtime.getRuntime().maxMemory() * 1.0 / (1024 * 1024));
        //当前分配的总内存
        float totalMemory = (float) (Runtime.getRuntime().totalMemory() * 1.0 / (1024 * 1024));
        //剩余内存
        float freeMemory = (float) (Runtime.getRuntime().freeMemory() * 1.0 / (1024 * 1024));
        StringBuilder builder = new StringBuilder();
        builder
                .append("执行次数:").append(test_size).append("\n")
                .append("测试前总电量:")
                .append(BatteryModuleNameInfo.getInstance().getBatteryScale()).append("\n")
                .append("测试前剩余电量:")
                .append(BatteryModuleNameInfo.getInstance().getBatteryLevel()).append("\n")
                .append("测试前电池温度:")
                .append(BatteryModuleNameInfo.getInstance().getBatteryTemperature()).append("\n")
                .append("测试前最大分配内存:")
                .append(maxMemory).append("\n")
                .append("测试前当前分配的总内存:")
                .append(totalMemory).append("\n")
                .append("测试前剩余内存:")
                .append(freeMemory).append("\n");


        long abs = 0;
        int max = 0, min = Integer.MAX_VALUE;
        long time = System.currentTimeMillis();
        for (int i = 0; i < test_size; i++) {
            String[] result = {
                    "cat /proc/net/tcp",
                    "cat /proc/net/tcp6",
                    "cat /proc/net/udp",
                    "cat /proc/net/udp6",
                    "cat /proc/net/raw",
                    "cat /proc/net/raw6",
            };
            HashSet<NetInfo> pkgs = new HashSet<NetInfo>();
            try {
                for (String cmd : result
                ) {
                    // pkgs.addAll(NetImpl.getInstance(mContext).getNetInfoFromCmd(cmd));
                }
            } catch (Exception e) {
                throwables.add(e);
            }
            JSONArray array = new JSONArray();
            for (NetInfo info :
                    pkgs) {
                array.put(info.toJson());
            }
            String json = array.toString();

            int length = json.length();
            max = Math.max(max, length);
            min = Math.min(min, length);
            abs = (abs + length);
            Log.v("testcasep16", i + "");
        }
        abs = abs / test_size;
        time = System.currentTimeMillis() - time;

        System.gc();

        //最大分配内存
        maxMemory = (float) (Runtime.getRuntime().maxMemory() * 1.0 / (1024 * 1024));
        //当前分配的总内存
        totalMemory = (float) (Runtime.getRuntime().totalMemory() * 1.0 / (1024 * 1024));
        //剩余内存
        freeMemory = (float) (Runtime.getRuntime().freeMemory() * 1.0 / (1024 * 1024));


        builder
                .append("\n")
                .append("总耗时:").append(time).append("\n")
                .append("平均耗时:").append(time / (double) test_size).append("\n")
                .append("测试后总电量:")
                .append(BatteryModuleNameInfo.getInstance().getBatteryScale()).append("\n")
                .append("测试后剩余电量:")
                .append(BatteryModuleNameInfo.getInstance().getBatteryLevel()).append("\n")
                .append("测试后电池温度:")
                .append(BatteryModuleNameInfo.getInstance().getBatteryTemperature()).append("\n")
                .append("测试后最大分配内存:")
                .append(maxMemory).append("\n")
                .append("测试后当前分配的总内存:")
                .append(totalMemory).append("\n")
                .append("测试后剩余内存:")
                .append(freeMemory).append("\n")
                .append("平均:最大:最小:")
                .append(abs).append("\n")
                .append(max).append("\n")
                .append(min).append("\n");

        for (Throwable throwable : throwables
        ) {
            builder.append(throwable.getMessage()).append("\n");
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(mContext.getCacheDir().getAbsoluteFile() + "/netimpl.log");
            OutputStreamWriter or = new OutputStreamWriter(outputStream);
            BufferedWriter writer = new BufferedWriter(or);
            writer.write(builder.toString());

            writer.close();
            or.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone rt = RingtoneManager.getRingtone(mContext.getApplicationContext(), uri);
            rt.play();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
