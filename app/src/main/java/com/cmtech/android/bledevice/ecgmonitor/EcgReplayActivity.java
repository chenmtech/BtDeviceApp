package com.cmtech.android.bledevice.ecgmonitor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.model.BleDeviceUtil;
import com.cmtech.dsp.bmefile.BmeFile;
import com.cmtech.dsp.bmefile.BmeFileHead30;
import com.cmtech.dsp.exception.FileException;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;

import static cn.sharesdk.framework.Platform.SHARE_FILE;

public class EcgReplayActivity extends AppCompatActivity {
    private static final String TAG = "EcgReplayActivity";

    private List<File> fileList = new ArrayList<>();
    private EcgFileAdapter fileAdapter;
    private RecyclerView rvFileList;
    private BmeFile selectedFile;

    private NewWaveView ecgView;

    private Button btnEcgShare;
    private Button btnImportFromWX;
    private Button btnEcgDelete;

    // 用于设置EcgWaveView的参数
    private int viewGridWidth = 10;               // 设置ECG View中的每小格有10个像素点
    // 下面两个参数可用来计算View中的xRes和yRes
    private float viewXGridTime = 0.04f;          // 设置ECG View中的横向每小格代表0.04秒，即25格/s，这是标准的ECG走纸速度
    private float viewYGridmV = 0.1f;             // 设置ECG View中的纵向每小格代表0.1mV

    private class ShowTask extends TimerTask {
        @Override
        public void run() {
            synchronized (EcgReplayActivity.this) {
                try {
                    ecgView.addData(selectedFile.readData());
                } catch (FileException e) {
                    e.printStackTrace();
                    cancel();
                }
            }
        }
    }
    private Timer showTimer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgreplay);

        if(!EcgMonitorDevice.ECGFILEDIR.exists()) {
            EcgMonitorDevice.ECGFILEDIR.mkdir();
        }

        File[] files = BleDeviceUtil.listDirBmeFiles(EcgMonitorDevice.ECGFILEDIR);
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return file.getName().compareTo(t1.getName());
            }
        });
        fileList = new ArrayList<>(Arrays.asList(files));


        rvFileList = findViewById(R.id.rvEcgFileList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvFileList.setLayoutManager(layoutManager);
        rvFileList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        fileAdapter = new EcgFileAdapter(fileList, this);
        rvFileList.setAdapter(fileAdapter);
        fileAdapter.notifyDataSetChanged();

        btnEcgShare = findViewById(R.id.btn_ecgreplay_share);
        btnEcgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareBmeFileThroughWechat();
            }
        });

        btnImportFromWX = findViewById(R.id.btn_ecgreplay_import);
        btnImportFromWX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deselectFile();
                File wxFileDir = new File(Environment.getExternalStorageDirectory().getPath()+"/tencent/MicroMsg/Download");
                try {
                    File[] wxFileList = BleDeviceUtil.listDirBmeFiles(wxFileDir);
                    for(File file : wxFileList) {
                        File toFile = FileUtil.getFile(EcgMonitorDevice.ECGFILEDIR, file.getName());

                        for(File file1 : fileList) {
                            if(file1.getName().equalsIgnoreCase(toFile.getName())) {
                                fileList.remove(file1);
                            }
                        }

                        if(toFile.exists()) toFile.delete();

                        FileUtil.moveFile(file, toFile);
                        fileList.add(toFile);
                        fileAdapter.setSelectItem(-1);
                        fileAdapter.notifyDataSetChanged();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnEcgDelete = findViewById(R.id.btn_ecgreplay_delete);
        btnEcgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (selectedFile != null) {
                        BmeFile tmpFile = selectedFile;

                        deselectFile();

                        FileUtil.deleteFile(tmpFile.getFile());
                        fileList.remove(fileAdapter.getSelectItem());
                        fileAdapter.notifyDataSetChanged();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        ecgView = findViewById(R.id.ecg_view);

        Intent intent = getIntent();
        if(intent != null && intent.getStringExtra("fileName") != null) {
            ViseLog.e(intent.getStringExtra("fileName"));
            // 这里需要把文件复制到本应用的文件目录中
            final File file = new File(intent.getStringExtra("fileName"));
            ViseLog.e(file.toString());
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    replayEcgFile(file);
                }
            }, 1000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public synchronized void replayEcgFile(File file) {
        try {
            deselectFile();

            selectedFile = BmeFile.openBmeFile(file.getCanonicalPath());
            int interval = 1000/selectedFile.getFs();
            //int dataLength = selectedFile.availableData();
            //Toast.makeText(EcgReplayActivity.this, "dataLength = "+dataLength, Toast.LENGTH_LONG).show();
            initialEcgView();

            showTimer = new Timer();
            showTimer.scheduleAtFixedRate(new ShowTask(), interval, interval);

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
        ecgView.initView();
        ecgView.startShow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        deselectFile();

    }

    // 用微信分享BME文件
    private void shareBmeFileThroughWechat() {
        if(selectedFile == null) return;

        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(SHARE_FILE);
        sp.setTitle(selectedFile.getFile().getName());
        sp.setText(selectedFile.getFile().getName());
        Bitmap bmp= BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cmiot_16);
        sp.setImageData(bmp);
        sp.setFilePath(selectedFile.getFileName());
        Platform wxPlatform = ShareSDK.getPlatform (Wechat.NAME);
        wxPlatform.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                Toast.makeText(EcgReplayActivity.this, "分享成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                Toast.makeText(EcgReplayActivity.this, "分享错误", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel(Platform platform, int i) {
                Toast.makeText(EcgReplayActivity.this, "分享取消", Toast.LENGTH_SHORT).show();
            }
        });
        // 执行分享
        wxPlatform.share(sp);
    }

    private void deselectFile() {
        if(selectedFile != null) {
            try {
                selectedFile.close();
                selectedFile = null;
                if(showTimer != null) {
                    showTimer.cancel();
                }
                ecgView.initView();
            } catch (FileException e) {
                e.printStackTrace();
            }
        }
    }

}
