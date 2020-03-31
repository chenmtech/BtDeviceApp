package com.cmtech.android.bledeviceapp.activity;


import android.app.Activity;
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

import org.litepal.LitePal;

import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

import static com.cmtech.android.bledeviceapp.activity.HuaweiLoginActivity.HUAWEI_PLAT_NAME;

public class LoginActivity extends AppCompatActivity {
    private static final String SMS_PLAT_NAME = "SMS";
    public static final Map<String, Integer> SUPPORT_PLATFORM = new HashMap<String, Integer>() {
        {
            put(QQ.NAME, R.mipmap.ic_qq);
            put(Wechat.NAME, R.mipmap.ic_wechat);
            put(HUAWEI_PLAT_NAME, R.mipmap.ic_huawei);
            put(SMS_PLAT_NAME, R.mipmap.ic_sms);
        }
    };

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
    }

    public static void loginMainActivity(Activity activity, String platName, String platId, String name) {
        Account account = LitePal.where("platName = ? and platId = ?", platName, platId).findFirst(Account.class);
        if(account == null) {
            account = new Account();
            account.setPlatName(platName);
            account.setPlatId(platId);
        }
        account.setName(name);
        account.save();
        AccountManager.getInstance().setAccount(account);

        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    private boolean checkPrivacyGrant() {
        boolean granted = cbGrant.isChecked();
        if(granted) {
            MobSDK.submitPolicyGrantResult(granted, null);
        } else {
            Toast.makeText(this, "如您同意上述隐私条款，请勾选授权框。", Toast.LENGTH_SHORT).show();
        }
        return granted;
    }

    private void loginUsingQQorWechat(Platform plat) {
        final String platName = plat.getName();
        ShareSDK.setActivity(LoginActivity.this);
        if (plat.isAuthValid()) {
            String platId = plat.getDb().getUserId();
            String name = plat.getDb().getUserName();
            LoginActivity.loginMainActivity(this, platName, platId, name);
        } else {
            //授权回调监听，监听oncomplete，onerror，oncancel三种状态
            plat.setPlatformActionListener(new PlatformActionListener() {
                @Override
                public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                    String platId = platform.getDb().getUserId();
                    String name = platform.getDb().getUserName();
                    String icon = platform.getDb().getUserIcon();
                    LoginActivity.loginMainActivity(LoginActivity.this, platName, platId, name);
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

    private void loginUsingHuaweiAccount() {
        Intent intent = new Intent(LoginActivity.this, HuaweiLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void loginUsingSMS(Context context) {
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
                    LoginActivity.loginMainActivity(LoginActivity.this, SMS_PLAT_NAME, country+phone, phone);
                } else{
                    // TODO 处理错误的结果
                }
            }
        });
        page.show(context);
    }
}
