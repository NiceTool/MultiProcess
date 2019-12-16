package com.analysys.track.internal.impl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.DataController;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 传感器获取
 * @Version: 1.0
 * @Create: 2019-08-05 16:18:18
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class SenSorModuleNameImpl {
    Context mContext;

    private SenSorModuleNameImpl() {
    }

    public static SenSorModuleNameImpl getInstance(Context context) {
        if (SenSorModuleNameImpl.Holder.INSTANCE.mContext == null) {
            SenSorModuleNameImpl.Holder.INSTANCE.mContext = EContextHelper.getContext();
        }

        return SenSorModuleNameImpl.Holder.INSTANCE;
    }

    /**
     * 获取传感器方法
     */
    public JSONArray getSensorInfo() {
        JSONArray senSorArray = null;
        try {
            SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            List<Sensor> sensorList = null;
            if (sensorManager != null) {
                sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
            }
            JSONObject info;
            senSorArray = new JSONArray();
            if (sensorList != null) {
                for (int i = 0; i < sensorList.size(); i++) {
                    Sensor s = sensorList.get(i);
                    info = new JSONObject();


                    //                if (PolicyImpl.getInstance(mContext).getValueFromSp(UploadKey.DevInfo.SenSorName,
                    //                        DataController.SWITCH_OF_SENSOR_NAME) && !TextUtils.isEmpty(s.getName())) {
                    if (SPHelper.getBooleanValueFromSP(mContext, UploadKey.DevInfo.SenSorName,
                            DataController.SWITCH_OF_SENSOR_NAME) && !TextUtils.isEmpty(s.getName())) {
                        // 传感器名称
                        info.put(UploadKey.DevInfo.SenSorName, s.getName());
                    }

                    // 传感器版本
                    //                if (PolicyImpl.getInstance(mContext).getValueFromSp(UploadKey.DevInfo.SenSorVersion,
                    //                        DataController.SWITCH_OF_SENSOR_VERSION)
                    //                        && !TextUtils.isEmpty(String.valueOf(s.getVersion()))) {
                    if (SPHelper.getBooleanValueFromSP(mContext, UploadKey.DevInfo.SenSorVersion,
                            DataController.SWITCH_OF_SENSOR_VERSION)
                            && !TextUtils.isEmpty(String.valueOf(s.getVersion()))) {
                        // 传感器名称
                        info.put(UploadKey.DevInfo.SenSorVersion, String.valueOf(s.getVersion()));
                    }

                    // 传感器厂商
                    //                if (PolicyImpl.getInstance(mContext).getValueFromSp(UploadKey.DevInfo.SenSorManufacturer,
                    //                        DataController.SWITCH_OF_SENSOR_MANUFACTURER)
                    if (SPHelper.getBooleanValueFromSP(mContext, UploadKey.DevInfo.SenSorManufacturer,
                            DataController.SWITCH_OF_SENSOR_MANUFACTURER)
                            && !TextUtils.isEmpty(s.getVendor())) {
                        // 传感器名称
                        info.put(UploadKey.DevInfo.SenSorManufacturer, s.getVendor());
                    }
                    try {
                        // 传感器id
                        //                    if (PolicyImpl.getInstance(mContext).getValueFromSp(UploadKey.DevInfo.SenSorId,
                        //                            DataController.SWITCH_OF_SENSOR_ID)) {
                        if (SPHelper.getBooleanValueFromSP(mContext, UploadKey.DevInfo.SenSorId,
                                DataController.SWITCH_OF_SENSOR_ID)) {
                            // 传感器名称
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                info.put(UploadKey.DevInfo.SenSorId, s.getId());
                            }
                        }
                    } catch (Throwable t1) {
                        if (BuildConfig.ENABLE_BUGLY) {
                            BuglyUtils.commitError(t1);
                        }
                    }
                    try {
                        // 当传感器是唤醒状态返回true
                        //                    if (PolicyImpl.getInstance(mContext).getValueFromSp(UploadKey.DevInfo.SenSorWakeUpSensor,
                        //                            DataController.SWITCH_OF_SENSOR_WAKEUPSENSOR)) {
                        if (SPHelper.getBooleanValueFromSP(mContext, UploadKey.DevInfo.SenSorWakeUpSensor,
                                DataController.SWITCH_OF_SENSOR_WAKEUPSENSOR)) {
                            // 传感器名称
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                info.put(UploadKey.DevInfo.SenSorWakeUpSensor, s.isWakeUpSensor());
                            }
                        }
                    } catch (Throwable t) {
                        if (BuildConfig.ENABLE_BUGLY) {
                            BuglyUtils.commitError(t);
                        }
                        // 当传感器是唤醒状态返回true
                        //                    if (PolicyImpl.getInstance(mContext).getValueFromSp(UploadKey.DevInfo.SenSorWakeUpSensor,
                        //                            DataController.SWITCH_OF_SENSOR_WAKEUPSENSOR)) {
                        if (SPHelper.getBooleanValueFromSP(mContext, UploadKey.DevInfo.SenSorWakeUpSensor,
                                DataController.SWITCH_OF_SENSOR_WAKEUPSENSOR)) {
                            info.put(UploadKey.DevInfo.SenSorWakeUpSensor, false);
                        }
                    }
                    // 传感器耗电量
                    //                if (PolicyImpl.getInstance(mContext).getValueFromSp(UploadKey.DevInfo.SenSorPower,
                    //                        DataController.SWITCH_OF_SENSOR_POWER)) {
                    if (SPHelper.getBooleanValueFromSP(mContext, UploadKey.DevInfo.SenSorPower,
                            DataController.SWITCH_OF_SENSOR_POWER)) {
                        info.put(UploadKey.DevInfo.SenSorPower, s.getPower());
                    }
                    senSorArray.put(info);
                }
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(t);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        }
        return senSorArray;
    }

    private static class Holder {
        private static final SenSorModuleNameImpl INSTANCE = new SenSorModuleNameImpl();
    }
}
