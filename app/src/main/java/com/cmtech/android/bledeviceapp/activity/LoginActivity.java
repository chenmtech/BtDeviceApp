package com.cmtech.android.bledeviceapp.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.AccountManager;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;

public class LoginActivity extends AppCompatActivity {

    private ImageButton qqLogin;
    private ImageButton hwLogin;
    private ImageButton phLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        qqLogin = findViewById(R.id.ib_qq_login);
        qqLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Platform plat = ShareSDK.getPlatform(QQ.NAME);
                login(plat);
            }
        });

        hwLogin = findViewById(R.id.ib_huawei_login);
        hwLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, HuaweiLoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        phLogin = findViewById(R.id.ib_phone_login);
        phLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void login(Platform plat) {
        final String platName = plat.getName();
        //抖音登录适配安卓9.0
        ShareSDK.setActivity(LoginActivity.this);
        if (plat.isAuthValid()) {
            String userId = plat.getDb().getUserId();
            String userName = plat.getDb().getUserName();
            login(platName+userId, userName);
        } else {
            //授权回调监听，监听oncomplete，onerror，oncancel三种状态
            plat.setPlatformActionListener(new PlatformActionListener() {
                @Override
                public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                    String userId = platform.getDb().getUserId();
                    String userName = platform.getDb().getUserName();
                    String userIcon = platform.getDb().getUserIcon();
                    login(platName+userId, userName);
                }

                @Override
                public void onError(Platform platform, int i, Throwable throwable) {

                }

                @Override
                public void onCancel(Platform platform, int i) {

                }
            });
            //单独授权，OnComplete返回的hashmap是空的
            plat.authorize();
        }
    }

    private void login(String userId, String userName) {
        Account account = new Account();
        account.setUserId(userId);
        account.setName(userName);
        AccountManager.getInstance().setAccount(account);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
