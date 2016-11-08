package com.taobao.weex.analyzer.core;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Description:
 * <p>
 * Created by rowandjj(chuyi)<br/>
 * Date: 2016/11/7<br/>
 * Time: 下午4:14<br/>
 */

public class CpuSampler {

    public static String sampleCpuRate() {
        BufferedReader cpuReader = null;
        String cpuRate = "";
        try {
            cpuReader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/stat")), 1024);
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

    public static String samplePidCpuRate() {
        BufferedReader pidReader = null;
        String cpuRate = "";
        try {
            int pid = android.os.Process.myPid();
            pidReader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/" + pid + "/stat")), 1024);
            cpuRate = pidReader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pidReader != null) {
                    pidReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return cpuRate;
    }
}
