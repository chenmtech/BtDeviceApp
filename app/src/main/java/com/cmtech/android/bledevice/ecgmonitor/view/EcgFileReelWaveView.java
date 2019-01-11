package com.cmtech.android.bledevice.ecgmonitor.view;

import android.content.Context;
import android.util.AttributeSet;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.view.ReelWaveView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.vise.utils.handler.HandlerUtil.runOnUiThread;

public class EcgFileReelWaveView extends ReelWaveView {
    private static final int MIN_SHOW_INTERVAL = 30;          // 最小更新显示的时间间隔，ms，防止更新太快导致程序阻塞

    private EcgFile ecgFile;            // 要播放显示的Ecg文件

    private boolean replaying = false;
    public boolean isReplaying() {
        return replaying;
    }

    private int num = 0;                // 当前读取的文件中的第几个数据

    private int interval = 0;                       // 每次更新显示的时间间隔，为采样间隔的整数倍

    private int dataNumReadEachShow = 1;                            // 每次更新显示时需要读取的数据个数
    private final List<Integer> cacheData = new ArrayList<>();      // 每次更新显示时需要读取的数据缓存

    private class ShowTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(ecgFile.isEOD()) {
                            stopShow();
                        } else {
                            for (int i = 0; i < dataNumReadEachShow; i++, num++) {
                                cacheData.add(ecgFile.readInt());
                                if (ecgFile.isEOD()) {
                                    break;
                                }
                            }
                            showData(cacheData);
                            cacheData.clear();
                            if(observer != null) {
                                observer.updateDataLocation(num);
                            }

                        }
                    } catch (IOException e) {
                        stopShow();
                    }
                }
            });

        }
    }
    private Timer showTimer;

    public interface IEcgFileReelWaveViewObserver {
        void updateShowState(boolean replaying);

        void updateDataLocation(long dataLocation);
    }

    private IEcgFileReelWaveViewObserver observer;


    public EcgFileReelWaveView(Context context) {
        super(context);
    }

    public EcgFileReelWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEcgFile(EcgFile ecgFile) {
        stopShow();
        this.ecgFile = ecgFile;
        int sampleInterval = 1000/ecgFile.getFs();
        dataNumReadEachShow = (int)(Math.ceil((double) MIN_SHOW_INTERVAL /sampleInterval));
        interval = dataNumReadEachShow *sampleInterval;
        ecgFile.seekData(0);
    }

    public void startShow() {
        if(!replaying) {
            if(ecgFile.isEOD()) {
                ecgFile.seekData(0);
                clearData();
                num = 0;
            }
            showTimer = new Timer();
            showTimer.scheduleAtFixedRate(new ShowTask(), interval, interval);
            replaying = true;
            if(observer != null) {
                observer.updateShowState(true);
            }
        }
    }

    public void stopShow() {
        if(replaying) {
            showTimer.cancel();
            showTimer = null;
            replaying = false;
            if(observer != null) {
                observer.updateShowState(false);
            }
        }
    }

    public void switchState() {
        if(replaying) {
            stopShow();
        } else {
            startShow();
        }
    }

    // 显示指定秒数的信号
    public void showAtLocation(int second) {
        long numAtLocation = second*ecgFile.getFs();
        if(numAtLocation > ecgFile.getDataNum()) {
            numAtLocation = ecgFile.getDataNum();
        }
        long begin = numAtLocation - getDataNumXDirection();
        if(begin < 0) {
            begin = 0;
        }

        ecgFile.seekData((int)begin);
        viewData.clear();
        while(begin++ <= numAtLocation) {
            try {
                viewData.add(ecgFile.readInt());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        drawDataOnForeCanvas();

        invalidate();

        num = (int)numAtLocation;

        if(observer != null) {
            observer.updateDataLocation(num);
        }

    }

    // 登记心电回放观察者
    public void registerEcgFileReelWaveViewObserver(IEcgFileReelWaveViewObserver observer) {
        this.observer = observer;
    }

    // 删除心电回放观察者
    public void removeEcgFileReelWaveViewObserver() {
        observer = null;
    }
}
