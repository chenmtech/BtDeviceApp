package com.cmtech.android.bledeviceapp.activity;


import android.content.Intent;
import android.os.Bundle;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private EditText etUserName;
    private EditText etPassword;
    private Button btnSignUp;
    private Button btnLogin;
    private CheckBox cbGrant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (MyApplication.getAccountManager().localLogin()) {
            startMainActivity();
            return;
        }

        etUserName = findViewById(R.id.et_user_name);
        etPassword = findViewById(R.id.et_password);

        btnSignUp = findViewById(R.id.btn_signup);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPrivacyGrantChecked()) return;
                String userName = etUserName.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if(checkUserNameAndPassword(userName) && checkUserNameAndPassword(password))
                    signUp(userName, password);
                else {
                    Toast.makeText(LoginActivity.this, "用户名或密码格式错误，请重新输入", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = etUserName.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if(checkUserNameAndPassword(userName) && checkUserNameAndPassword(password))
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

    private void signUp(String userName, String password) {
        MyApplication.getAccountManager().signUp(this, userName, password);
    }

    private void login(String userName, String password) {
        MyApplication.getAccountManager().login(userName, password, this, "正在登录，请稍等...", new ICodeCallback() {
            @Override
            public void onFinish(int code) {
                if (MyApplication.getAccountManager().isValid()) {
                    startMainActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "账户登录失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private static boolean checkUserNameAndPassword(String str) {
        String regex = "([a-zA-Z0-9]{5,10})";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        return m.matches();
    }
}