package com.sms.interceptor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by zhangfei on 2015/7/25.
 */
public class SmsService extends Service{
    private static final String TAG = "SmsService";
    private static final int CHECK_INTERVAL = 30 * 1000;
    private PendingIntent mPendIntent;
    private AlarmManager mAlarmManager;

    public static void startService(Context context){
        Intent intent = new Intent(context, SmsService.class);
        context.startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent(SmsReceiver.ACTION_CHECK_SMS_IN_LOOP);
            mPendIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAlarmManager.cancel(mPendIntent);
            scheduleCheckSms(this);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startService(this);
        Log.d(TAG, "onDestroy() and restart SmsService");
    }

    private void scheduleCheckSms(Context context) {
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(SmsReceiver.ACTION_CHECK_SMS_IN_LOOP);
        PendingIntent pendIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        int triggerAtTime = (int) (SystemClock.elapsedRealtime() + CHECK_INTERVAL);
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, CHECK_INTERVAL, pendIntent);
    }
}
