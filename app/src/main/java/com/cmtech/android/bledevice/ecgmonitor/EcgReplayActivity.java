package com.cmtech.android.bledevice.ecgmonitor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.cmtech.android.bledeviceapp.R;
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

public class EcgReplayActivity extends AppCompatActivity {
    private static final String TAG = "EcgReplayActivity";

    private List<File> fileList = new ArrayList<>();
    private EcgFileAdapter fileAdapter;
    private RecyclerView rvFileList;

    private WaveView ecgView;

    // 用于设置EcgWaveView的参数
    private int viewGridWidth = 10;               // 设置ECG View中的每小格有10个像素点
    // 下面两个参数可用来计算View中的xRes和yRes
    private float viewXGridTime = 0.04f;          // 设置ECG View中的横向每小格代表0.04秒，即25格/s，这是标准的ECG走纸速度
    private float viewYGridmV = 0.1f;             // 设置ECG View中的纵向每小格代表0.1mV

    private class ReplayThread extends Thread {
        private int interval;
        private BmeFile file;

        public ReplayThread(BmeFile file) {
            this.file = file;
            interval = 1000/file.getFs();
        }

        @Override
        public void run() {
            while(true) {
                try {
                    ecgView.addData(file.readData());
                    try {
                        sleep(interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (FileException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private ReplayThread workThread;

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

        ecgView = findViewById(R.id.ecg_view);

    }

    public void replayEcgFile(File file) {
        if(workThread != null && workThread.isAlive()) {
            workThread.interrupt();

        }

        try {
            BmeFile ecgFile = BmeFile.openBmeFile(file.getCanonicalPath());

            //ViseLog.e(ecgFile.getBmeFileHead().toString());

            int sampleRate = ecgFile.getFs();
            int value1mV = ((BmeFileHead30)ecgFile.getBmeFileHead()).getValue1mV();
            int xRes = Math.round(viewGridWidth / (viewXGridTime * sampleRate));   // 计算横向分辨率
            float yRes = value1mV * viewYGridmV / viewGridWidth;                     // 计算纵向分辨率
            ecgView.setRes(xRes, yRes);
            ecgView.setGridWidth(viewGridWidth);
            ecgView.setZeroLocation(0.5);
            ecgView.clearView();
            ecgView.startShow();

            workThread = new ReplayThread(ecgFile);
            workThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (FileException e) {
            e.printStackTrace();
        }
    }
}
