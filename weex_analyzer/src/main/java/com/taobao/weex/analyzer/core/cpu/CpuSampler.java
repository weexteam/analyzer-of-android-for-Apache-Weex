package com.taobao.weex.analyzer.core.cpu;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Description: <p> Created by rowandjj(chuyi)<br/> Date: 2016/11/7<br/> Time: 下午4:14<br/>
 */

class CpuSampler {

    private CpuSampler(){}
    static String sampleCpuRate() {
        return doSample("/proc/stat");
    }

    static String samplePidCpuRate() {
        int pid = android.os.Process.myPid();
        return doSample("/proc/" + pid + "/stat");
    }

    @VisibleForTesting
    static String doSample(@NonNull String filename) {
        BufferedReader cpuReader = null;
        String cpuRate = "";
        try {
            cpuReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)), 1024);
            cpuRate = cpuReader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cpuReader != null) {
                    cpuReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return cpuRate;
    }
}
