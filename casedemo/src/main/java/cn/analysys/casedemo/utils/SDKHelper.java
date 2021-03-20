package cn.analysys.casedemo.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import androidx.annotation.NonNull;

import com.analysys.track.db.DBConfig;
import com.analysys.track.db.DBManager;
import com.analysys.track.db.DBUtils;
import com.analysys.track.internal.impl.DeviceImpl;
import com.analysys.track.internal.impl.ftime.LmFileUitls;
import com.analysys.track.utils.AndroidManifestHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.MDate;
import com.analysys.track.utils.PermissionUtils;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.pkg.PkgList;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.cslib.CaseHelper;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: 所有SDK的引用全部放该类
 * @Version: 1.0
 * @Create: 2021/03/67 11:26:31
 * @author: sanbo
 */
public class SDKHelper {

    @TargetApi(23)
    public static void reqPermission(@NonNull Activity activity, @NonNull String[] permissionList, int requestCode) {
        PermissionUtils.reqPermission(activity, permissionList, requestCode);
    }

    public static ConcurrentHashMap<String, Long> getFileAndCacheTime() {
        ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<String, Long>();
        List<LmFileUitls.AppTime> ats = LmFileUitls.getLastAliveTimeInBaseDir(getContext());
        if (ats.size() > 0) {
            for (LmFileUitls.AppTime at : ats) {
                String pkg = at.getPackageName();
                long time = at.getLastActiveTime();
                map.put(pkg, time);
            }
        }
        return map;
    }

    public static ConcurrentHashMap<String, Long> getSDDirTime() {
        ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<String, Long>();
        List<LmFileUitls.AppTime> ats = LmFileUitls.getLastAliveTimeInSD(getContext());
        if (ats.size() > 0) {
            for (LmFileUitls.AppTime at : ats) {
                String pkg = at.getPackageName();
                long time = at.getLastActiveTime();
                map.put(pkg, time);
            }
        }
        return map;
    }

    /**
     * 获取末次活跃的列表
     *
     * @return
     */
    public static List<String> getLastAliveTimeStr() {
        List<String> result = new CopyOnWriteArrayList<>();
        List<LmFileUitls.AppTime> ats = LmFileUitls.getLastAliveTimeInBaseDir(getContext());
        if (ats.size() > 0) {
            for (LmFileUitls.AppTime at : ats) {
                result.add(at.toString());
            }
        }
        return result;
    }


    /**
     * 获取可用的context
     *
     * @return
     */
    public static Context getContext() {
        return getContext(CaseHelper.getCaseContext());
    }

    public static Context getContext(Context context) {
        return EContextHelper.getContext(context);
    }

    /**
     * 调用系统shell
     *
     * @param cmd
     * @return
     */
    public static String shell(String cmd) {
        return ShellUtils.shell(cmd);
    }

    /**
     * 获取安卓ID
     *
     * @return
     */
    public static String getAndroidID() {
        return DeviceImpl.getInstance(getContext()).getValueFromSettingSystem(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String convertLongTimeToHms(long time) {
        return MDate.convertLongTimeToHms(time);
    }

    /**
     * 获取安装app数量
     *
     * @return
     */
    public static int getInstallAppSize() {
        List<String> pkgs = PkgList.getInstance(getContext()).getAppPackageList();
        if (pkgs == null || pkgs.size() < 0) {
            return 0;
        }
        return pkgs.size();
    }

    public static void getInstallAppSizeByApi() {
        PkgList.getInstance(getContext()).getByApi();
    }

    public static void getInstallAppSizeByShell() {
        PkgList.getInstance(getContext()).getByShell();
    }

    public static void getInstallAppSizeByUid() {
        PkgList.getInstance(getContext()).getByUid();
    }


    /**
     * 判断是否两个类是否是有祖、父类关系
     *
     * @param subClass
     * @param fatherClass
     * @return
     */
    public static boolean isSubClass(Class<?> subClass, Class<?> fatherClass) {
        return AndroidManifestHelper.isSubClass(subClass, fatherClass);
    }

    public static void logi(String info) {
        //dev-sdk build.gradle中必须设置release=false&logcat=true
        ELOG.i(info);
    }

    public static void prepareDB() {
        DBManager.getInstance(getContext()).openDB();
        DBManager.getInstance(getContext()).closeDB();
    }

    public static boolean checkAppsnapshotDB() {
        try {
            if (DBUtils.isTableExist(DBManager.getInstance(getContext()).openDB(), DBConfig.AppSnapshot.TABLE_NAME)) {
                return true;
            }
        } catch (Throwable e) {
        }

        return false;
    }

    public static boolean checkLocationDB() {
        try {
            if (DBUtils.isTableExist(DBManager.getInstance(getContext()).openDB(), DBConfig.Location.TABLE_NAME)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public static boolean checkFinfoDB() {
        try {
            if (DBUtils.isTableExist(DBManager.getInstance(getContext()).openDB(), DBConfig.FInfo.TABLE_NAME)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public static boolean checkXxxDB() {
        try {
            if (DBUtils.isTableExist(DBManager.getInstance(getContext()).openDB(), DBConfig.XXXInfo.TABLE_NAME)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public static boolean checkScanDB() {
        try {
            if (DBUtils.isTableExist(DBManager.getInstance(getContext()).openDB(), DBConfig.ScanningInfo.TABLE_NAME)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public static boolean checkOCDB() {
        try {
            if (DBUtils.isTableExist(DBManager.getInstance(getContext()).openDB(), DBConfig.OC.TABLE_NAME)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public static boolean checkNetDB() {
        try {
            if (DBUtils.isTableExist(DBManager.getInstance(getContext()).openDB(), DBConfig.NetInfo.TABLE_NAME)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }
}
