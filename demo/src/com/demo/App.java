package com.demo;

import android.app.Application;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // EguanImpl.getInstance().setDebugMode(this, true);
        // EguanImpl.getInstance().initEguan(this, "7752552892442721d", "app channel");
    }

}
