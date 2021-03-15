package cn.analysys.casedemo.cases.utils;

import android.widget.Toast;

import com.cslib.CaseHelper;
import com.cslib.utils.L;

/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: 调用demo和case的入口
 * @Version: 1.0
 * @Create: 2021/03/70 18:06:39
 * @author: sanbo
 */
public class Woo {
    /**
     * case中的toast
     *
     * @param info
     */
    private static void toastFromCase(String info) {
        Toast.makeText(CaseHelper.getCaseContext(), info, Toast.LENGTH_SHORT).show();
    }

    /**
     * case中的toast
     *
     * @param info
     */
    public static void logFormCase(String info) {
        L.i(info);
        toastFromCase(info);
    }
}
