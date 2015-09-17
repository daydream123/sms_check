package com.sms.interceptor;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by zhangfei on 2015/7/24.
 */
public class Config {
    private static final String INTERCEPT_NUMBER = "拦截手机";
    private static final String REPORT_NUMBER = "汇报手机";
    private static final String CHECK_INTERVAL = "检测间隔";

    private static final String MINUTE = "分钟";

    private static final String CONFIG_FILE = "config.txt";

    public static ConfigInfo getConfigInfo(Context context){
        if (isSDCardAvailable(context)) {
            String configPath = Environment.getExternalStorageDirectory() + "/Android/" + CONFIG_FILE;
            try {
                ConfigInfo info = new ConfigInfo();
                BufferedReader reader = new BufferedReader(new FileReader(new File(configPath)));
                String temp;
                while ((temp = reader.readLine()) != null) {
                    int index = temp.indexOf("=");
                    if (temp.startsWith(INTERCEPT_NUMBER)) {
                        info.setPhoneNumber(temp.substring(index + 1));
                    } else if (temp.startsWith(REPORT_NUMBER)) {
                        info.setReportNumber(temp.substring(index + 1));
                    } else if (temp.startsWith(CHECK_INTERVAL)) {
                        String interval = temp.substring(index + 1);

                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static boolean isSDCardAvailable(final Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (context.getExternalCacheDir() != null) {
                return true;
            }
        }
        return false;
    }

    public static class ConfigInfo {
        private String phoneNumber;
        private String reportNumber;
        private int checkInterval;

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getReportNumber() {
            return reportNumber;
        }

        public void setReportNumber(String reportNumber) {
            this.reportNumber = reportNumber;
        }

        public int getCheckInterval() {
            return checkInterval;
        }

        public void setCheckInterval(int checkInterval) {
            this.checkInterval = checkInterval;
        }
    }

}
