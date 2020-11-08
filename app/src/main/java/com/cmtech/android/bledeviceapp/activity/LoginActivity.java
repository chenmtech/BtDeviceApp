package com.cmtech.android.bledeviceapp.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.AccountManager;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.PhoneAccount;
import com.mob.MobSDK;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

import static com.cmtech.android.bledeviceapp.global.AppConstant.PHONE_PLAT_NAME;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

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

        /*if(true) {
            //loginUsingPhone(this);
            login(PHONE_PLAT_NAME, "8615019187404", "", "");
            return;
        }*/

        etUserName = findViewById(R.id.et_user_name);
        etPassword = findViewById(R.id.et_password);

        btnSignUp = findViewById(R.id.btn_signup);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPrivacyGrantChecked()) return;
                String userName = etUserName.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                signUp(userName, password);
            }
        });

        btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = etUserName.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                login(userName, password);
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
        if(!granted) {
            Toast.makeText(this, R.string.pls_check_privacy, Toast.LENGTH_SHORT).show();
        }
        return granted;
    }

    @Override
    public void onBackPressed() {

    }

    private void signUp(String userName, String password) {
        MyApplication.getAccountManager().signUp(this, userName, password);
    }

    private void login(String userName, String password) {
        if(MyApplication.getAccountManager().localLogin(userName, password)) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

            MyApplication.getAccountManager().webLogin(this, null);
        } else {
            MyApplication.getAccountManager().webLogin(userName, password, this, "正在登录，请稍等...", new ICodeCallback() {
                @Override
                public void onFinish(int code) {
                    if(code == RETURN_CODE_SUCCESS) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "账户登录失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}
