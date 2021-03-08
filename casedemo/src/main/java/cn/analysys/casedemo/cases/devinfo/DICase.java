package cn.analysys.casedemo.cases.devinfo;

import com.analysys.track.internal.impl.DeviceImpl;
import com.cslib.CaseHelper;
import com.cslib.defcase.ETestCase;

import cn.analysys.casedemo.cases.CaseCtl;
import cn.analysys.casedemo.utils.EL;

public class DICase extends ETestCase {
    public DICase() {
        super("DI");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        String di = DeviceImpl.getInstance(CaseCtl.getContext()).getDeviceId();
        EL.i("di: " + di);
        return true;
    }

}
