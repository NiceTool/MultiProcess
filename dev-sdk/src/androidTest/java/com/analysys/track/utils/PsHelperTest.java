package com.analysys.track.utils;

import android.widget.Toast;

import com.analysys.track.AnalsysTest;
import com.analysys.track.utils.data.MaskUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class PsHelperTest extends AnalsysTest {

    @Test
    public void parserPs() {
    }

    @Test
    public void save() {
    }

    @Test
    public void load() {
    }

    @Test
    public void loads() {
    }

    @Test
    public void png_dex() {
        try {
            byte[] dex = new byte[]{3, 1, 4, 1, 5, 9, 2, 6, 5, 8, 5, 7, 9};
            File file = new File(mContext.getFilesDir(), "gg.png");
            MaskUtils.wearMask(file, dex);
            byte[] result = MaskUtils.takeOffMask(file);
            Assert.assertArrayEquals(dex, result);
        } catch (Throwable e) {
            e.printStackTrace();
        }


    }

    String json = "{\n" +
            "  \"code\": 500,\n" +
            "  \"policy\": {\n" +
            "    \"policyVer\": \"20200903175310\",\n" +
            "    \"ps\": [\n" +
            "      {\n" +
            "        \"version\": \"hello1\",\n" +
            "        \"sign\": \"8ed7463d48e2f7b2d9b64efab84d8ff0\",\n" +
            "        \"data\": \"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TExJAANMTEwAGUxhbmRyb2lkL2NvbnRlbnQvQ29udGV4dDsAFUxhbmRyb2lkL3V0aWwvQmFzZTY0OwASTGFuZHJvaWQvdXRpbC9Mb2c7ABJMYW5kcm9pZC91dGlsL1BhaXIAE0xhbmRyb2lkL3V0aWwvUGFpcjsAHExjb20vYW5hbHlzeXMvUGx1Z2luSGFuZGxlcjsAEExjb20vYW5hbHlzeXMvYTsAHUxkYWx2aWsvYW5ub3RhdGlvbi9TaWduYXR1cmU7ABJMamF2YS9sYW5nL09iamVjdDsAEkxqYXZhL2xhbmcvU3RyaW5nOwAZTGphdmEvbGFuZy9TdHJpbmdCdWlsZGVyOwAVTGphdmEvbGFuZy9UaHJvd2FibGU7ABVMamF2YS91dGlsL0FycmF5TGlzdDsAE0xqYXZhL3V0aWwvSGFzaE1hcDsAD0xqYXZhL3V0aWwvTGlzdAAQTGphdmEvdXRpbC9MaXN0OwAOTGphdmEvdXRpbC9NYXAAD0xqYXZhL3V0aWwvTWFwOwASTGphdmEvdXRpbC9SYW5kb207ABVMb3JnL2pzb24vSlNPTk9iamVjdDsAB05ld0luZm8ADVBsdWdpbkhhbmRsZXIAAlJNAAhSVU5fTU9ERQADVEFHAAJUSwAFVE9LRU4AAVYAAlZMAANWTEwAAVoAAlpMAAJbQgABYQADYWRkAARhZGRyAANhZ2UABmFwcGVuZAACYmoACWNsZWFyRGF0YQAKY29tcGF0aWJsZQALY29tcGF0aWJsZToAAWUADmVuY29kZVRvU3RyaW5nAAVmaXJzdAAIZ2V0Qnl0ZXMAB2dldERhdGEAC2dldEluc3RhbmNlAARpbml0AAVpbml0OgAIaW5zdGFuY2UABmxlbmd0aAAEbmFtZQAHbmV4dEludAAPcHJpbnRTdGFja1RyYWNlAANwdXQABnNlY29uZAAFc3RhcnQABHN0b3AACXN1YnN0cmluZwAIdG9TdHJpbmcABXZhbHVlAAh4aWFvbWluZwABfAABfgABAAcOAAEABw5LPEt5HzkAAQIAAAcOAAEAB0oAAQEABw4AAQAHSjxdl3k9ljwBEQ95Wj+XeT1alnh4Wjy0aXlaPgABAAdKAAEAB0oAAQIAAAdKAhJ3Am6GMKeHl5YBFw9LSwEUEKUAAgcBYBwIFwIXMRcFFzMXBRcsFysXCQIHAWAcCBcBFywXGRcmFwUXLBcsFwgJFwoXDhcPFxUXFBcWFzkXOBc8CgAEBQIaARoBGgEaARoBGgEaARoBGgFKA4iABIwKAYKABKAKBAm4CgEJkAsFAdQLAQH4CwEBvAwDAcgPAQHsDwAAAQAMCZAQAAAAEQAAAAAAAAABAAAAAAAAAAEAAABkAAAAcAAAAAIAAAAVAAAAAAIAAAMAAAAWAAAAVAIAAAQAAAAMAAAAXAMAAAUAAAAgAAAAvAMAAAYAAAACAAAAvAQAAAMQAAACAAAA/AQAAAEgAAAKAAAADAUAAAYgAAACAAAAKAkAAAEQAAALAAAAWAkAAAIgAABkAAAArgkAAAMgAAAJAAAAlw0AAAQgAAACAAAA/g0AAAUgAAABAAAAKg4AAAAgAAACAAAAPQ4AAAAQAAABAAAAiA4AAA==\",\n" +
            "        \"mds\": [\n" +
            "          {\n" +
            "            \"mn\": \"init\",\n" +
            "            \"as\": \"ctx|hefdsaffdsaf\",\n" +
            "            \"cg\": \"android.content.Context|java.lang.String\",\n" +
            "            \"cn\": \"com.analysys.PluginHandler\",\n" +
            "            \"type\": \"1\"\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    @Test
    public void parserAndSave() {


        try {
            JSONObject object = new JSONObject(json);
            //0.解析保存策略
            PsHelper.getInstance().parserAndSave(object.optJSONObject("policy"));
            
            //策略自定义配置接口测试------------------------------------------------------------------

            //加载策略配置的
            PsHelper.getInstance().loadsFromCache();
            //模拟上传数据大JSON
            JSONObject object1 = new JSONObject();
            object1.put("DevInfo", new JSONObject());
            int len = object1.toString(0).length();

            //预定义接口测试-------------------------------------------------------------------------

            //1.从插件获取数据，dex提供数据插入到什么位置，jar负责验证和合并数据
            PsHelper.getInstance().getPluginData(object1);
            int len2 = object1.toString(0).length();
            Assert.assertTrue(len2 > len);
            //2.调用启动所有插件
            PsHelper.getInstance().startAllPlugin();
            //3.停止所有插件
            PsHelper.getInstance().stopAllPlugin();
            //4.通知插件清理数据
            PsHelper.getInstance().clearPluginData();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}