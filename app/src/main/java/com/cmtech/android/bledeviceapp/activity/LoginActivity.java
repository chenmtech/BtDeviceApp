package com.cmtech.android.bledeviceapp.activity;


import static com.cmtech.android.bledeviceapp.activity.SignUpActivity.checkPassword;
import static com.cmtech.android.bledeviceapp.activity.SignUpActivity.checkUserName;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_SUCCESS;

import android.content.Intent;
import android.os.Bundle;
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

/**
 *
 * ClassName:      SplashActivity
 * Description:    登录界面Activity
 * Author:         chenm
 * CreateDate:     2018/10/27 09:18
 * UpdateUser:     chenm
 * UpdateDate:     2019-04-24 09:18
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class LoginActivity extends AppCompatActivity {
    private EditText etUserName;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnSignUp;
    private Button btnForgetPassword;
    private CheckBox cbGrant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (MyApplication.getAccountManager().localLogin()) {
            //ViseLog.e("local login ok");
            startMainActivity();
            return;
        }

        etUserName = findViewById(R.id.et_user_name);
        etUserName.setText(MyApplication.getAccount().getUserName());
        etPassword = findViewById(R.id.et_password);


        btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPrivacyGrantChecked()) return;
                String userName = etUserName.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if(checkUserName(userName) && checkPassword(password))
                    login(userName, password);
                else {
                    Toast.makeText(LoginActivity.this, "用户名或密码格式错误，请重新输入", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSignUp = findViewById(R.id.btn_signup);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        btnForgetPassword = findViewById(R.id.btn_forget_password);
        btnForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ChangePasswordActivity.class);
                startActivityForResult(intent, 2);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        cbGrant.setChecked(MobSDK.getPrivacyGrantedStatus()==1);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void login(String userName, String password) {
        MyApplication.getAccountManager().login(userName, password, this, "正在登录，请稍等...", new ICodeCallback() {
            @Override
            public void onFinish(int code, String msg) {
                if(code == RCODE_SUCCESS && MyApplication.getAccountManager().isValid()) {
                    startMainActivity();
                } else {
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isPrivacyGrantChecked() {
        boolean granted = cbGrant.isChecked();
        if (!granted) {
            Toast.makeText(this, R.string.pls_check_privacy, Toast.LENGTH_SHORT).show();
        }
        MobSDK.submitPolicyGrantResult(granted);
        return granted;
    }
}