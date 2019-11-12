package com.analysys.track.utils;

import com.analysys.track.BuildConfig;

import java.io.Closeable;
import java.net.HttpURLConnection;

public class StreamerUtils {

    public static void safeClose(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUGLY) {
                    BuglyUtils.commitError(e);
                }
            }
        }
    }

    public static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUGLY) {
                    BuglyUtils.commitError(e);
                }
            }
        }
    }

    public static void safeClose(HttpURLConnection connection) {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }

    }

    public static void safeClose(Process proc) {
        if (proc != null) {
            try {
                proc.exitValue();
            } catch (Throwable t) {
                if (BuildConfig.ENABLE_BUGLY) {
                    BuglyUtils.commitError(t);
                }
//                proc.destroy();
            }
            proc = null;

        }

    }

    public static void safeClose(ProcessBuilder pb) {
        if (pb != null) {
            pb.directory();
            pb = null;
        }

    }

}
