package com.cmtech.android.bledeviceapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.cmtech.android.bledevice.core.BleDeviceUtil;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

/**
 *  LoginActivity: 登录界面
 *  Created by bme on 2018/10/27.
 */

public class LoginActivity extends AppCompatActivity {
    // 使能蓝牙权限返回码
    private final static int REQUESTCODE_ENABLEBLUETOOTH = 1;

    private EditText etAccount;
    private Button btnSignin;
    private Button btnSignup;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Button btnPhoneSignin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etAccount = findViewById(R.id.account);
        btnSignin = findViewById(R.id.btn_account_signin);
        btnSignup = findViewById(R.id.btn_account_signup);
        btnPhoneSignin = findViewById(R.id.btn_phone_signin);

        // 检查权限
        checkPermissions();

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        // 读账户名
        String account = pref.getString("account", "");
        etAccount.setText(account);

        // 根据是否自动登录及是否已经使能蓝牙，决定是否登录
        if(!TextUtils.isEmpty(account) && BleDeviceUtil.isBleEnable(MyApplication.getContext())) {
            signIn(account);
        }

        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = etAccount.getText().toString();
                signIn(account);
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = etAccount.getText().toString();
                signUp(account);
            }
        });

        btnPhoneSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCode(LoginActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUESTCODE_ENABLEBLUETOOTH:
                if (resultCode == RESULT_OK) {
                    enableBluetooth();

                    String account = etAccount.getText().toString();
                    signIn(account);

                } else if (resultCode == RESULT_CANCELED) { // 不同意
                    Toast.makeText(this, "蓝牙不打开，程序无法运行", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                for(int i = 0; i < grantResults.length; i++) {
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        // 不同意权限
                        Toast.makeText(this, "没有必要的权限，程序无法正常运行", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    } else {
                        if (permissions[i].equals(ACCESS_COARSE_LOCATION)) {
                            enableBluetooth();
                        }
                    }
                }

                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveLoginInfoToPref();
    }

    // 注册账户
    private void signUp(String account) {
        if(!UserAccountManager.getInstance().isAccountInfoValid(account)) {
            Toast.makeText(LoginActivity.this, "注册的账户信息无效。", Toast.LENGTH_SHORT).show();
        }

        boolean result = UserAccountManager.getInstance().signUp(account);
        if(!result) {
            Toast.makeText(LoginActivity.this, "账户已存在。", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(LoginActivity.this, "注册成功。", Toast.LENGTH_SHORT).show();
        }
    }

    // 登录
    private void signIn(String account) {
        boolean result = UserAccountManager.getInstance().signIn(account);
        if(result) {
            startMainActivity();
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "账户和密码不正确。", Toast.LENGTH_SHORT).show();
        }
    }

    // 启动MainActivity
    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    // 将登录信息保存到Pref
    private void saveLoginInfoToPref() {
        String account = etAccount.getText().toString();

        editor = pref.edit();

        editor.putString("account", account);

        editor.apply();
    }


    /**
     * 检查权限
     */
    private void checkPermissions() {
        List<String> permission = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //校验是否已具有模糊定位权限
            if (ContextCompat.checkSelfPermission(LoginActivity.this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permission.add(ACCESS_COARSE_LOCATION);
            }
            else{
                enableBluetooth();
            }
        } else {
            enableBluetooth();
        }

        //校验是否已具有外部存储的权限
        if(ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if(permission.size() != 0)
            ActivityCompat.requestPermissions(LoginActivity.this, permission.toArray(new String[0]), 1);
    }

    // 使能蓝牙
    private void enableBluetooth() {
        if (!BleDeviceUtil.isBleEnable(MyApplication.getContext())) {
            BleDeviceUtil.enableBluetooth(this, REQUESTCODE_ENABLEBLUETOOTH);
        }
    }

    private void authorize(Platform plat) {
        if (plat == null || !plat.isClientValid()) {
            Toast.makeText(this, "无法登陆", Toast.LENGTH_SHORT).show();
            return;
        }
        //判断指定平台是否已经完成授权
        if(plat.isAuthValid()) {
            String userId = plat.getDb().getUserId();
            if (userId != null) {
                Toast.makeText(this, "已经授权", Toast.LENGTH_SHORT).show();
                login(plat.getName(), userId, null);
                return;
            }
        }
        plat.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int action, HashMap<String, Object> hashMap) {
                if (action == Platform.ACTION_USER_INFOR) {
                    Toast.makeText(LoginActivity.this, "授权成功", Toast.LENGTH_SHORT).show();
                    login(platform.getName(), platform.getDb().getUserId(), hashMap);
                }
                System.out.println(hashMap);
                System.out.println("------User Name ---------" + platform.getDb().getUserName());
                System.out.println("------User ID ---------" + platform.getDb().getUserId());
            }

            @Override
            public void onError(Platform platform, int action, Throwable throwable) {
                if (action == Platform.ACTION_USER_INFOR) {
                    Toast.makeText(LoginActivity.this, "授权失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancel(Platform platform, int action) {
                if (action == Platform.ACTION_USER_INFOR) {
                    Toast.makeText(LoginActivity.this, "授权取消", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // true不使用SSO授权，false使用SSO授权
        plat.SSOSetting(false);
        //获取用户资料
        plat.authorize();
    }

    private void login(String plat, String userId, HashMap<String, Object> userInfo) {
        Toast.makeText(LoginActivity.this, "开始登陆", Toast.LENGTH_SHORT).show();
    }

    private void sendCode(Context context) {
        RegisterPage page = new RegisterPage();
        //如果使用我们的ui，没有申请模板编号的情况下需传null
        page.setTempCode(null);
        page.setRegisterCallback(new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE) {
                    // 处理成功的结果
                    HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
                    String country = (String) phoneMap.get("country"); // 国家代码，如“86”
                    String phone = (String) phoneMap.get("phone"); // 手机号码，如“13800138000”
                    // TODO 利用国家代码和手机号码进行后续的操作

                } else{
                    // TODO 处理错误的结果
                }
            }
        });
        page.show(context);
    }

}
