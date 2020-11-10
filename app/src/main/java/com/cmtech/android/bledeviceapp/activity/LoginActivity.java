package com.cmtech.android.bledeviceapp.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.util.WebFailureHandler;
import com.vise.log.ViseLog;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

public class LoginActivity extends AppCompatActivity {
    private static final String CHINA_PHONE_NUMBER = "86";
    private static final int MSG_WAIT_SECOND = 1;
    private EditText etUserName;
    private EditText etPassword;
    private Button btnSignUp;
    private Button btnLogin;
    private Button btnGetVeriCode;
    private CheckBox cbGrant;

    private String userName; // 用户名，即手机号
    private String password; // 密码
    private String veriCode; // 验证码
    private Thread countDownThread; // 倒计时线程

    // 手机短信验证回调事件处理器
    private final EventHandler eventHandler = new EventHandler() {
        public void afterEvent(int event, int result, Object data) {
            // afterEvent会在子线程被调用，因此如果后续有UI相关操作，需要将数据发送到UI线程
            Message msg = new Message();
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
                            Toast.makeText(LoginActivity.this, "验证码已发出，请稍等。", Toast.LENGTH_SHORT).show();
                        } else {
                            ((Throwable) data).printStackTrace();
                        }
                    } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            // 验证码验证通过的结果, 启动注册
                            signUp(userName, password);
                        } else {
                            Toast.makeText(LoginActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                            ((Throwable) data).printStackTrace();
                        }
                    }
                    return false;
                }
            }).sendMessage(msg);
        }
    };

    private final Handler waitASecondHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == MSG_WAIT_SECOND) {
                int nSecond = msg.arg1;

                if(nSecond != 0)
                    LoginActivity.this.btnGetVeriCode.setText(String.format(Locale.getDefault(), "%d秒后\n重新获取", nSecond));
                else {
                    LoginActivity.this.btnGetVeriCode.setText("获取验证码");
                    LoginActivity.this.btnGetVeriCode.setEnabled(true);
                }
            }
            return false;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (MyApplication.getAccountManager().localLogin()) {
            //ViseLog.e("local login ok");
            startMainActivity();
            return;
        }

        // 注册一个事件回调，用于处理SMSSDK接口请求的结果 
        SMSSDK.registerEventHandler(eventHandler);

        etUserName = findViewById(R.id.et_user_name);
        etPassword = findViewById(R.id.et_password);

        btnGetVeriCode = findViewById(R.id.btn_get_qr_code);
        btnGetVeriCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = etUserName.getText().toString().trim();
                if(checkUserName(userName)) {
                    LoginActivity.this.userName = userName;
                    getVeriCode(userName); // 获取验证码
                    btnGetVeriCode.setEnabled(false);
                    startCountDownTimer();
                } else
                    Toast.makeText(LoginActivity.this, "手机号格式错误，请重新输入。", Toast.LENGTH_SHORT).show();
            }
        });

        EditText etVeriCode = findViewById(R.id.et_qr_code);
        btnSignUp = findViewById(R.id.btn_signup);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPrivacyGrantChecked()) return;
                String userName = etUserName.getText().toString().trim();
                if(LoginActivity.this.userName == null || !LoginActivity.this.userName.equals(userName)) {
                    Toast.makeText(LoginActivity.this, "请先验证手机号。", Toast.LENGTH_SHORT).show();
                    return;
                }
                password = etPassword.getText().toString().trim();
                if(checkUserName(userName) && checkPassword(password)) {
                    veriCode = etVeriCode.getText().toString().trim();
                    verify(userName, veriCode);
                }
                else {
                    Toast.makeText(LoginActivity.this, "用户名或密码格式错误", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = etUserName.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if(checkUserName(userName) && checkPassword(password))
                    login(userName, password);
                else {
                    Toast.makeText(LoginActivity.this, "用户名或密码格式错误，请重新输入", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cbGrant = findViewById(R.id.cb_privacy_grant);

        TextView tvPrivacy = findViewById(R.id.tv_privacy);
        tvPrivacy.setMovementMethod(LinkMovementMethod.getInstance());

        TextView tvAgreement = findViewById(R.id.tv_agreement);
        tvAgreement.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private boolean isPrivacyGrantChecked() {
        boolean granted = cbGrant.isChecked();
        if (!granted) {
            Toast.makeText(this, R.string.pls_check_privacy, Toast.LENGTH_SHORT).show();
        }
        return granted;
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

    private void signUp(String userName, String password) {
        MyApplication.getAccountManager().signUp(this, userName, password, new ICodeCallback() {
            @Override
            public void onFinish(int code) {
                ViseLog.e("code:"+code);
                if(code == RETURN_CODE_SUCCESS) {
                    Toast.makeText(LoginActivity.this, "账户注册成功", Toast.LENGTH_SHORT).show();
                    etPassword.setText(""); // 清空密码
                } else {
                    Toast.makeText(LoginActivity.this, WebFailureHandler.handle(code), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void login(String userName, String password) {
        MyApplication.getAccountManager().login(userName, password, this, "正在登录，请稍等...", new ICodeCallback() {
            @Override
            public void onFinish(int code) {
                if(code == RETURN_CODE_SUCCESS && MyApplication.getAccountManager().isValid()) {
                    startMainActivity();
                } else {
                    Toast.makeText(LoginActivity.this, WebFailureHandler.handle(code), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private static boolean checkPassword(String str) {
        String regex = "([a-zA-Z0-9]{5,10})";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    private static boolean checkUserName(String str) {
        String regex = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        return m.find();
    }

    // 获取验证码
    private static void getVeriCode(final String phone) {
        // 请求验证码，其中country表示国家代码，如“86”；phone表示手机号码，如“13800138000”
        SMSSDK.getVerificationCode(CHINA_PHONE_NUMBER, phone);
    }

    // 提交验证
    private static void verify(final String phone, final String veriCode) {
        // 提交验证码，其中的code表示验证码，如“1357”
        SMSSDK.submitVerificationCode(CHINA_PHONE_NUMBER, phone, veriCode);
    }

    private void startCountDownTimer() {
        countDownThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int nSecond = 60;
                try {
                    while (--nSecond >= 0) {
                        Thread.sleep(1000);
                        Message.obtain(waitASecondHandler, MSG_WAIT_SECOND, nSecond, 0).sendToTarget();
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