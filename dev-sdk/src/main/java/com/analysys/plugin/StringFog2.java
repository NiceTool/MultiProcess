package com.analysys.plugin;

import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;

import java.io.UnsupportedEncodingException;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 字符串混淆用的, 这个类在打包的时候,会通过ASM插桩到代码中
 * @Version: 1.0
 * @Create: 2019-11-06 11:38:19
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class StringFog2 {

    /**
     * 加密的key,应该跟插件中的key一致,不然会发生错误
     */
    public static final String key = "VBgIAFV";
    public static final StringFogImpl FOG = new StringFogImpl();

    public static String encrypt(String data) {
        return FOG.encrypt(data, StringFog2.key);
    }

    public static String decrypt(String data) {
        return FOG.decrypt(data, StringFog2.key);
    }

    public static boolean overflow(String data) {
        return FOG.overflow(data, StringFog2.key);
    }

    /**
     * @Copyright 2019 analysys Inc. All rights reserved.
     * @Description: 字符串混淆实现类
     * @Version: 1.0
     * @Create: 2019-11-06 11:40:04
     * @author: miqt
     * @mail: miqingtang@analysys.com.cn
     */
    public final static class StringFogImpl implements IStringFog {

        private static final String CHARSET_NAME_UTF_8 = "UTF-8";

        @Override
        public String encrypt(String data, String key) {
            String newData = "";
            try {
                try {
                    newData = new String(Base64.encode(xor(data.getBytes(CHARSET_NAME_UTF_8), key), Base64.NO_WRAP));
                } catch (UnsupportedEncodingException e) {
                    newData = new String(Base64.encode(xor(data.getBytes(), key), Base64.NO_WRAP));
                }
                if (EGContext.FLAG_DEBUG_INNER) {
                    Log.d(BuildConfig.tag_stringfog, "[key=" + key + "][" + data + "]-->[" + newData + "]");
                }
            } catch (Throwable e) {
            }
            return newData;
        }

        @Override
        public String decrypt(String data, String key) {
            String newData = "";
            try {
                try {
                    newData = new String(xor(Base64.decode(data, Base64.NO_WRAP), key), CHARSET_NAME_UTF_8);
                } catch (UnsupportedEncodingException e) {
                    newData = new String(xor(Base64.decode(data, Base64.NO_WRAP), key));
                }
                if (EGContext.FLAG_DEBUG_INNER) {
                    Log.d(BuildConfig.tag_stringfog + "2", "[key=" + key + "][" + data + "]-->[" + newData + "]");
                }
            } catch (Throwable e) {
            }

            return newData;
        }

        @Override
        public boolean overflow(String data, String key) {
            return data != null && data.length() * 4 / 3 >= 65535;
        }

        public byte[] xor(byte[] data, String key) {
            int len = data.length;
            int lenKey = key.length();
            int i = 0;
            int j = 0;
            while (i < len) {
                if (j >= lenKey) {
                    j = 0;
                }
                data[i] = (byte) (data[i] ^ key.charAt(j));
                i++;
                j++;
            }
            return data;
        }

    }

}
