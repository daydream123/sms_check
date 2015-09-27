package com.sms.interceptor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sms.R;

/**
 * Created by zhangfei on 15/9/20.
 */
public class RegisteredActivity extends Activity{

    private TextView mPhoneNumText;
    private TextView mIdNoText;
    private TextView mBankCardText;
    private Button mLogoutBtn;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        setContentView(R.layout.registered_activity);
        mPhoneNumText = (TextView) findViewById(R.id.tv_phone);
        mIdNoText = (TextView) findViewById(R.id.tv_id_no);

        mPhoneNumText.setText(PreferenceUtils.getPhoneNumber(this));
        mIdNoText.setText(PreferenceUtils.getIdNo(this));

        mBankCardText = (TextView) findViewById(R.id.tv_bank_card);

        String bankCard = PreferenceUtils.getBankCard(this);
        bankCard = bankCard.substring(0, 4) + "********" + bankCard.substring(bankCard.length() - 4, bankCard.length());
        mBankCardText.setText(bankCard);

        mLogoutBtn = (Button) findViewById(R.id.btn_logout);
        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceUtils.setBankCard(mContext, "");
                PreferenceUtils.setIdNo(mContext, "");
                PreferenceUtils.setPhoneNumber(mContext, "");
                PreferenceUtils.setRegistered(mContext, false);

                Intent intent = new Intent(mContext, UnregisteredActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
