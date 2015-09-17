package com.sms.interceptor;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zhangfei on 2015/7/19.
 */
public class SmsPreference {
    private static final String PREFERENCE_FILE = "sms_intercept";

    private static final String KEY_IS_FIRST_TIME_STARTED = "is_first_time_started";
    private static final String KEY_LAST_SMS_ID = "last_sms_id";
    private static final String KEY_PHONE_TO_BE_INTERCEPTED = "phone_to_be_intercepted";
    private static final String KEY_PHONE_TO_REPORT = "phone_to_report";

    private static SharedPreferences getPrefs (Context context){
        return context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_MULTI_PROCESS);
    }

    public static boolean isFirstTimeStarted(Context context){
        boolean flag =  getPrefs(context).getBoolean(KEY_IS_FIRST_TIME_STARTED, true);
        getPrefs(context).edit().putBoolean(KEY_IS_FIRST_TIME_STARTED, true);
        return flag;
    }

    public static long getLastSmsId(Context context){
        return getPrefs(context).getLong(KEY_LAST_SMS_ID, 0l);
    }

    public static void setLastSmsId (Context context, long id) {
        getPrefs(context).edit().putLong(KEY_LAST_SMS_ID, id).commit();
    }

    public static String getPhoneToBeIntercepted(Context context){
        return getPrefs(context).getString(KEY_PHONE_TO_BE_INTERCEPTED, "");
    }

    public static void setPhoneToBeIntercepted(Context context, String mobileNo) {
        getPrefs(context).edit().putString(KEY_PHONE_TO_BE_INTERCEPTED, mobileNo).commit();
    }

    public static void setPhoneToReport(Context context, String number){
        getPrefs(context).edit().putString(KEY_PHONE_TO_REPORT, number).commit();
    }

    public static String getPhoneToReport(Context context){
        return getPrefs(context).getString(KEY_PHONE_TO_REPORT, "");
    }

}
