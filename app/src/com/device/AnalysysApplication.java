package com.device;

import android.app.Application;
import com.analysys.track.AnalysysTracker;


public class AnalysysApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AnalysysTracker.init(this, "7752552892442721d", "WanDouJia");
        AnalysysTracker.setDebugMode(this,true);
    }
}
