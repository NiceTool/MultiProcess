package com.hhhaiai.mpdemo;

import me.hhhaiai.ImpTask;
import me.hhhaiai.mptils.MpLog;
import me.hhhaiai.mptils.ProcessUtils;

public class ChinaPrint implements ImpTask {
    @Override
    public String getName() {
        return "[" + ProcessUtils.getCurrentProcessName() + "] I'm Chinese!";
    }

    @Override
    public void work() {
        MpLog.d(
                "ChinaPrint. work ------------["
                        + ProcessUtils.getCurrentProcessName()
                        + "]------print msg");
    }
}
