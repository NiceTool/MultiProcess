package com.analysys.track.internal.impl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.text.TextUtils;

import com.analysys.track.internal.Content.DataController;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.reflectinon.EContextHelper;

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
            SenSorModuleNameImpl.Holder.INSTANCE.mContext = EContextHelper.getContext(context);
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
            List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
            JSONObject info;
            senSorArray = new JSONArray();
            for (int i = 0; i < sensorList.size(); i++) {
                Sensor s = sensorList.get(i);
                info = new JSONObject();
                if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.DevInfo.SenSorName,
                        DataController.SWITCH_OF_SENSOR_NAME) && !TextUtils.isEmpty(s.getName())) {
                    // 传感器名称
                    info.put(DeviceKeyContacts.DevInfo.SenSorName, s.getName());
                }

                // 传感器版本
                if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.DevInfo.SenSorVersion,
                        DataController.SWITCH_OF_SENSOR_VERSION)
                        && !TextUtils.isEmpty(String.valueOf(s.getVersion()))) {
                    // 传感器名称
                    info.put(DeviceKeyContacts.DevInfo.SenSorVersion, String.valueOf(s.getVersion()));
                }

                // 传感器厂商
                if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.DevInfo.SenSorManufacturer,
                        DataController.SWITCH_OF_SENSOR_MANUFACTURER) && !TextUtils.isEmpty(s.getVendor())) {
                    // 传感器名称
                    info.put(DeviceKeyContacts.DevInfo.SenSorManufacturer, s.getVendor());
                }
                try {
                    // 传感器id
                    if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.DevInfo.SenSorId,
                            DataController.SWITCH_OF_SENSOR_ID)) {
                        // 传感器名称
                        info.put(DeviceKeyContacts.DevInfo.SenSorId, s.getId());
                    }
                } catch (Throwable t1) {
                }
                try {
                    // 当传感器是唤醒状态返回true
                    if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.DevInfo.SenSorWakeUpSensor,
                            DataController.SWITCH_OF_SENSOR_WAKEUPSENSOR)) {
                        // 传感器名称
                        info.put(DeviceKeyContacts.DevInfo.SenSorWakeUpSensor, s.isWakeUpSensor());
                    }
                } catch (Throwable t) {
                    // 当传感器是唤醒状态返回true
                    if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.DevInfo.SenSorWakeUpSensor,
                            DataController.SWITCH_OF_SENSOR_WAKEUPSENSOR)) {
                        info.put(DeviceKeyContacts.DevInfo.SenSorWakeUpSensor, false);
                    }
                }
                // 传感器耗电量
                if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.DevInfo.SenSorPower,
                        DataController.SWITCH_OF_SENSOR_POWER)) {
                    info.put(DeviceKeyContacts.DevInfo.SenSorPower, s.getPower());
                }
                senSorArray.put(info);
            }
        } catch (Throwable t) {
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
