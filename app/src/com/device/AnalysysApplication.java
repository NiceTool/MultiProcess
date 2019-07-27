package com.device;

import android.app.Application;

import com.analysys.track.AnalysysTracker;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 自定义的application
 * @Version: 1.0
 * @Create: 2019-07-27 14:03:51
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class AnalysysApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initAnalysys();
    }

    /**
     * 初始化统计功能
     */
    private void initAnalysys() {
        // 设置打开debug模式，上线请置为false
        AnalysysTracker.setDebugMode(false);
        // 初始化接口:第二个参数填写您在平台申请的appKey,第三个参数填写
        AnalysysTracker.init(this, "7752552892442721d", "WanDouJia");
    }
}
