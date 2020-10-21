package com.cmtech.android.bledeviceapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.vise.utils.file.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_CACHE;
import static com.cmtech.android.bledeviceapp.global.AppConstant.SPLASH_ACTIVITY_COUNT_DOWN_SECOND;

/**
  *
  * ClassName:      SplashActivity
  * Description:    启动界面Activity
  * Author:         chenm
  * CreateDate:     2018/10/27 09:18
  * UpdateUser:     chenm
  * UpdateDate:     2019-04-24 09:18
  * UpdateRemark:   更新说明
  * Version:        1.0
 */
public class SplashActivity extends AppCompatActivity {
    private static final int MSG_COUNT_DOWN = 1; // count down message

    private TextView tvSecond;
    private Thread thread; // count down thread

    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == MSG_COUNT_DOWN) {
                int nSecond = msg.arg1;
                SplashActivity.this.tvSecond.setText(String.format("%d%s", nSecond, getString(R.string.second)));

                if(nSecond == 0) {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 检查权限
        checkPermissions();
    }
    // 检查权限
    private void checkPermissions() {
        List<String> permission = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(SplashActivity.this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permission.add(ACCESS_COARSE_LOCATION);
            }
        }
        if(ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(permission.size() != 0)
            ActivityCompat.requestPermissions(SplashActivity.this, permission.toArray(new String[0]), 1);
        else
            initialize();
    }

    private void initialize() {
        tvSecond = findViewById(R.id.tv_count_second);
        tvSecond.setText(String.format(Locale.getDefault(), "%d%s", SPLASH_ACTIVITY_COUNT_DOWN_SECOND, getString(R.string.second)));

        startCountDown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                for(int result : grantResults) {
                    if(result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "The app exits because of no permission.", Toast.LENGTH_SHORT).show();
                        MyApplication.killProcess();
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

        try {
            stopCountDown();
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        MyApplication.killProcess();
    }

    private void startCountDown() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int nSecond = SPLASH_ACTIVITY_COUNT_DOWN_SECOND;
                try {
                    while (--nSecond >= 0) {
                        Thread.sleep(1000);
                        Message.obtain(handler, MSG_COUNT_DOWN, nSecond, 0).sendToTarget();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        thread.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(DIR_CACHE.exists())
                        FileUtil.cleanDirectory(DIR_CACHE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void stopCountDown() throws InterruptedException{
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            thread.join();
        }
    }

}
