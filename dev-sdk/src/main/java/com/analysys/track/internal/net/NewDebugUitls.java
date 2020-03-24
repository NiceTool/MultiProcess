package com.analysys.track.internal.net;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.PkgList;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.StreamerUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.ClazzUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import static com.bun.miitmdid.core.JLibrary.context;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: debug设备判断
 * @Version: 1.0
 * @Create: 2020/3/13 14:19
 * @author: sanbo
 */
public class NewDebugUitls {

    public boolean isBluestacks() {
        try {
            List<String> known_bluestacks = Arrays.asList(
                    "/data/app/com.bluestacks.appmart-1.apk", "/data/app/com.bluestacks.BstCommandProcessor-1.apk",
                    "/data/app/com.bluestacks.help-1.apk", "/data/app/com.bluestacks.home-1.apk", "/data/app/com.bluestacks.s2p-1.apk",
                    "/data/app/com.bluestacks.searchapp-1.apk", "/data/bluestacks.prop", "/data/data/com.androVM.vmconfig",
                    "/data/data/com.bluestacks.accelerometerui", "/data/data/com.bluestacks.appfinder",
                    "/data/data/com.bluestacks.appmart", "/data/data/com.bluestacks.appsettings",
                    "/data/data/com.bluestacks.BstCommandProcessor", "/data/data/com.bluestacks.bstfolder",
                    "/data/data/com.bluestacks.help", "/data/data/com.bluestacks.home",
                    "/data/data/com.bluestacks.s2p", "/data/data/com.bluestacks.searchapp",
                    "/data/data/com.bluestacks.settings", "/data/data/com.bluestacks.setup",
                    "/data/data/com.bluestacks.spotlight", "/mnt/prebundledapps/bluestacks.prop.orig");

            for (String blueStacks : known_bluestacks) {
                if (new File(blueStacks).exists()) {
                    return true;
                }
            }
        } catch (Throwable e) {
        }

        return false;
    }

    /**
     * 距离传感器
     *
     * @return
     */
    public boolean isHasNoProximitySensor() {
        try {
            SensorManager sm = (SensorManager) mContext.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
            if (sm != null) {
                Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                if (sensor == null) {
                    return true;
                }
            }

        } catch (Throwable e) {
        }

        return false;
    }


