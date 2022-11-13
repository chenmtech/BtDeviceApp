package com.cmtech.android.bledeviceapp.activity;


import static com.cmtech.android.bledeviceapp.activity.SignUpActivity.checkPassword;
import static com.cmtech.android.bledeviceapp.activity.SignUpActivity.checkUserName;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_SUCCESS;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.mob.MobSDK;

import java.util.Locale;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 *
 * ClassName:      ResetPasswordActivity
 * Description:    重置密码界面Activity
 * Author:         chenm
 * CreateDate:     2018/10/27 09:18
 * UpdateUser:     chenm
 * UpdateDate:     2019-04-24 09:18
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class ResetPasswordActivity extends AppCompatActivity {
    private static final String CHINA_PHONE_NUMBER = "86";
    private static final int MSG_COUNT_DOWN_SECOND = 1;
    private EditText etUserName;
    private EditText etPassword;
    private Button btnResetPassword;
    private Button btnGetVeriCode;
    private CheckBox cbGrant;

    private String userNameVerifing; // 等待验证的用户名
    private String userNameVerified; // 已验证的用户名，即手机号
    private String password; // 密码
    private String veriCode; // 验证码
    private Thread countDownThread; // 倒计时线程

    // 手机短信验证回调事件处理器
    private final EventHandler eventHandler = new EventHandler() {
        public void afterEvent(int event, int result, Object data) {
            // afterEvent会在子线程被调用，因此如果后续有UI相关操作，需要将数据发送到UI线程
            Message msg = Message.obtain();//new Message();
            msg.arg1 = event;
            msg.arg2 = result;
            msg.obj = data;
            new Handler(Looper.getMainLooper(), new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    int event = msg.arg1;
                    int result = msg.arg2;
                    Object data = msg.obj;
                    if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            // 请注意，此时只是完成了发送验证码的请求，验证码短信还需要几秒钟之后才送达
                            ResetPasswordActivity.this.userNameVerified = userNameVerifing;
                            Toast.makeText(ResetPasswordActivity.this, "验证码已发出，请稍等。", Toast.LENGTH_SHORT).show();
                        } else {
                            ((Throwable) data).printStackTrace();
                        }
                    } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            // 验证码验证通过, 启动重置密码
                            resetPassword(userNameVerified, password);
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                            ((Throwable) data).printStackTrace();
                        }
                    }
                    return false;
                }
            }).sendMessage(msg);
        }
    };

    private final Handler countDownHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == MSG_COUNT_DOWN_SECOND) {
                int nSecond = msg.arg1;

                if(nSecond != 0)
                    ResetPasswordActivity.this.btnGetVeriCode.setText(String.format(Locale.getDefault(), "%d秒后\n重新获取", nSecond));
                else {
                    ResetPasswordActivity.this.btnGetVeriCode.setText("获取验证码");
                    ResetPasswordActivity.this.btnGetVeriCode.setEnabled(true);
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // 注册一个事件回调，用于处理SMSSDK接口请求的结果
        SMSSDK.registerEventHandler(eventHandler);

        etUserName = findViewById(R.id.et_user_name);
        etPassword = findViewById(R.id.et_password);

        btnGetVeriCode = findViewById(R.id.btn_get_qr_code);
        btnGetVeriCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPrivacyGrantChecked()) return;
                userNameVerifing = etUserName.getText().toString().trim();
                if(checkUserName(userNameVerifing)) {
                    SMSSDK.getVerificationCode(CHINA_PHONE_NUMBER, userNameVerifing); // 获取验证码
                    btnGetVeriCode.setEnabled(false);
                    startCountDownTimer();
                } else
                    Toast.makeText(ResetPasswordActivity.this, "手机号格式错误，请重新输入。", Toast.LENGTH_SHORT).show();
            }
        });

        EditText etVeriCode = findViewById(R.id.et_qr_code);
        btnResetPassword = findViewById(R.id.btn_reset_password);
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPrivacyGrantChecked()) return;
                String userName = etUserName.getText().toString().trim();
                if(ResetPasswordActivity.this.userNameVerified == null || !ResetPasswordActivity.this.userNameVerified.equals(userName)) {
                    Toast.makeText(ResetPasswordActivity.this, "请先获取手机号验证码。", Toast.LENGTH_SHORT).show();
                    return;
                }
                password = etPassword.getText().toString().trim();
                if(checkUserName(userName) && checkPassword(password)) {
                    veriCode = etVeriCode.getText().toString().trim();
                    SMSSDK.submitVerificationCode(CHINA_PHONE_NUMBER, userName, veriCode); // 提交验证码进行验证
                }
                else {
                    Toast.makeText(ResetPasswordActivity.this, "用户名或密码格式错误", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cbGrant = findViewById(R.id.cb_privacy_grant);
        cbGrant.setChecked(MobSDK.getPrivacyGrantedStatus()==1);

        TextView tvPrivacy = findViewById(R.id.tv_privacy);
        tvPrivacy.setMovementMethod(LinkMovementMethod.getInstance());

        TextView tvAgreement = findViewById(R.id.tv_agreement);
        tvAgreement.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SMSSDK.unregisterEventHandler(eventHandler);
        try {
            stopCountDownTimer();
        } catch (InterruptedException ignored) {
        }
    }

    private void resetPassword(String userName, String password) {
        MyApplication.getAccountManager().resetPassword(this, userName, password, new ICodeCallback() {
            @Override
            public void onFinish(int code, String msg) {
                Toast.makeText(ResetPasswordActivity.this, msg, Toast.LENGTH_SHORT).show();
                if(code == RCODE_SUCCESS) {
                    etPassword.setText(""); // 清空显示的密码
                    finish();
                }
            }
        });
    }

    private boolean isPrivacyGrantChecked() {
        boolean granted = cbGrant.isChecked();
        if (!granted) {
            Toast.makeText(this, R.string.pls_check_privacy, Toast.LENGTH_SHORT).show();
        }
        MobSDK.submitPolicyGrantResult(granted);
        return granted;
    }

    private void startCountDownTimer() {
        countDownThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int nSecond = 60;
                try {
                    while (--nSecond >= 0) {
                        Thread.sleep(1000);
                        Message.obtain(countDownHandler, MSG_COUNT_DOWN_SECOND, nSecond, 0).sendToTarget();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        countDownThread.start();
    }

    private void stopCountDownTimer() throws InterruptedException{
        if (countDownThread != null && countDownThread.isAlive()) {
            countDownThread.interrupt();
            countDownThread.join();
        }
    }

}