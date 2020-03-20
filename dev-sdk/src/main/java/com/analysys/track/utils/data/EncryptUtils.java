package com.analysys.track.utils.data;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.SystemUtils;

import java.security.MessageDigest;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static android.provider.Settings.System.AIRPLANE_MODE_ON;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 加解密工具类。
 * 用法:
 * 数据库初始化时,调用checkEncryptKey(Context context)确认之前加解密部分是否正常工作, 正常工作可以测试下DB之前数据是否正常解密,根据结果进行相关操作
 * 加密调用接口:encrypt(Context context, String str)
 * 解密调用接口:decrypt(Context context, String str)
 * @Version: 1.0
 * @Create: 2018年2月2日 上午11:50:40
 * @Author: sanbo
 */
public class EncryptUtils {
    private static final byte[] iv = {0xA, 0xB, 0xC, 0xD, 0xE, 0xF, 3, 11, 42, 9, 1, 6, 8, 33, 21, 91};
    private static final String SP_EK_ID = "track_id";
    private static final String SP_CONTENT = "track";
    private static String mEncryptKey = null;

    /**
     * 测试key是否可用
     *
     * @param context
     * @return
     */
    public static boolean checkEncryptKey(Context context) {
        try {
            String testEnKey = encrypt(context, SP_CONTENT);
            if (!TextUtils.isEmpty(testEnKey)) {
                String testDeKey = decrypt(context, testEnKey);
                if (!TextUtils.isEmpty(testDeKey)) {
                    if (SP_CONTENT.equals(testDeKey)) {
                        return true;
                    }
                }

            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return false;
    }

    public static void reInitKey(Context context) {
        if (context == null) {
            return;
        }
        clearEncryptKey(context);
        init(context);
    }

    private static void clearEncryptKey(Context context) {
        try {
            if (context == null) {
                return;
            }
            SharedPreferences pref = context.getSharedPreferences(EGContext.SPUTIL, Context.MODE_PRIVATE);
            if (pref != null) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(SP_EK_ID, "");
                editor.apply();
            }
            mEncryptKey = null;
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(t);
            }
        }
    }

    /**
     * 加密接口
     *
     * @param context
     * @param src     加密的字符串
     * @return 加密后的字符串，可能为""
     */
    public static String encrypt(Context context, String src) {
        try {
            if (TextUtils.isEmpty(mEncryptKey)) {
                init(context);
            }
            if (TextUtils.isEmpty(src)) {
                return "";
            }
            byte[] b = encrypt(src.getBytes(), mEncryptKey.getBytes());
            return Base64.encodeToString(b, Base64.DEFAULT);

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
            return "";
        }
    }

    /**
     * 解密接口
     *
     * @param context
     * @param str     待解密的字符串
     * @return 解密后的字符串
     */
    public static String decrypt(Context context, String str) {
        try {

            if (TextUtils.isEmpty(mEncryptKey)) {
                init(context);
            }
            if (TextUtils.isEmpty(str)) {
                return "";
            }
            byte[] b = Base64.decode(str.getBytes(), Base64.DEFAULT);
            return new String(decrypt(b, mEncryptKey.getBytes()));
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
            return "";
        }
    }

    private static boolean canGetAndroidId(Context context) {
        try {
            if (isAirplaneModeOn(context)) {
                return false;
            } else {
                return !TextUtils
                        .isEmpty(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return false;
    }

    /**
     * 初始化秘钥key,检验确保可用.
     *
     * @param context
     */
    @SuppressWarnings("deprecation")
    public static void init(Context context) {
        try {
            // 1.获取本地参考key
            SharedPreferences pref = context.getSharedPreferences(EGContext.SPUTIL, Context.MODE_PRIVATE);
            String id = pref.getString(SP_EK_ID, "");

            // 2.参考key异常则重新生成
            if (TextUtils.isEmpty(id) || !id.contains("|")) {
                String preID = null;
                if (canGetAndroidId(context)) {
                    preID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                }
                String serialNO = SystemUtils.getSerialNumber();
                // 确保preID非空
                if (TextUtils.isEmpty(preID)) {
                    String uuid = String.valueOf(UUID.randomUUID());
                    if (uuid.contains("-")) {
                        String[] s = uuid.split("-");
                        if (s.length > 0) {
                            preID = s[s.length - 1];
                        }
                    } else {
                        preID = uuid;
                    }
                }
                // 确保serialNO非空
                if (TextUtils.isEmpty(serialNO)) {
                    serialNO = String.valueOf(System.currentTimeMillis() / 10000);
                }
                id = preID + "|" + serialNO;
                pref.edit().putString(SP_EK_ID, id).apply();
            }

            // 3.确保参考key的情况下，生成秘钥
            if (!TextUtils.isEmpty(id)) {
                String[] ss = id.split("\\|");
                String preID = ss[0];
                String fID = ss[1];
                int firstKey = 2;

                try {
                    firstKey = ensure(preID.charAt(2));
                } catch (Throwable e) {
                    try {
                        firstKey = ensure(preID.charAt(2));
                    } catch (Throwable ee) {
                        firstKey = 3;
                    }
                }

                int endKey = 5;
                try {
                    endKey = ensure(fID.charAt(2));
                } catch (NumberFormatException e) {
                    if (BuildConfig.ENABLE_BUG_REPORT) {
                        BugReportForTest.commitError(e);
                    }
                    try {
                        endKey = ensure(fID.charAt(3));
                    } catch (NumberFormatException ee) {
                        if (BuildConfig.ENABLE_BUG_REPORT) {
                            BugReportForTest.commitError(ee);
                        }
                        endKey = 6;
                    }
                }

                int start = 0;
                if (firstKey < 6) {
                    start = firstKey;
                } else {
                    start = ensureLowFive(firstKey);
                }

                int dur = 0;
                if (endKey < 6) {
                    dur = endKey;
                } else {
                    dur = ensureLowFive(endKey);
                }
                mEncryptKey = md5(SP_CONTENT + id.substring(start, (start + dur)));
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    /**
     * 确保小于10
     *
     * @param key 大于5的数字
     * @return 小于10的值
     */
    private static int ensureLowFive(int key) {
        ;
        int temp = Math.abs(key - 10);
        if (temp > 5) {
            return ensureLowFive(temp);
        } else {
            return temp;
        }
    }

    /**
     * 确定char是数字
     *
     * @param key
     * @return 返回小于10的数字
     */
    private static int ensure(char key) throws NumberFormatException {
        return Integer.parseInt(Character.toString(key));
    }

    private static String md5(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(str.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (Exception e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return "";
    }

    @SuppressLint("TrulyRandom")
    private static byte[] encrypt(byte[] input, byte[] password) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        SecretKeySpec keySpec = new SecretKeySpec(password, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(input);
    }

    private static byte[] decrypt(byte[] input, byte[] password) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        SecretKeySpec keySpec = new SecretKeySpec(password, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(input);
    }

    @SuppressWarnings("deprecation")
    private static boolean isAirplaneModeOn(Context context) {
        try {
            if (context == null) {
                return false;
            }
            ContentResolver contentResolver = context.getContentResolver();
            if (contentResolver != null) {
                return Settings.System.getInt(contentResolver, AIRPLANE_MODE_ON, 0) != 0;
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(t);
            }
        }
        return false;
    }

}
