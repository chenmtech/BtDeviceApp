package com.cmtech.android.bledeviceapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

/**
  *
  * ClassName:      LoginActivity
  * Description:    登录Activity
  * Author:         chenm
  * CreateDate:     2018/10/27 09:18
  * UpdateUser:     chenm
  * UpdateDate:     2019-04-24 09:18
  * UpdateRemark:   更新说明
  * Version:        1.0
 */
public class LoginActivity extends AppCompatActivity {
    private static final String CHINA_PHONE_NUMBER = "86"; // 中国手机号
    private static final int MSG_COUNT_DOWN = 1; // 倒计时消息
    private static final long MS_PER_DAY = 24 * 60 * 60 * 1000; // 每天的毫秒数
    private static final String MAGIC_VERI_CODE = "abcdef"; // 万能验证码
    private static final int DAY_NUM = 3; // 连续未登录天数，超过这个天数将要求重新登录
    private static final String KEY_PHONE = "phone";
    private static final String KEY_TIME = "time";

    private EditText etPhone;
    private Button btnGetVeriCode;

    private SharedPreferences pref;
    private String phone; // 手机号
    private Thread countThread; // 倒计时线程

    // 手机短信验证回调事件处理器
    private final EventHandler eventHandler = new EventHandler() {
        public void afterEvent(int event, int result, Object data) {
            // afterEvent会在子线程被调用，因此如果后续有UI相关操作，需要将数据发送到UI线程
            Message msg = new Message();
            msg.arg1 = event;
            msg.arg2 = result;
            msg.obj = data;
            new Handler(Looper.getMainLooper(), new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    int event = msg.arg1;
                    int result = msg.arg2;
                    Object data = msg.obj;
                    if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        //ViseLog.e("result = " + result);
                        //ViseLog.e("data = " + data);
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            // TODO 处理成功得到验证码的结果
                            // 请注意，此时只是完成了发送验证码的请求，验证码短信还需要几秒钟之后才送达
                            if((Boolean) data) {
                                // 智能验证成功，直接登录
                                signIn(phone, true);
                            } else {
                                Toast.makeText(LoginActivity.this, "验证码已发出，请稍等。", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // TODO 处理错误的结果
                            ((Throwable) data).printStackTrace();
                        }
                    } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            // TODO 处理验证码验证通过的结果
                            signIn(phone, true);
                        } else {
                            // TODO 处理错误的结果
                            Toast.makeText(LoginActivity.this, "验证失败。", Toast.LENGTH_SHORT).show();
                            ((Throwable) data).printStackTrace();
                        }
                    }
                    // TODO 其他接口的返回结果也类似，根据event判断当前数据属于哪个接口
                    return false;
                }
            }).sendMessage(msg);
        }
    };

    private final Handler countHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == MSG_COUNT_DOWN) {
                int nSecond = msg.arg1;
                if(nSecond != 0)
                    LoginActivity.this.btnGetVeriCode.setText(String.format(Locale.getDefault(), "%d秒后\n重新获取", nSecond));
                else {
                    LoginActivity.this.btnGetVeriCode.setText("获取验证码");
                    LoginActivity.this.btnGetVeriCode.setEnabled(true);
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 检查权限
        checkPermissions();
    }

    private void initialize() {
        // 注册一个事件回调，用于处理SMSSDK接口请求的结果
        SMSSDK.registerEventHandler(eventHandler);

        TextView tvWelcome = findViewById(R.id.tv_welcometext);
        String welcomeText = String.format(getResources().getString(R.string.welcome_text_format), getResources().getString(R.string.app_name));
        tvWelcome.setText(welcomeText);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        phone = pref.getString(KEY_PHONE, "");
        long lastLoginTime = pref.getLong(KEY_TIME, -1);
        if(System.currentTimeMillis() - lastLoginTime < DAY_NUM*MS_PER_DAY) {
            signIn(phone, false);
        }

        etPhone = findViewById(R.id.phone);
        etPhone.setText(phone);

        btnGetVeriCode = findViewById(R.id.btn_get_vericode);
        btnGetVeriCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone = etPhone.getText().toString();
                getVeriCode(phone); // 获取验证码
                btnGetVeriCode.setEnabled(false);
                startCountDown();
            }
        });

        Button btnSignin = findViewById(R.id.btn_signin);
        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    stopCountDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                phone = etPhone.getText().toString();
                EditText etVeriCode = findViewById(R.id.verficationcode);
                String veriCode = etVeriCode.getText().toString();
                if(veriCode.equals(MAGIC_VERI_CODE)) {
                    signIn(phone, true);
                } else {
                    verify(phone, veriCode); // 验证
                }
            }
        });
    }

    // 检查权限
    private void checkPermissions() {
        List<String> permission = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(LoginActivity.this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permission.add(ACCESS_COARSE_LOCATION);
            }
        }
        if(ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(permission.size() != 0)
            ActivityCompat.requestPermissions(LoginActivity.this, permission.toArray(new String[0]), 1);
        else
            initialize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                for(int result : grantResults) {
                    if(result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "没有必要的权限，程序无法正常运行", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    }
                }
                break;
        }

        initialize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SMSSDK.unregisterEventHandler(eventHandler);
        try {
            stopCountDown();
        } catch (InterruptedException ignored) {
        }
    }

    // 登录
    private void signIn(String phone, boolean isSaveLoginInfo) {
        AccountManager manager = AccountManager.getInstance();
        if(manager.signIn(phone) || manager.signUp(phone)) {
            if(isSaveLoginInfo)
                saveLoginInfo(pref, phone, System.currentTimeMillis());

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "登录错误。", Toast.LENGTH_SHORT).show();
        }
    }

    // 将登录信息保存到Pref
    public static void saveLoginInfo(SharedPreferences pref, String phone, long time) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_PHONE, phone);
        editor.putLong(KEY_TIME, time);
        editor.commit();
    }

    // 获取验证码
    private void getVeriCode(final String phone) {
        // 请求验证码，其中country表示国家代码，如“86”；phone表示手机号码，如“13800138000”
        SMSSDK.getVerificationCode(CHINA_PHONE_NUMBER, phone);
    }

    // 提交验证
    private void verify(final String phone, final String veriCode) {
        // 提交验证码，其中的code表示验证码，如“1357”
        SMSSDK.submitVerificationCode(CHINA_PHONE_NUMBER, phone, veriCode);
    }

    private void startCountDown() {
        countThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int nSecond = 60;
                try {
                    while (--nSecond >= 0) {
                        Thread.sleep(1000);
                        Message.obtain(countHandler, MSG_COUNT_DOWN, nSecond, 0).sendToTarget();
                    }
                } catch (InterruptedException e) {
                    ViseLog.i("The timer of getting veri code is interrupted.");
                    Thread.currentThread().interrupt();
                }
            }
        });
        countThread.start();
    }

    private void stopCountDown() throws InterruptedException{
        if (countThread != null && countThread.isAlive()) {
            countThread.interrupt();
            countThread.join();
        }
    }

}
