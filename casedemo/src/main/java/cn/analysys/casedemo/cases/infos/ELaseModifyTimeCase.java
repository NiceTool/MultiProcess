package cn.analysys.casedemo.cases.infos;

import com.cslib.defcase.ETestCase;

import java.util.List;

import cn.analysys.casedemo.sdkimport.Helper;
import cn.analysys.casedemo.utils.EL;

public class ELaseModifyTimeCase extends ETestCase {
    public ELaseModifyTimeCase() {
        super("文件末次修改");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {

        List<String> lastModifyTimeInfos = Helper.getLastAliveTimeStr();
        int len = lastModifyTimeInfos.size();
        EL.i("文件个数:" + len);
        if (len == 1) {
            if (lastModifyTimeInfos.get(0).contains(Helper.getContext().getPackageName())) {
                EL.w("只包含自己");
                return false;
            }
        } else if (len > 1) {
            for (int i = 0; i < lastModifyTimeInfos.size(); i++) {
                EL.d(lastModifyTimeInfos.get(i));
            }
            return true;
        }

        return false;
    }

}
