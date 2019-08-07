package com.analysys.track.utils.reflectinon;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.ELOG;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 热更使用类
 * @Version: 1.0
 * @Create: 2019-07-27 16:13:28
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class PatchHelper {

    public static void load(Context context, File file, String className, String methodName)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
            return;
        }

//        L.info(context, "extractFile:" + file.getAbsolutePath());
        String dexpath = file.getPath();
//        L.info(context, "dexpath:" + dexpath);

        // 0 表示Context.MODE_PRIVATE
        File fileRelease = context.getDir("dex", 0);
//        L.info(context, "fileRelease:" + fileRelease);
        DexClassLoader classLoader = new DexClassLoader(dexpath, fileRelease.getAbsolutePath(), null,
                context.getClassLoader());
//        L.info(context, "classLoader:" + classLoader);
        Class<?> c = classLoader.loadClass(className);
//        L.info(context, "c:" + c.getName());

//        Method[] ms = c.getMethods();
//        for (Method m : ms) {
//            L.info(context, m.toString());
//        }
//        L.info(context, "c m:" + c.getName());
        Method p = c.getMethod(methodName);
        p.invoke(null);
    }

    public static void loads(Context context, File file) {
        try {
//  PatchHelper.loadStatic(context, file, "com.analysys.Ab", "init", new Class[]{Context.class, String.class, String.class}, new Object[]{context, "cdate004", "cdate004"});
            loadStatic(context, file, "com.analysys.Ab", "init", new Class[]{Context.class},
                    new Object[]{context});
        } catch (Throwable e) {
        }
    }

    public static void loadStatic(Context context, File file, String className, String methodName, Class[] pareTyples,
                                  Object[] pareVaules) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("inside loadStatic. will load [%s.%s]", className, methodName);
        }
//        L.info(context, "inside load static......");
        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
            return;
        }
//        L.info(context, "extractFile:" + file.getAbsolutePath());
        String dexpath = file.getPath();
//        L.info(context, "dexpath:" + dexpath);
        // 0 表示Context.MODE_PRIVATE
        File fileRelease = context.getDir("dex", 0);
//        L.info(context, "fileRelease:" + fileRelease);
        DexClassLoader classLoader = new DexClassLoader(dexpath, fileRelease.getAbsolutePath(), null,
                context.getClassLoader());
//        L.info(context, "classLoader:" + classLoader);
        Class<?> c = classLoader.loadClass(className);
//        L.info(context, "c:" + c.getName());

//        Method[] ms = c.getMethods();
//        for (Method m : ms) {
//            L.info(context, m.toString());
//        }
//        L.info(context, "c m:" + c.getName());

//        Method method = c.getMethod(methodName, pareTyples); // 在指定类中获取指定的方法
        Method method = c.getDeclaredMethod(methodName, pareTyples); // 在指定类中获取指定的方法
        if (method != null) {
            method.setAccessible(true);
            method.invoke(null, pareVaules);
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(" loadStatic over......");
        }
    }


    /**
     * 调用非静态方法、使用的是空构造
     *
     * @param context
     * @param file
     * @param className
     * @param methodName
     * @param pareTyples
     * @param pareVaules
     * @throws InvocationTargetException
     */
    public static void load(Context context, File file, String className, String methodName, Class[] pareTyples,
                            Object[] pareVaules) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, InstantiationException {
        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
            return;
        }

//        L.info(context, "extractFile:" + file.getAbsolutePath());
        String dexpath = file.getPath();
//        L.info(context, "dexpath:" + dexpath);

        // 0 表示Context.MODE_PRIVATE
        File fileRelease = context.getDir("dex", 0);
//        L.info(context, "fileRelease:" + fileRelease);
        DexClassLoader classLoader = new DexClassLoader(dexpath, fileRelease.getAbsolutePath(), null,
                context.getClassLoader());
//        L.info(context, "classLoader:" + classLoader);
        Class<?> c = classLoader.loadClass(className);

        Constructor ctor = c.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object obj = ctor.newInstance();
//        L.info(context, "c:" + c.getName());

//        Method[] ms = c.getMethods();
//        for (Method m : ms) {
//            L.info(context, m.toString());
//        }
//        L.info(context, "c m:" + c.getName());

        Method method = c.getMethod(methodName, pareTyples); // 在指定类中获取指定的方法
        method.setAccessible(true);
        method.invoke(obj, pareVaules);

    }
}
