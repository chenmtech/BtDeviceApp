package com.cmtech.android.bledeviceapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;
import com.cmtech.android.bledevice.core.BleDeviceUtil;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

/**
 *  LoginActivity: 登录界面
 *  Created by bme on 2018/10/27.
 */

public class LoginActivity extends AppCompatActivity {
    // 使能蓝牙权限返回码
    private final static int REQUESTCODE_ENABLEBLUETOOTH = 1;

    private EditText etAccount;
    private EditText etPassword;
    private Button btnSignin;
    private Button btnSignup;
    private CheckBox cbRememberPassword;
    private CheckBox cbAutoSignin;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 检查权限
        checkPermissions();

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        etAccount = findViewById(R.id.account);
        etPassword = findViewById(R.id.password);
        btnSignin = findViewById(R.id.btn_account_signin);
        btnSignup = findViewById(R.id.btn_account_signup);
        cbRememberPassword = findViewById(R.id.cb_remember_password);
        cbAutoSignin = findViewById(R.id.cb_auto_signin);

        // 读账户名
        String account = pref.getString("account", "");
        etAccount.setText(account);

        String password = "";
        // 读是否记住密码，并根据结果决定是否读取密码
        boolean isRemember = pref.getBoolean("remember_password", false);
        if(isRemember) {
            password = pref.getString("password", "");
        }
        cbRememberPassword.setChecked(isRemember);

        // 设置密码
        etPassword.setText(password);

        // 读是否自动登录
        boolean autoSignin = pref.getBoolean("auto_signin", false);
        cbAutoSignin.setChecked(autoSignin);

        // 根据是否自动登录及是否已经使能蓝牙，决定是否登录
        if(autoSignin && BleDeviceUtil.isBleEnable(MyApplication.getContext())) {
            signIn(account, password);
        }

        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = etAccount.getText().toString();
                String password = etPassword.getText().toString();
                signIn(account, password);
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = etAccount.getText().toString();
                String password = etPassword.getText().toString();
                signUp(account, password);
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

                    boolean autoSignin = pref.getBoolean("auto_signin", false);
                    if(autoSignin) {
                        String account = etAccount.getText().toString();
                        String password = etPassword.getText().toString();
                        signIn(account, password);
                    }

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
    private void signUp(String account, String password) {
        if(!UserAccountManager.getInstance().isAccountInfoValid(account, password)) {
            Toast.makeText(LoginActivity.this, "注册的账户信息无效。", Toast.LENGTH_SHORT).show();
        }

        boolean result = UserAccountManager.getInstance().signUp(account, password);
        if(!result) {
            Toast.makeText(LoginActivity.this, "账户已存在。", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(LoginActivity.this, "注册成功。", Toast.LENGTH_SHORT).show();
        }
    }

    // 登录
    private void signIn(String account, String password) {
        boolean result = UserAccountManager.getInstance().signIn(account, password);
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
        String password = etPassword.getText().toString();

        editor = pref.edit();

        editor.putString("account", account);

        if(cbRememberPassword.isChecked()) {
            editor.putString("password", password);
        } else {
            editor.remove("password");
        }

        editor.putBoolean("remember_password", cbRememberPassword.isChecked());
        editor.putBoolean("auto_signin", cbAutoSignin.isChecked());

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

}
