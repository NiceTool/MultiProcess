package com.analysys.plugin;

import android.os.SystemClock;
import android.util.Log;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class TimePrint {
    private static HashMap<String, InnerClass> map = new HashMap<>();

    public static void start(String name) {
        InnerClass data = map.get(name);

        if (data == null || data.time == null) {
            map.put(name, new InnerClass(SystemClock.currentThreadTimeMillis()));
            return;
        }

        data.integer.incrementAndGet();

    }

    public static void end(String name) {
        InnerClass data = map.get(name);
        if (data == null || data.time == null) {
            Log.d("TimePrint", name + " <-- " + "not has data !");
            return;
        }

        if (data.integer.decrementAndGet() <= 0) {
            map.remove(name);
            long time = SystemClock.currentThreadTimeMillis() - data.time;
            if (time <= 30) {
                return;
            }

            String threadName = "[Thread:" + Thread.currentThread().getName() + "]";
            if (time <= 100) {
                Log.d("TimePrint", threadName + name + " <-- " + time);
            } else if (time <= 300) {
                Log.i("TimePrint", threadName + name + " <-- " + time);
            } else if (time <= 500) {
                Log.w("TimePrint", threadName + name + " <-- " + time);
            } else {
                Log.e("TimePrint", threadName + name + " <-- " + time);
            }
        }
    }

    static class InnerClass {
        AtomicInteger integer;
        Long time;

        public InnerClass(Long time) {
            this.integer = new AtomicInteger(1);
            this.time = time;
        }
    }
}
