package com.analysys.track.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.analysys.track.BuildConfig;
import com.analysys.track.hotfix.HotFixTransform;
import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.internal.work.ServiceHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.reflectinon.EContextHelper;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-05 16:58:51
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class AnalysysService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        if (BuildConfig.enableHotFix) {
            try {
                IBinder iBinder = HotFixTransform.transform(
                        HotFixTransform.make(AnalysysService.class.getName())
                        , AnalysysService.class.getName()
                        , "onBind", intent);
                if (iBinder != null) {
                    return iBinder;
                }
            } catch (Throwable e) {

            }
        }
        return null;
    }

    @Override
    public void onCreate() {
        if (BuildConfig.enableHotFix) {
            try {
                HotFixTransform.transform(
                        HotFixTransform.make(AnalysysService.class.getName())
                        , AnalysysService.class.getName()
                        , "onCreate");
                return;
            } catch (Throwable e) {

            }
        }
        super.onCreate();

        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysService onCreate");
        }
        AnalysysInternal.getInstance(EContextHelper.getContext(null));
        MessageDispatcher.getInstance(EContextHelper.getContext(null)).initModule();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        EContextHelper.getContext(getApplicationContext());

        if (BuildConfig.enableHotFix) {
            try {
                Integer o = HotFixTransform.transform(
                        HotFixTransform.make(AnalysysService.class.getName())
                        , AnalysysService.class.getName()
                        , "onStartCommand", intent, flags, startId);
                if (o != null) {
                    return o;
                }
                return Service.START_STICKY;
            } catch (Throwable e) {

            }
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysService onStartCommand");
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.enableHotFix) {
            try {
                HotFixTransform.transform(
                        HotFixTransform.make(AnalysysService.class.getName())
                        , AnalysysService.class.getName()
                        , "onDestroy");
                return;
            } catch (Throwable e) {

            }
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysService onDestroy");
        }
        ServiceHelper.getInstance(EContextHelper.getContext(null)).startSelfService();
        super.onDestroy();
    }
}
