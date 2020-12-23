package com.cmtech.android.bledeviceapp.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.util.WebFailureHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

public class LoginActivity extends AppCompatActivity {
    private EditText etUserName;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnSignUp;
    private Button btnForgetPassword;

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

        btnSignUp = findViewById(R.id.btn_signup);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
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

        btnForgetPassword = findViewById(R.id.btn_forget_password);
        btnForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
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
            public void onFinish(int code) {
                if(code == RETURN_CODE_SUCCESS && MyApplication.getAccountManager().isValid()) {
                    startMainActivity();
                } else {
                    Toast.makeText(LoginActivity.this, WebFailureHandler.toString(code), Toast.LENGTH_SHORT).show();
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
}