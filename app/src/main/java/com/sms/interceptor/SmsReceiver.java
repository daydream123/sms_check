package com.sms.interceptor;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by zhangfei on 2015/7/18.
 */
public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    public static final String ACTION_CHECK_SMS_IN_LOOP = "com.sms.action.CHECK_SMS_IN_LOOP";

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            SmsService.startService(context);
        } else if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(action)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return;
            }
            EmailUtils.checkSmsInLoop(context);
        } else if (ACTION_CHECK_SMS_IN_LOOP.equals(action)) {
            Log.d("sms", "check in loop");
            EmailUtils.checkSmsInLoop(context);
        } else if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            SmsService.startService(context);
        } else {
            // 如果是来电
            TelephonyManager tManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            switch (tManager.getCallState()) {
                case TelephonyManager.CALL_STATE_RINGING:
                    SmsService.startService(context);
                    break;
            }
        }
    }



}