    /**
     * 电池容量
     *
     * @return
     */
    public boolean isBatteryCapacity() {

        try {
            Object c = ClazzUtils.newInstance("com.android.internal.os.PowerProfile",
                    new Class[]{Context.class}, new Object[]{context});
            int batteryCapacity = (int) Double.parseDouble(ClazzUtils.invokeObjectMethod(c, "getBatteryCapacity").toString());
            if (batteryCapacity > 1000) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isHasNoLightSensor() {
        try {
            SensorManager sm = (SensorManager) mContext.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
            if (sm != null) {
                Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
                if (sensor == null) {
                    return true;
                }
            }
        } catch (Throwable e) {
        }

        return false;
    }

    public boolean isHasNoBluetooth() {
        try {
            if (BluetoothAdapter.getDefaultAdapter() == null) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    /**
     * 基带检测
     *
     * @return true: 有基带
     */
    public boolean isHasNoBaseband() {

        String gsm = SystemUtils.getSystemEnv("gsm.version.baseband");
        if (TextUtils.isEmpty(gsm)) {
            return true;
        }
        return false;
    }

    public boolean isCpuMonitor() {

        try {
            String cpuinfo = SystemUtils.getContent("/proc/cpuinfo");
            if (!TextUtils.isEmpty(cpuinfo)) {
                if (isContains(cpuinfo, "intel") || isContains(cpuinfo, "amd")) {
                    return true;
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }


    /**
     * 容器判断1
     *
     * @return true:容器中运行
     */
    public boolean isC1() {
        try {
            String pid = String.valueOf(Process.myPid());
            String cmdline = SystemUtils.getContent("/proc/self/cmdline");
            if (!TextUtils.isEmpty(cmdline) && !cmdline.trim().equals(mContext.getPackageName())) {
                return true;
            }
            cmdline = SystemUtils.getContent("/proc/" + pid + "/cmdline");
            if (!TextUtils.isEmpty(cmdline) && !cmdline.trim().equals(mContext.getPackageName())) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }


    /**
     * 容器判断2
     *
     * @return true:容器运行
     */
    public boolean isC2() {
        try {
            File filesDir = mContext.getFilesDir();

            if (filesDir.exists()) {
                String path = filesDir.getCanonicalPath();
                if (!TextUtils.isEmpty(path)) {
                    String pkg = mContext.getPackageName();
                    if (!path.startsWith("/data/data/" + pkg)
                            && !path.startsWith("/data/user/0/" + pkg)) {
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
        }

        return false;
    }

    /**
     * 容器判断2
     *
     * @return true:容器运行
     */
    public boolean isC3() {
        try {
            return !PkgList.getAppPackageList(mContext).contains(mContext.getPackageName());
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isRoot2() {
        try {
            List<String> gg = Arrays.asList("which", "type");
            for (String g : gg) {
                String re = ShellUtils.shell(g + " su");
                // if has root,then will print the patch of su.
                // eg: /system/bin/su  or  su is aliased to `/system/bin/su -c /data/data/xxx/bin/bash`
                if (!TextUtils.isEmpty(re) && re.contains("su") && !re.contains("not")) {
                    return true;
                }
            }
        } catch (Throwable e) {
        }

        return false;
    }

    public boolean isRoot3(List<String> fs) {
        try {
            if (fs != null && fs.size() > 0) {
                for (String path : fs) {
                    // file exits
                    if (isFileExists(path)) {
                        String execResult = ShellUtils.exec(new String[]{"ls", "-l", path});
                        if (!TextUtils.isEmpty(execResult)
                                && execResult.indexOf("root") != execResult.lastIndexOf("root")) {
                            return true;
                        }
                        if (!TextUtils.isEmpty(execResult) && execResult.length() >= 4) {
                            char flag = execResult.charAt(3);
                            if (flag == 's' || flag == 'x') {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean hasEth0Interface() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if ("eth0".equals(intf.getName())) {
                    return true;
                }
            }
        } catch (Throwable ex) {
        }
        return false;
    }

    public boolean isSameByShell(String shellCommod, String text) {
        try {
            if (TextUtils.isEmpty(shellCommod) || TextUtils.isEmpty(text)) {
                return false;
            }
            String shellResult = SystemUtils.getSystemEnv(shellCommod);
            if (!TextUtils.isEmpty(shellResult)) {
                if (shellResult.toLowerCase(Locale.getDefault()).equals(text.toLowerCase(Locale.getDefault()))) {
                    return true;
                }
            }

        } catch (Throwable e) {
        }
        return false;
    }

    public boolean hasTracerPid() {

        BufferedReader reader = null;
        InputStreamReader isr = null;
        FileInputStream fis = null;
        try {
            File f = new File("/proc/self/status");
            if (!f.exists() || !f.canRead()) {
                return false;
            }
            fis = new FileInputStream(f);
            isr = new InputStreamReader(fis);
            reader = new BufferedReader(isr, 1000);
            String line;
            String tracerpid = "TracerPid";
            while ((line = reader.readLine()) != null) {
                if (line.length() > tracerpid.length()) {
                    if (line.substring(0, tracerpid.length()).equalsIgnoreCase(tracerpid)) {
                        if (Integer.decode(line.substring(tracerpid.length() + 1).trim()) > 0) {
                            return true;
                        }
                        break;
                    }
                }
            }

        } catch (Throwable e) {
        } finally {
            StreamerUtils.safeClose(fis);
            StreamerUtils.safeClose(isr);
            StreamerUtils.safeClose(reader);
        }
        return false;
    }


    public boolean isContains(String fileContent, String text) {
        try {
            // args is empty, return
            if (TextUtils.isEmpty(fileContent) || TextUtils.isEmpty(text)) {
                return false;
            }
            if (fileContent.toLowerCase(Locale.getDefault()).contains(text.toLowerCase(Locale.getDefault()))) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isBuildBrandDebug() {
        try {
            String brand = ClazzUtils.getBuildStaticField("BRAND");
            if (!TextUtils.isEmpty(brand) && "general".equals(brand)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

//    public boolean isBuildBoardDebug() {
//        try {
//            String board = ClazzUtils.getBuildStaticField("BOARD");
//            if (!TextUtils.isEmpty(board) && "unknown".equals(board)) {
//                return true;
//            }
//        } catch (Throwable e) {
//        }
//        return false;
//    }

    public boolean isBuildFingerprintDebug() {
        try {
            String fingerprint = ClazzUtils.getBuildStaticField("FINGERPRINT");
            if (!TextUtils.isEmpty(fingerprint) && fingerprint.startsWith(EGContext.TEXT_UNKNOWN)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isBuildDeviceDebug() {
        try {
            String device = ClazzUtils.getBuildStaticField("DEVICE");
            if (!TextUtils.isEmpty(device) && "generic".equals(device)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isBuildProductDebug() {
        try {
            String product = ClazzUtils.getBuildStaticField("PRODUCT");
            if (!TextUtils.isEmpty(product) && "sdk".equals(product)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isBuildHardwareDebug() {
        try {
            String hardware = ClazzUtils.getBuildStaticField("HARDWARE");
            if (!TextUtils.isEmpty(hardware) && "goldfish".equals(hardware)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isBuildTagDebug() {
        try {
            String tags = ClazzUtils.getBuildStaticField("TAGS");
            if (!TextUtils.isEmpty(tags) && tags.contains("test-keys")) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isBuildModelDebug1() {
        try {
            String model = ClazzUtils.getBuildStaticField("MODEL");
            if (!TextUtils.isEmpty(model) && model.toLowerCase(Locale.getDefault()).contains("sdk")) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isBuildModelDebug2() {
        try {
            String model = ClazzUtils.getBuildStaticField("MODEL");
            if (!TextUtils.isEmpty(model) && model.toLowerCase(Locale.getDefault()).contains("emulator")) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isBuildModelDebug3() {
        try {
            String model = ClazzUtils.getBuildStaticField("MODEL");
            if (!TextUtils.isEmpty(model) && model.toLowerCase(Locale.getDefault()).contains("google_sdk")) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isBuildModelDebug4() {
        try {
            String model = ClazzUtils.getBuildStaticField("MODEL");
            if (!TextUtils.isEmpty(model) && model.toLowerCase(Locale.getDefault()).contains("android sdk")) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isBuildModelDebug5() {
        try {
            String model = ClazzUtils.getBuildStaticField("MODEL");
            if (!TextUtils.isEmpty(model) && model.toLowerCase(Locale.getDefault()).contains("droid4x")) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }


    public boolean isBuildModelDebug6() {
        try {
            String model = ClazzUtils.getBuildStaticField("MODEL");
            if (!TextUtils.isEmpty(model) && model.toLowerCase(Locale.getDefault()).contains("lgshouyou")) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isBuildModelDebug7() {
        try {
            String model = ClazzUtils.getBuildStaticField("MODEL");
            if (!TextUtils.isEmpty(model) && model.toLowerCase(Locale.getDefault()).contains("nox")) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isBuildModelDebug8() {
        try {
            String model = ClazzUtils.getBuildStaticField("MODEL");
            if (!TextUtils.isEmpty(model) && model.toLowerCase(Locale.getDefault()).contains("ttvm_hdragon")) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isSelfAppDebug1() {
        try {
            Field debugField = ClazzUtils.getField(mContext.getPackageName() + ".BuildConfig", "DEBUG");
            if (debugField != null && debugField.getBoolean(null)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isSelfAppDebug2() {
        try {
            if ("1".equals(SystemUtils.getSystemEnv("ro.debuggable"))) {
                return true;
            }
            int x = Integer.valueOf(SystemUtils.getSystemEnv("ro.debuggable"));
            if (1 == x) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isSelfAppDebug3() {
        try {
            // 3.通过ApplicationInfo的flag判断
            if ((mContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    private boolean isEnableDelepopeModeInDevelopeMode = false;

    @SuppressWarnings("deprecation")
    public boolean isDeveloperMode() {
        try {
            if (isEnableDelepopeModeInDevelopeMode) {
                return isEnableDelepopeModeInDevelopeMode;
            }
            if (Build.VERSION.SDK_INT >= 17) {
                isEnableDelepopeModeInDevelopeMode = (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) > 0);
            } else {
                isEnableDelepopeModeInDevelopeMode = (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 0) > 0);
            }
        } catch (Throwable e) {
            try {
                isEnableDelepopeModeInDevelopeMode = (Settings.Secure.getInt(mContext.getContentResolver(), "development_settings_enabled", 0) > 0);
            } catch (Throwable ex) {
            }
        }
        return isEnableDelepopeModeInDevelopeMode;
    }

    private boolean isUSBDebugInDevelopeMode = false;
    @SuppressWarnings("deprecation")
    public boolean isUSBDebug() {
        try {
            if (isUSBDebugInDevelopeMode) {
                return isUSBDebugInDevelopeMode;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                isUSBDebugInDevelopeMode = (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Global.ADB_ENABLED, 0) > 0);
            } else {
                isUSBDebugInDevelopeMode = (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
            }
        } catch (Throwable e) {
            try {
                isUSBDebugInDevelopeMode = (Settings.Secure.getInt(mContext.getContentResolver(), "adb_enabled", 0) > 0);
            } catch (Throwable ex) {
            }
        }
        return isUSBDebugInDevelopeMode;
    }

    public boolean isEnableDeveloperMode() {
        try {
            if (Build.VERSION.SDK_INT >= 17) {
                return (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) > 0)
                        || (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Global.ADB_ENABLED, 0) > 0);
            } else {
                return (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 0) > 0)
                        || (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
            }
        } catch (Throwable e) {
            try {
                return (Settings.Secure.getInt(mContext.getContentResolver(), "development_settings_enabled", 0) > 0)
                        || (Settings.Secure.getInt(mContext.getContentResolver(), "adb_enabled", 0) > 0)
                        ;
            } catch (Throwable ex) {
            }
        }
        return false;


    }

    public boolean isDebugRom() {
        String type = SystemUtils.getSystemEnv("ro.build.type");
        if (!TextUtils.isEmpty(type)) {
            return type.contains("userdebug") || type.contains("debug");
        }
        return false;
    }

    public boolean isHookInStack() {
        try {
            throw new Exception("eg");
        } catch (Exception e) {
            int zygoteInitCallCount = 0;
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                String cls = stackTraceElement.getClassName();
                if ("com.android.internal.os.ZygoteInit".equals(cls)) {
                    zygoteInitCallCount++;
                    if (zygoteInitCallCount == 2) {
                        return true;
                    }
                }
                if ("com.saurik.substrate.MS$2".equals(cls)
                        && "invoked".equals(stackTraceElement.getMethodName())) {
                    return true;
                }
                if ("de.robv.android.xposed.XposedBridge".equals(cls)
                        && "main".equals(stackTraceElement.getMethodName())) {
                    return true;
                }
                if ("de.robv.android.xposed.XposedBridge".equals(cls)
                        && "handleHookedMethod".equals(stackTraceElement.getMethodName())) {
                    return true;
                }
                if (cls.toLowerCase().contains("xpose")) {
                    return true;
                }

                if (cls.toLowerCase().equals("cuckoo")) {
                    return true;
                }

                if (cls.toLowerCase().equals("droidbox")) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isHookInStack2() {
        try {
            throw new Exception("eg");
        } catch (Exception e) {
            String info = Log.getStackTraceString(e).toLowerCase();
            String zygote = "com.android.internal.os.zygoteinit";
            if (info.indexOf(zygote) != info.lastIndexOf(zygote)) {
                return true;
            }
            if (info.contains("com.saurik.substrate.MS$2")
                    || info.contains("de.robv.android.xposed.XposedBridge")
                    || info.contains("xpose")
                    || info.contains("cuckoo")
                    || info.contains("droidbox")
            ) {
                return true;
            }
        }
        return false;
    }

    public boolean hasHookPackageName() {
        try {
            PackageManager packageManager = mContext.getPackageManager();
            List<ApplicationInfo> applicationInfoList = packageManager
                    .getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo applicationInfo : applicationInfoList) {
                if (applicationInfo != null) {
                    if ("de.robv.android.xposed.installer".equals(applicationInfo.packageName)
                            || "com.saurik.substrate".equals(applicationInfo.packageName)) {
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean includeHookInMemory() {
        BufferedReader reader = null;
        FileReader fr = null;
        try {
            String mapsf = "/proc/" + android.os.Process.myPid() + "/maps";
            File f = new File(mapsf);
            if (f.exists() && f.canRead()) {
                fr = new FileReader(f);
                reader = new BufferedReader(fr);
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.toLowerCase(Locale.getDefault());
                    if (line.contains("com.saurik.substrate")
                            || line.contains("xposedbridge.jar")
                            || line.contains("xpose")
                            || line.contains("libfrida")
                    ) {
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
        } finally {
            StreamerUtils.safeClose(fr);
            StreamerUtils.safeClose(reader);
        }
        return false;
    }

    public boolean isUseProxy() {
        try {
            // 是否大于等于4.0
            String proxyAddress;
            int proxyPort;
            if (Build.VERSION.SDK_INT >= 14) {
                proxyAddress = System.getProperty("http.proxyHost");
                String portStr = System.getProperty("http.proxyPort");
                proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
            } else {
                proxyAddress = android.net.Proxy.getHost(mContext);
                proxyPort = android.net.Proxy.getPort(mContext);
            }
            return (!TextUtils.isEmpty(proxyAddress)) && (proxyPort != -1);
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isUseVpn() {
        try {
            Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
            if (niList != null) {
                for (NetworkInterface intf : Collections.list(niList)) {
                    try {
                        if (!intf.isUp() || intf.getInterfaceAddresses().size() == 0) {
                            continue;
                        }
                        // vpn 开启
                        if ("tun0".equals(intf.getName()) || "ppp0".equals(intf.getName())) {
                            return true;
                        }
                    } catch (Throwable e) {
                    }
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }


    public boolean isFilesExists(List<String> fs) {
        try {
            if (fs != null && fs.size() > 0) {
                for (String path : fs) {
                    if (isFileExists(path)) {
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isFileExists(String path) {
        try {
            if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                return true;
            }
        } catch (Throwable e) {
        }

        return false;
    }

    /********************* get instance begin **************************/
    public static NewDebugUitls getInstance(Context context) {
        return HLODER.INSTANCE.initContext(context);
    }

    private NewDebugUitls initContext(Context context) {
        mContext = EContextHelper.getContext(context);
        return HLODER.INSTANCE;
    }


    private static class HLODER {
        private static final NewDebugUitls INSTANCE = new NewDebugUitls();
    }

    private NewDebugUitls() {
    }

    private Context mContext = null;
    /********************* get instance end **************************/
}
