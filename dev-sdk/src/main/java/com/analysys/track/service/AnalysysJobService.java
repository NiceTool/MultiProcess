package com.analysys.track.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;

import com.analysys.track.AnalysysTracker;
import com.analysys.track.BuildConfig;
import com.analysys.track.impl.HotFixTransform;
import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.internal.work.ServiceHelper;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.reflectinon.ClazzUtils;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-08 10:46:43
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
@TargetApi(21)
public class AnalysysJobService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters params) {
        try {
            //禁止灰色 api logcat
            ClazzUtils.unseal();
            AnalysysTracker.setContext(this);
            if (BuildConfig.enableHotFix) {
                try {
                    Boolean aBoolean = HotFixTransform.transform(
                            HotFixTransform.make(AnalysysJobService.class.getName())
                            , AnalysysJobService.class.getName()
                            , "onStartJob", params);
                    if (aBoolean != null) {
                        return aBoolean;
                    }
                } catch (Throwable e) {

                }
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i("AnalysysJobService onStartJob");
            }
            // 传递Context。防止因为Context缺失导致的调用异常
            AnalysysInternal.getInstance(null);
            MessageDispatcher.getInstance(null).initModule();
        } catch (Throwable e) {
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        try {
            //禁止灰色 api logcat
            ClazzUtils.unseal();
            AnalysysTracker.setContext(this);
            if (BuildConfig.enableHotFix) {
                try {
                    Boolean aBoolean = HotFixTransform.transform(
                            HotFixTransform.make(AnalysysJobService.class.getName())
                            , AnalysysJobService.class.getName()
                            , "onStopJob", params);
                    if (aBoolean != null) {
                        return aBoolean;
                    }
                } catch (Throwable e) {

                }
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i("AnalysysJobService onStopJob");
            }
            ServiceHelper.getInstance(EContextHelper.getContext()).startSelfService();
        } catch (Throwable e) {
        }
        return false;
    }

}
