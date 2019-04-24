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

import com.cmtech.android.bledevice.core.BleDeviceUtil;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.UserManager;
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
  * Description:    登录界面Activity
  * Author:         chenm
  * CreateDate:     2018/10/27 09:18
  * UpdateUser:     chenm
  * UpdateDate:     2019-04-24 09:18
  * UpdateRemark:   更新说明
  * Version:        1.0
 */
public class LoginActivity extends AppCompatActivity {
    private final static int RC_ENABLE_BLUETOOTH = 1;

    private final static String CHINA_PHONE_NUMBER = "86";

    private final static int MSG_WAIT_SECOND = 1;

    private EditText etPhone;

    private EditText etVeriCode;

    private Button btnGetVeriCode;

    private final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

    private String phone; // 手机号

    private String veriCode; // 验证码

    //private final ScheduledExecutorService oneSecondService = Executors.newSingleThreadScheduledExecutor();

    private Thread timerThread;

    // 手机短信验证回调事件处理器
    private EventHandler eventHandler = new EventHandler() {
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
                        ViseLog.e("result = " + result);
                        ViseLog.e("data = " + data);
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

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == MSG_WAIT_SECOND) {
                int second = msg.arg1;

                if(second != 0)
                    LoginActivity.this.btnGetVeriCode.setText(String.format(Locale.getDefault(), "%d秒后\n重新获取", second));
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

        etPhone = findViewById(R.id.phone);

        etVeriCode = findViewById(R.id.verficationcode);

        btnGetVeriCode = findViewById(R.id.btn_get_vericode);

        Button btnSignin = findViewById(R.id.btn_signin);

        // 注册一个事件回调，用于处理SMSSDK接口请求的结果
        SMSSDK.registerEventHandler(eventHandler);

        TextView tvWelcome = findViewById(R.id.tv_welcometext);

        String welcomeText = String.format(getResources().getString(R.string.welcome_text_format), getResources().getString(R.string.app_name));

        tvWelcome.setText(welcomeText);

        phone = pref.getString("phone", "");

        etPhone.setText(phone);

        long lastLoginTime = pref.getLong("login_time", -1);

        long oneDayMillis = 24 * 60 * 60 * 1000;

        if(System.currentTimeMillis() - lastLoginTime < oneDayMillis) {
            signIn(phone, false);
        }

        btnGetVeriCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone = etPhone.getText().toString();

                getVeriCode(phone); // 获取验证码

                btnGetVeriCode.setEnabled(false);

                timerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 60;
                        while (--i >= 0) {
                            try {
                                Thread.sleep(1000);
                                Message msg = new Message();
                                msg.what = MSG_WAIT_SECOND;
                                msg.arg1 = i;
                                handler.sendMessage(msg);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                timerThread.start();

                /*oneSecondService.execute(new Runnable() {
                    @Override
                    public void run() {

                    }
                });*/
            }
        });

        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone = etPhone.getText().toString();

                veriCode = etVeriCode.getText().toString();

                verify(phone, veriCode); // 验证
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_ENABLE_BLUETOOTH:
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SMSSDK.unregisterEventHandler(eventHandler);

        if(timerThread != null) {
            timerThread.interrupt();
            timerThread = null;
        }

        /*if(!oneSecondService.isShutdown()) {
            oneSecondService.shutdownNow();
        }*/
    }

    // 登录
    private void signIn(String phone, boolean isSave) {
        UserManager manager = UserManager.getInstance();
        if(manager.signIn(phone) || manager.signUp(phone)) {
            //Toast.makeText(LoginActivity.this, "登录成功。", Toast.LENGTH_LONG).show();
            if(isSave)
                saveLoginInfoToPref();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "登录错误。", Toast.LENGTH_SHORT).show();
        }
    }

    // 将登录信息保存到Pref
    private void saveLoginInfoToPref() {
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("phone", phone);

        editor.putLong("login_time", System.currentTimeMillis());

        editor.commit();
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
            BleDeviceUtil.enableBluetooth(this, RC_ENABLE_BLUETOOTH);
        }
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

}
