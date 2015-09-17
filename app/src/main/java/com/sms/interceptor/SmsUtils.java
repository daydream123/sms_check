package com.sms.interceptor;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.List;

/**
 * Created by zhangfei on 2015/7/26.
 */
public class SmsUtils {
    private static final String TAG = "SmsUtils";
    public static final String CONFIG_MSG_PREFIX = "请求配置短信拦截号码=";
    public static final String CONFIG_OK_MSG = "短信拦截已配置好了";

    private static String[] COLUMNS = new String[]{
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
    };

    public static void saveLatestSmsId(Context context) {
        Cursor cursor = context.getContentResolver().query(
                Uri.parse("content://sms/inbox"),
                COLUMNS, null, null,
                Telephony.Sms._ID + " DESC");

        try {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(0);
                Log.d(TAG, "savedLatestSmsId:" + id);
                SmsPreference.setLastSmsId(context, id);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean isEquals(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return false;
        }
        str1 = str1.replace("+86", "").replaceAll("\\s*", "").replaceAll("-", "");
        str2 = str2.replace("+86", "").replaceAll("\\s*", "").replaceAll("-", "");
        return str1.equals(str2);

    }

    public static void sendSms(String body, String receiver) {
        SmsManager smsMgr = SmsManager.getDefault();
        List<String> contents = smsMgr.divideMessage(body);
        for (String text : contents) {
            smsMgr.sendTextMessage(receiver, null, text, null, null);
            Log.d(TAG, "handleSms:" + text);
        }
    }

    public static void deleteSms(Context context, String where, String[] whereArgs) {
        context.getContentResolver().delete(Uri.parse("content://sms"), where, whereArgs);
    }
}
