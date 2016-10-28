package com.taobao.weex.analyzer.core;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 16/10/8<br/>
 * Time: 下午4:22<br/>
 */

public class MemoryChecker {
    private MemoryChecker() {
    }

    /**
     * 获取当前内存占用,单位是MB
     */
    public static double checkMemoryUsage() {
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

}
