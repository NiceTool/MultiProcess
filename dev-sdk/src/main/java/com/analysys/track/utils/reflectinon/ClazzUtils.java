package com.analysys.track.utils.reflectinon;

import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.ELOG;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ClazzUtils {
    public static Method getMethod(String clazzName, String methodName, Class<?>... parameterTypes) {
        return getMethod(getClass(clazzName), methodName, parameterTypes);
    }

    public static Method getMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            if (parameterTypes == null || parameterTypes.length == 0) {
                method = clazz.getDeclaredMethod(methodName);
                method.setAccessible(true);
            } else {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        if (method == null) {
            try {
                if (parameterTypes == null || parameterTypes.length == 0) {
                    method = clazz.getMethod(methodName);
                } else {
                    method = clazz.getMethod(methodName, parameterTypes);
                }
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUGLY) {
                    BuglyUtils.commitError(e);
                }
            }
        }
        return method;
    }

    public static Field getField(Class clazz, String fieldName) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        if (field == null) {
            try {
                field = clazz.getField(fieldName);
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUGLY) {
                    BuglyUtils.commitError(e);
                }
            }
        }
        return field;
    }

    public static Class getClass(String name) {
        if (TextUtils.isEmpty(name)) {
            return Object.class;
        }
        try {
            return Class.forName(name);
        } catch (Throwable e) {
            return Object.class;
        }

    }

    public static Object invokeObjectMethod(Object o, String methodName) {
        return invokeObjectMethod(o, methodName, null, null);
    }

    public static Object invokeObjectMethod(Object o, String methodName, Class<?>[] argsClass, Object[] args) {
        if (o == null || methodName == null) {
            return null;
        }
        Object returnValue = null;
        try {
            Class<?> c = o.getClass();
            Method method;
            method = getMethod(c, methodName, argsClass);
            returnValue = method.invoke(o, args);
        } catch (Throwable e) {
        }

        return returnValue;
    }


    public static Object getObjectFieldObject(Object o, String fieldName) {
        if (o == null || fieldName == null) {
            return null;
        }
        Field field = getField(o.getClass(), fieldName);
        try {
            return field.get(o);
        } catch (Throwable e) {

        }
        return null;
    }

    public static void setObjectFieldObject(Object o, String fieldName, Object value) {
        if (o == null || fieldName == null) {
            return;
        }
        Field field = getField(o.getClass(), fieldName);
        try {
            field.set(o, value);
        } catch (Throwable e) {

        }
    }

    public static Object getStaticFieldObject(Class clazz, String fieldName) {
        if (clazz == null || fieldName == null) {
            return null;
        }
        Field field = getField(clazz.getClass(), fieldName);
        try {
            return field.get(null);
        } catch (Throwable e) {

        }
        return null;
    }

    public static Object invokeStaticMethod(String clazzName, String methodName, Class<?>[] argsClass, Object[] args) {
        return invokeStaticMethod(getClass(clazzName), methodName, argsClass, args);
    }

    public static Object invokeStaticMethod(Class clazz, String methodName, Class<?>[] argsClass, Object[] args) {
        if (clazz == null || methodName == null) {
            return null;
        }
        Object returnValue = null;
        try {
            Method method;
            method = getMethod(clazz, methodName, argsClass);
            returnValue = method.invoke(null, args);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }

        return returnValue;
    }

    /**
     * 是否包含方法
     *
     * @param clazz
     * @param methodName
     * @param parameterTypes
     * @return
     */
    public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            if (clazz == null || TextUtils.isEmpty(methodName)) {
                return false;
            }
            if (parameterTypes == null || parameterTypes.length == 0) {
                return clazz.getMethod(methodName) != null;
            } else {
                return clazz.getMethod(methodName, parameterTypes) != null;
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return false;
    }

    /**
     * 获取构造函数
     *
     * @param clazzName
     * @param types
     * @param values
     * @return
     */
    public static Object getConstructor(String clazzName, Class[] types, Object[] values) {

        try {
            Constructor ctor = getDeclaredConstructor(getClass(clazzName), types);
            if (ctor != null) {
                ctor.setAccessible(true);
                return ctor.newInstance(values);
            }
        } catch (Throwable igone) {
        }
        return null;
    }

    private static Constructor getDeclaredConstructor(Class<?> clazz, Class[] types) {
        try {
            return clazz.getDeclaredConstructor(types);
        } catch (NoSuchMethodException e) {
            try {
                return clazz.getConstructor(types);
            } catch (NoSuchMethodException igone) {
            }
        }
        return null;
    }
}
