package com.analysys.track.utils;

import android.content.Context;

import com.analysys.track.internal.impl.DeviceImpl;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.sp.SPHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class DeflterCompressUtils {

    /**
     * @param data
     * @return byte[]
     */
    public static byte[] compress(byte[] data) {
        byte[] output = new byte[0];

        Deflater compresser = new Deflater();

        compresser.reset();
        compresser.setInput(data);
        compresser.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!compresser.finished()) {
                int i = compresser.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
        } catch (Exception e) {
            output = data;
            ELOG.e(e.getMessage()+"  compress has an exception.");
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
            }
        }
        compresser.end();
        return output;
    }
    public static String makeSercretKey(String value , Context ctx) {
        StringBuilder sb = new StringBuilder();
        if(value.length() > 3){
            SPHelper.getDefault(ctx).edit().putString(EGContext.APPKEY ,value).commit();
            value = value.substring(0,3);
        }
        String sdkv = DeviceImpl.getInstance(ctx).getSdkVersion();
        SPHelper.getDefault(ctx).edit().putString(EGContext.SDKV , sdkv).commit();
        if(sdkv.contains("|")){
            sdkv = sdkv.substring(0,sdkv.indexOf("|")).replace(".", "");
        }else{
            sdkv = sdkv.replace(".", "");
        }
        sb.append(sdkv);//版本号-主版本号去掉点---规则变动，不需要处理了

        sb.append(DeviceImpl.getInstance(ctx).getDebug());//是否debug模式，0/1值
        sb.append(value);//前三位
        long time = System.currentTimeMillis();
        SPHelper.getDefault(ctx).edit().putString(EGContext.TIME , String.valueOf(time)).commit();
        //"1548155536157"
        sb.append(time);
        ELOG.i(sb.toString()+" ::::::::::::key");
       return Md5Utils.getMD5(sb.toString());
    }
    /**
     * Deflater 解压数据
     */
    public static byte[] decompress(byte[] data) {
        if (data == null) {
            return new byte[0];
        }


        byte[] output = new byte[0];

        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
        } finally {
            try {
                o.close();
            } catch (IOException e) {
            }
        }

        decompresser.end();
        return output;
    }
}
