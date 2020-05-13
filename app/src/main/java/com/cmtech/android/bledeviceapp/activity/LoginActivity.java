package com.cmtech.android.bledeviceapp.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.KMWebService;
import com.mob.MobSDK;
import com.vise.log.ViseLog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.cmtech.android.bledeviceapp.AppConstant.HW_PLAT_NAME;
import static com.cmtech.android.bledeviceapp.AppConstant.SMS_PLAT_NAME;

public class LoginActivity extends AppCompatActivity {
    private ImageButton qqLogin;
    private ImageButton wxLogin;
    private ImageButton hwLogin;
    private ImageButton smsLogin;
    private CheckBox cbGrant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Platform plat = ShareSDK.getPlatform(QQ.NAME);
        if(plat.isAuthValid()) {
            loginUsingQQorWechat(plat);
            return;
        }
        plat = ShareSDK.getPlatform(Wechat.NAME);
        if(plat.isAuthValid()) {
            loginUsingQQorWechat(plat);
            return;
        }

        qqLogin = findViewById(R.id.ib_qq_login);
        qqLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkPrivacyGrant()) return;
                Platform plat = ShareSDK.getPlatform(QQ.NAME);
                loginUsingQQorWechat(plat);
            }
        });

        wxLogin = findViewById(R.id.ib_wechat_login);
        wxLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkPrivacyGrant()) return;
                Platform plat = ShareSDK.getPlatform(Wechat.NAME);
                loginUsingQQorWechat(plat);
            }
        });

        hwLogin = findViewById(R.id.ib_huawei_login);
        hwLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkPrivacyGrant()) return;
                loginUsingHuaweiAccount();
            }
        });

        smsLogin = findViewById(R.id.ib_sms_login);
        smsLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkPrivacyGrant()) return;
                loginUsingSMS(LoginActivity.this);
            }
        });

        cbGrant = findViewById(R.id.cb_privacy_grant);

        TextView tvPrivacy = findViewById(R.id.tv_privacy);
        tvPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private boolean checkPrivacyGrant() {
        boolean granted = cbGrant.isChecked();
        if(granted) {
            MobSDK.submitPolicyGrantResult(granted, null);
        } else {
            Toast.makeText(this, "请勾选同意隐私条款。", Toast.LENGTH_SHORT).show();
        }
        return granted;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 1: // 华为登录返回码
                if (resultCode == RESULT_OK) {
                    String platId = data.getStringExtra("platId");
                    String userName = data.getStringExtra("userName");
                    String icon = data.getStringExtra("icon");
                    login(HW_PLAT_NAME, platId, userName, icon);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loginUsingQQorWechat(Platform plat) {
        final String platName = plat.getName();
        ShareSDK.setActivity(LoginActivity.this);
        if (plat.isAuthValid()) {
            String platId = plat.getDb().getUserId();
            String username = plat.getDb().getUserName();
            String icon = plat.getDb().getUserIcon();
            login(platName, platId, username, icon);
        } else {
            //授权回调监听，监听oncomplete，onerror，oncancel三种状态
            plat.setPlatformActionListener(new PlatformActionListener() {
                @Override
                public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                    String platId = platform.getDb().getUserId();
                    String userName = platform.getDb().getUserName();
                    String icon = platform.getDb().getUserIcon();
                    login(platName, platId, userName, icon);
                }

                @Override
                public void onError(Platform platform, int i, Throwable throwable) {
                    Toast.makeText(LoginActivity.this, "登录错误。", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancel(Platform platform, int i) {

                }
            });
            //单独授权，OnComplete返回的hashmap是空的
            plat.authorize();
        }
    }

    private void loginUsingHuaweiAccount() {
        Intent intent = new Intent(LoginActivity.this, HuaweiLoginActivity.class);
        startActivityForResult(intent, 1);
    }

    private void loginUsingSMS(Context context) {
        RegisterPage page = new RegisterPage();
        page.setTempCode(null);
        page.setRegisterCallback(new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE) {
                    HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
                    String country = (String) phoneMap.get("country");
                    String phone = (String) phoneMap.get("phone");
                    String platId = country+phone;
                    login(SMS_PLAT_NAME, platId, phone, "");
                } else{
                    // TODO 处理错误的结果
                }
            }
        });
        page.show(context);
    }

    private void login(String platName, String platId, String name, String icon) {
        loginKMServer(platName, platId);
        AccountManager.login(platName, platId, name, icon);

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void loginKMServer(String platName, String platId) {
        KMWebService.signUporLogin(platName, platId, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "网络错误。", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.body() == null) return;
                String respBody = response.body().string();
                try {
                    JSONObject json = new JSONObject(respBody);
                    int code = json.getInt("code");
                    final String errStr = json.getString("errStr");
                    ViseLog.e(code+errStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
