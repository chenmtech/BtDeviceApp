package com.cmtech.android.bledevice.ecgmonitor.ecgfile;


import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;
import com.cmtech.dsp.bmefile.BmeFileHead30;
import com.cmtech.dsp.exception.FileException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.vise.utils.handler.HandlerUtil.runOnUiThread;

public class EcgFileReplayModel {
    private EcgFile ecgFile;
    private boolean replayStatus = false;

    // 设备观察者
    private IEcgFileReplayObserver observer;

    // 用于设置EcgWaveView的参数
    private int viewGridWidth = 10;               // 设置ECG View中的每小格有10个像素点
    // 下面两个参数可用来计算View中的xRes和yRes
    private float viewXGridTime = 0.04f;          // 设置ECG View中的横向每小格代表0.04秒，即25格/s，这是标准的ECG走纸速度
    private float viewYGridmV = 0.1f;             // 设置ECG View中的纵向每小格代表0.1mV


    private class ShowTask extends TimerTask {
        @Override
        public void run() {
            try {
                showEcgData(ecgFile.readData());
            } catch (FileException e) {
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopShow();
                    }
                });
            }
        }
    }
    private Timer showTimer;


    public EcgFileReplayModel(String ecgFileName) {
        try {
            ecgFile = EcgFile.openBmeFile(ecgFileName);
        } catch (FileException e) {
            e.printStackTrace();
        }
    }

    public EcgFile getEcgFile() {
        return ecgFile;
    }

    public List<EcgFileComment> getCommentList() {
        return ecgFile.getEcgFileHead().getCommentList();
    }

    public void replay() {
        initEcgView();

        int interval = 1000/ecgFile.getFs();

        startShow(interval);
    }

    public void addComment(String comment) {
        String commentator = UserAccountManager.getInstance().getUserAccount().getName();
        long timeCreated = new Date().getTime();
        ecgFile.addComment(new EcgFileComment(commentator, timeCreated, comment));
        updateCommentList();
    }

    private void startShow(int interval) {
        if(!replayStatus) {
            showTimer = new Timer();
            showTimer.scheduleAtFixedRate(new ShowTask(), interval, interval);
            replayStatus = true;
        }
    }

    private void stopShow() {
        if(replayStatus) {
            showTimer.cancel();
            showTimer = null;
            replayStatus = false;
        }
    }


    // 登记心电回放观察者
    public void registerEcgFileReplayObserver(IEcgFileReplayObserver observer) {
        this.observer = observer;
    }

    // 删除心电回放观察者
    public void removeEcgFileObserver() {
        observer = null;
    }

    private void updateCommentList() {
        if(observer != null) {
            observer.updateCommentList();
        }
    }

    private void initEcgView() {
        if(observer != null) {
            int sampleRate = ecgFile.getFs();
            int value1mV = ((BmeFileHead30)ecgFile.getBmeFileHead()).getCalibrationValue();
            int xRes = Math.round(viewGridWidth / (viewXGridTime * sampleRate));   // 计算横向分辨率
            float yRes = value1mV * viewYGridmV / viewGridWidth;                     // 计算纵向分辨率
            observer.initEcgView(xRes, yRes, viewGridWidth, 0.5);
        }
    }

    private void showEcgData(int data) {
        if(observer != null) {
            observer.showEcgData(data);
        }
    }

}
