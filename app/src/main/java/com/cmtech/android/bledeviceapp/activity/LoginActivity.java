package com.cmtech.android.bledeviceapp.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.mob.MobSDK;
import com.vise.log.ViseLog;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

public class LoginActivity extends AppCompatActivity {

    private ImageButton qqLogin;
    private ImageButton wxLogin;
    private ImageButton hwLogin;
    private ImageButton phLogin;
    private CheckBox cbGrant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        qqLogin = findViewById(R.id.ib_qq_login);
        qqLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkPrivacyGrant()) return;
                Platform plat = ShareSDK.getPlatform(QQ.NAME);
                login(plat);
            }
        });

        wxLogin = findViewById(R.id.ib_weixin_login);
        wxLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkPrivacyGrant()) return;
                Platform plat = ShareSDK.getPlatform(Wechat.NAME);
                login(plat);
            }
        });

        hwLogin = findViewById(R.id.ib_huawei_login);
        hwLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkPrivacyGrant()) return;
                Intent intent = new Intent(LoginActivity.this, HuaweiLoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        phLogin = findViewById(R.id.ib_phone_login);
        phLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkPrivacyGrant()) return;
                sendCode(LoginActivity.this);
            }
        });

        cbGrant = findViewById(R.id.cb_privacy_grant);
    }

    private boolean checkPrivacyGrant() {
        boolean granted = cbGrant.isChecked();
        if(granted) {
            MobSDK.submitPolicyGrantResult(granted, null);
        } else {
            Toast.makeText(this, "如您同意本软件使用的隐私条款，请勾选授权框。", Toast.LENGTH_SHORT).show();
        }
        return granted;
    }

    private void login(Platform plat) {
        final String platName = plat.getName();
        //抖音登录适配安卓9.0
        ShareSDK.setActivity(LoginActivity.this);
        if (plat.isAuthValid()) {
            String userId = plat.getDb().getUserId();
            String userName = plat.getDb().getUserName();
            login(platName, userId, userName);
        } else {
            //授权回调监听，监听oncomplete，onerror，oncancel三种状态
            plat.setPlatformActionListener(new PlatformActionListener() {
                @Override
                public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                    String userId = platform.getDb().getUserId();
                    String userName = platform.getDb().getUserName();
                    String userIcon = platform.getDb().getUserIcon();
                    login(platName, userId, userName);
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

    private void login(String platName, String userId, String userName) {
        Account account = new Account();
        account.setPlatName(platName);
        account.setUserId(userId);
        account.setName(userName);
        AccountManager.getInstance().setAccount(account);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void sendCode(Context context) {
        RegisterPage page = new RegisterPage();
        //如果使用我们的ui，没有申请模板编号的情况下需传null
        page.setTempCode(null);
        page.setRegisterCallback(new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE) {
                    // 处理成功的结果
                    HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
                    // 国家代码，如“86”
                    String country = (String) phoneMap.get("country");
                    // 手机号码，如“13800138000”
                    String phone = (String) phoneMap.get("phone");
                    // TODO 利用国家代码和手机号码进行后续的操作
                    ViseLog.e(country+phone);
                    login("Phone", country+phone, phone);
                } else{
                    // TODO 处理错误的结果
                }
            }
        });
        page.show(context);
    }
}
