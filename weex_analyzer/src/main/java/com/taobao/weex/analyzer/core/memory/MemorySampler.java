package com.taobao.weex.analyzer.core.memory;

import android.support.annotation.WorkerThread;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/8<br/>
 * Time: 下午4:22<br/>
 */

public class MemorySampler {
    private MemorySampler() {
    }

    /**
     * 获取当前内存占用,单位是MB
     */
    public static double getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (double) 1048576;
    }

    /**
     * 获取当前应用能获取的总内存,单位是MB
     */
    public static double maxMemory() {
        return Runtime.getRuntime().maxMemory() / (double) 1048576;
    }

    /**
     * 获取当前应用总内存
     * */
    public static double totalMemory() {
        return Runtime.getRuntime().totalMemory() / (double) 1048576;
    }

    @WorkerThread
    public static void tryForceGC() {
        // System.gc() does not garbage collect every time. Runtime.gc() is
        // more likely to perfom a gc.
        Runtime.getRuntime().gc();
        enqueueReferences();
        System.runFinalization();
    }

    private static void enqueueReferences() {
        /*
         * Hack. We don't have a programmatic way to wait for the reference queue
         * daemon to move references to the appropriate queues.
         */
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new AssertionError();
        }
    }

}
