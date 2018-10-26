package com.cmtech.android.bledeviceapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.UserAccount;
import com.cmtech.android.bledevicecore.model.BleDeviceUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

/**
 *  LoginActivity: 登录界面
 *  Created by bme on 2018/10/27.
 */

public class LoginActivity extends AppCompatActivity {

    private final static int REQUESTCODE_ENABLEBLUETOOTH = 1;    // 使能蓝牙

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


        if(MainActivity.hasSignin()) {
            startMainActivity();
            finish();
        }

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        etAccount = findViewById(R.id.account);
        etPassword = findViewById(R.id.password);

        btnSignin = findViewById(R.id.btn_account_signin);
        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = etAccount.getText().toString();
                String password = etPassword.getText().toString();
                signin(account, password);
            }
        });

        btnSignup = findViewById(R.id.btn_account_signup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = etAccount.getText().toString();
                String password = etPassword.getText().toString();
                signup(account, password);
            }
        });


        cbRememberPassword = findViewById(R.id.cb_remember_password);
        boolean isRemember = pref.getBoolean("remember_password", false);
        if(isRemember) {
            String account = pref.getString("account", "");
            String password = pref.getString("password", "");
            etAccount.setText(account);
            etPassword.setText(password);
            cbRememberPassword.setChecked(true);
        } else {
            cbRememberPassword.setChecked(false);
        }

        cbAutoSignin = findViewById(R.id.cb_auto_signin);
        boolean autoSignin = pref.getBoolean("auto_signin", false);
        if(autoSignin) {
            String account = etAccount.getText().toString();
            String password = etPassword.getText().toString();
            signin(account, password);
            cbAutoSignin.setChecked(true);
        } else {
            cbAutoSignin.setChecked(false);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUESTCODE_ENABLEBLUETOOTH:
                if (resultCode == RESULT_OK) {
                    enableBluetooth();
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
                        Toast.makeText(this, "没有权限程序无法运行", Toast.LENGTH_SHORT).show();
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
        saveLoginSetup();
    }

    private void signup(String account, String password) {
        List<UserAccount> find = LitePal.where("name = ?", account).find(UserAccount.class);
        if(find != null && find.size() > 0) {
            Toast.makeText(LoginActivity.this, "账户已存在。", Toast.LENGTH_SHORT).show();
        } else {
            UserAccount user = new UserAccount();
            user.setName(account);
            user.setPassword(password);
            user.save();
            Toast.makeText(LoginActivity.this, "账户注册成功。", Toast.LENGTH_SHORT).show();
        }
    }

    private void signin(String account, String password) {
        List<UserAccount> find = LitePal.where("name = ? and password = ?", account, password).find(UserAccount.class);
        if(find != null && find.size() == 1) {
            UserAccount user = new UserAccount();
            user.setName(account);
            user.setPassword(password);
            MainActivity.setUserAccount(user);

            startMainActivity();
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "账户和密码不正确。", Toast.LENGTH_SHORT).show();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        Intent inputIntent = getIntent();
        if(inputIntent != null && inputIntent.getData() != null) {
            intent.setData(inputIntent.getData());
        }
        startActivity(intent);
    }

    private void saveLoginSetup() {
        String account = etAccount.getText().toString();
        String password = etPassword.getText().toString();

        editor = pref.edit();
        if(cbRememberPassword.isChecked()) {
            editor.putBoolean("remember_password", true);
            editor.putString("account", account);
            editor.putString("password", password);

        } else {
            editor.remove("remember_password");
            editor.remove("account");
            editor.remove("password");
        }

        if(cbAutoSignin.isChecked()) {
            editor.putBoolean("auto_signin", true);
        } else {
            editor.remove("auto_signin");
        }
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
