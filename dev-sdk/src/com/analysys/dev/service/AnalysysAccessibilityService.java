package com.analysys.dev.service;

import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.internal.impl.OCImpl;
import com.analysys.dev.utils.reflectinon.Reflecer;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018年9月6日 上午11:01:46
 * @Author: sanbo
 */
public class AnalysysAccessibilityService extends AccessibilityService {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Reflecer.init();
        settingAccessibilityInfo();
    }

    private void settingAccessibilityInfo() {
        AccessibilityServiceInfo mASInfo = new AccessibilityServiceInfo();
        // 响应事件的类型，这里是窗口发生改变时
        mASInfo.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        // 反馈给用户的类型，这里是通用类型
        mASInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        // 设置flag
        if (Build.VERSION.SDK_INT >= 16) {
            updateFlags(mASInfo);
        }
        // 相应时间
        mASInfo.notificationTimeout = 1;
        // 设置描述
        setServiceInfo(mASInfo);
    }

    @TargetApi(16)
    private void updateFlags(AccessibilityServiceInfo mASInfo) {

        mASInfo.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String pkgName = String.valueOf(event.getPackageName());
        OCImpl.getInstance(this).RunningApps(pkgName, EGContext.OC_COLLECTION_TYPE_AUX);
    }

    @Override
    public void onInterrupt() {

    }
}