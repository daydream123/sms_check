package com.sms.interceptor;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.List;

public class UiUtils {
    private static Handler sMainThreadHandler;
    private static long sLastClickTime;

    /**
     * @return a {@link Handler} tied to the main thread.
     */
    public static Handler getMainThreadHandler() {
        if (sMainThreadHandler == null) {
            // No need to synchronize -- it's okay to create an extra Handler,
            // which will be used
            // only once and then thrown away.
            sMainThreadHandler = new Handler(Looper.getMainLooper());
        }
        return sMainThreadHandler;
    }

    /**
     * A thread safe way to show a Toast. Can be called from any thread.
     *
     * @param context application  context
     * @param resId   Resource ID of the message string.
     */
    public static void showToast(Context context, int resId) {
        showToast(context, context.getResources().getString(resId));
    }

    /**
     * A thread safe way to show a Toast. Can be called from any thread.
     *
     * @param context application   context
     * @param message Message to show.
     */
    public static void showToast(final Context context, final String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }

        getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Generics version of {@link Activity#findViewById}
     */
    @SuppressWarnings("unchecked")
    public static <T extends View> T getViewOrNull(Activity parent, int viewId) {
        return (T) parent.findViewById(viewId);
    }

    /**
     * Generics version of {@link View#findViewById}
     */
    @SuppressWarnings("unchecked")
    public static <T extends View> T getViewOrNull(View parent, int viewId) {
        return (T) parent.findViewById(viewId);
    }

    /**
     * Same as {@link Activity#findViewById}, but crashes if there's no view.
     */
    @SuppressWarnings("unchecked")
    public static <T extends View> T getView(Activity parent, int viewId) {
        return (T) checkView(parent.findViewById(viewId));
    }

    /**
     * Same as {@link View#findViewById}, but crashes if there's no view.
     */
    @SuppressWarnings("unchecked")
    public static <T extends View> T getView(View parent, int viewId) {
        return (T) checkView(parent.findViewById(viewId));
    }

    private static View checkView(View v) {
        if (v == null) {
            throw new IllegalArgumentException("View doesn't exist");
        }
        return v;
    }

    /**
     * check whether device is tablet or phone
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * At phone platform, set the phone screen is portrait; otherwise landscape.
     */
    public static void setRequestOrizentation(Activity activity) {
        if (isTablet(activity)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR | ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR | ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * Check App is running and can be seen.
     */
    public static boolean isAppInForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                return appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
            }
        }
        return false;
    }

    /**
     * Check android service component is running in background.
     */
    public static boolean isServiceRunning(Context context, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> serviceList = activityManager.getRunningServices(30);

        if (!(serviceList.size() > 0)) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    /**
     * Check activity is opened even it was onPause() status.
     */
    public static boolean isActivityOpened(Context context, String componentName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> taskInfos = activityManager.getRunningTasks(1);
        if (taskInfos != null && taskInfos.size() > 0) {
            RunningTaskInfo taskInfo = taskInfos.get(0);
            if (taskInfo.topActivity.getClassName().equals(componentName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check screen is on or off.
     */
    public static boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }

    /**
     * Check user double clicked button, if true should do nothing,
     * otherwise do things you want to do.
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - sLastClickTime;
        if (0 < timeD && timeD < 1000) {
            return true;
        }
        sLastClickTime = time;
        return false;
    }

    @TargetApi(VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void dimSystemBarCan(Activity activity, boolean dim) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
            if (dim) {
                decorView.setSystemUiVisibility(uiOptions);
            } else {
                // Calling setSystemUiVisibility() with a value of 0 to clears all flags.
                decorView.setSystemUiVisibility(0);
            }
        }
    }

    /**
     * Hidden system status bar, you can also do by set style in Manifest for activity like below:
     * <p/>
     * <p>If you hide the system bars in your activity's onCreate() method and the user presses Home,
     * the system bars will reappear. When the user reopens the activity, onCreate() won't get called,
     * so the system bars will remain visible. If you want system UI changes to persist as the user
     * navigates in and out of your activity, set UI flags in onResume() or onWindowFocusChanged().
     * <p/>
     * <pre>
     * android:theme="@android:style/Theme.Holo.NoActionBar.Fullscreen"
     * </pre>
     *
     * @param activity
     * @param hide
     */
    @TargetApi(VERSION_CODES.JELLY_BEAN)
    public static void hideStatusBar(Activity activity, boolean hide) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            View decorView = activity.getWindow().getDecorView();
            ActionBar actionBar = activity.getActionBar();
            if (hide) {
                // Hide the status bar and action bar
                int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
                actionBar.hide();
            } else {
                // Show the status bar and action bar
                decorView.setSystemUiVisibility(0);
                actionBar.show();
            }
        } else {
            if (hide) {
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN &
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
    }

    /**
     * Hide both the navigation bar and the status bar.
     * <p/>
     * <p>SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
     * a general rule, you should design your app to hide the status bar whenever you
     * hide the navigation bar.
     * <p/>
     * <p>Note: If you hide the system bars in your activity's onCreate() method and the
     * user presses Home, the system bars will reappear. When the user reopens the activity,
     * onCreate() won't get called, so the system bars will remain visible. If you want system
     * UI changes to persist as the user navigates in and out of your activity, set UI flags in
     * onResume() or onWindowFocusChanged().
     *
     * @param activity         Activity instance
     * @param keepLayoutStable if true, when layout content will appear behind the navigation bar.
     */
    @TargetApi(VERSION_CODES.JELLY_BEAN)
    public static void hideNavigationBar(Activity activity, boolean hide, boolean keepLayoutStable) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            if (hide) {
                if (keepLayoutStable) {
                    activity.getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                } else {
                    activity.getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
                }
            } else {
                activity.getWindow().getDecorView().setSystemUiVisibility(0);
            }
        }
    }

    /**
     * Hide both the navigation bar and the status bar but layout content can resize when switch state,
     * about detail please refer  {@link #hideNavigationBar(Activity, boolean, boolean)}
     *
     * @param activity activity instance
     * @param hide     whether hide navigation bar
     */
    public static void hideNavigationBar(Activity activity, boolean hide) {
        hideNavigationBar(activity, hide, false);
    }

    /**
     * Set the IMMERSIVE flag.<br>
     * Set the content to appear under the system bars so that the content doesn't resize when the system bars hide and show.
     *
     * @param activity
     * @param stickMode if true navigation bar disappear after user interaction.
     */
    @TargetApi(VERSION_CODES.KITKAT)
    private void setImmersiveMode(Activity activity, boolean immersive, boolean stickMode) {
        if (immersive) {
            if (stickMode) {
                activity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                activity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
            }
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    public static int getStatusBarHeight(Activity activity) {
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    public static int getActionBarHeight(Activity activity) {
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        int contentHeight = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        return contentHeight - statusBarHeight;
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static void setBackgroundResource(View view, int backgroundResource){
        // retrieve padding first
        int left = view.getPaddingLeft();
        int top = view.getPaddingTop();
        int right = view.getPaddingRight();
        int bottom = view.getPaddingBottom();

        view.setBackgroundResource(backgroundResource);

        // set padding again
        view.setPadding(left, top, right, bottom);
    }

    public static void setActionBarVisibility(Activity activity, boolean visible) {
        try {
            int resId;
            if (Build.VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
                resId = activity.getResources().getIdentifier(
                        "abs__action_bar_container",
                        "id",
                        activity.getPackageName());
            } else {
                resId = Resources.getSystem().getIdentifier("action_bar_container", "id", "android");
            }
            if (resId != 0) {
                activity.findViewById(resId).setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
