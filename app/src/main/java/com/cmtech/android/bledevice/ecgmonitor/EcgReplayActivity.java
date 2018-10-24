package com.cmtech.android.bledevice.ecgmonitor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.MainActivity;
import com.cmtech.android.bledeviceapp.adapter.ScanDeviceAdapter;
import com.cmtech.dsp.bmefile.BmeFile;
import com.cmtech.dsp.bmefile.BmeFileHead;
import com.cmtech.dsp.bmefile.BmeFileHead30;
import com.cmtech.dsp.exception.FileException;
import com.vise.log.ViseLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

public class EcgReplayActivity extends AppCompatActivity {
    private static final String TAG = "EcgReplayActivity";

    private static final int MSG_START_REPLAY = 0;
    private static final int MSG_SHOW_DATA = 1;

    private List<File> fileList = new ArrayList<>();
    private EcgFileAdapter fileAdapter;
    private RecyclerView rvFileList;
    private BmeFile selectedFile;

    private WaveView ecgView;

    private Button btnEcgShare;

    // 用于设置EcgWaveView的参数
    private int viewGridWidth = 10;               // 设置ECG View中的每小格有10个像素点
    // 下面两个参数可用来计算View中的xRes和yRes
    private float viewXGridTime = 0.04f;          // 设置ECG View中的横向每小格代表0.04秒，即25格/s，这是标准的ECG走纸速度
    private float viewYGridmV = 0.1f;             // 设置ECG View中的纵向每小格代表0.1mV

    private int interval = 10;

    private Thread showThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (EcgReplayActivity.this) {
                        if (selectedFile != null) {
                            ecgView.addData(selectedFile.readData());
                        }
                    }
                    Thread.sleep(interval);
                } catch (FileException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgreplay);

        if(EcgMonitorDevice.ECGFILEDIR.exists()) {
            File[] files = EcgMonitorDevice.ECGFILEDIR.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File file, File t1) {
                    return file.getName().compareTo(t1.getName());
                }
            });
            fileList = Arrays.asList(files);
        }

        rvFileList = findViewById(R.id.rvEcgFileList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvFileList.setLayoutManager(layoutManager);
        rvFileList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        fileAdapter = new EcgFileAdapter(fileList, this);
        rvFileList.setAdapter(fileAdapter);

        btnEcgShare = findViewById(R.id.btn_ecg_share);
        btnEcgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showShare();
            }
        });

        ecgView = findViewById(R.id.ecg_view);

        showThread.start();
    }

    public synchronized void replayEcgFile(File file) {
        try {
            if (selectedFile != null) {
                selectedFile.close();
            }
            selectedFile = BmeFile.openBmeFile(file.getCanonicalPath());
            interval = 1000/selectedFile.getFs();
            initialEcgView();
        } catch (FileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initialEcgView() {
        if(selectedFile == null) return;
        int sampleRate = selectedFile.getFs();
        int value1mV = ((BmeFileHead30)selectedFile.getBmeFileHead()).getValue1mV();
        int xRes = Math.round(viewGridWidth / (viewXGridTime * sampleRate));   // 计算横向分辨率
        float yRes = value1mV * viewYGridmV / viewGridWidth;                     // 计算纵向分辨率
        ecgView.setRes(xRes, yRes);
        ecgView.setGridWidth(viewGridWidth);
        ecgView.setZeroLocation(0.5);
        ecgView.clearView();
        ecgView.startShow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        if(selectedFile != null) {
            try {
                selectedFile.close();
            } catch (FileException e) {
                e.printStackTrace();
            }
        }

        if(showThread != null && showThread.isAlive()) {
            showThread.interrupt();
        }

    }

    private void showShare() {
        if(selectedFile == null) return;

        OnekeyShare oks = new OnekeyShare();

        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间等使用
        //oks.setTitle("广医蓝牙心电采集信号");
        // titleUrl是标题的网络链接，QQ和QQ空间等使用
        //oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText(selectedFile.getFileName());
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");
        // 确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        //oks.setUrl("http://sharesdk.cn");
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        //oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        //oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        //oks.setSiteUrl("http://sharesdk.cn");


        // 启动分享GUI
        oks.show(this);
    }
}
