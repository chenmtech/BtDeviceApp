package com.cmtech.android.bledevice.ecgmonitor.view;

import android.content.Context;
import android.util.AttributeSet;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.view.RollWaveView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.vise.utils.handler.HandlerUtil.runOnUiThread;

/**
 * EcgFileRollWaveView: 用于播放心电文件的卷轴滚动式的波形显示视图
 * Created by bme on 2018/12/06.
 */

public class EcgFileRollWaveView extends RollWaveView {
    private static final int MIN_SHOW_INTERVAL = 30;          // 最小更新显示的时间间隔，ms，防止更新太快导致程序阻塞

    private EcgFile ecgFile; // 要播放的Ecg文件
    private boolean replaying = false; // 是否正在播放
    private int num = 0; // 当前读取的文件中的第几个数据
    private int interval = 0; // 每次更新显示的时间间隔，为采样间隔的整数倍
    private int dataNumReadEachUpdate = 1; // 每次更新显示时需要读取的数据个数
    private final List<Integer> cacheData = new ArrayList<>(); // 每次更新显示时需要读取的数据缓存
    // 定时周期显示任务
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
                            // 读出数据
                            for (int i = 0; i < dataNumReadEachUpdate; i++, num++) {
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
    private Timer showTimer; // 定时器
    // 观察者接口
    public interface IEcgFileReelWaveViewObserver {
        void updateShowState(boolean replaying); // 更新显示状态
        void updateDataLocation(long dataLocation); // 更新当前数据位置
    }
    private IEcgFileReelWaveViewObserver observer; // 观察者


    public EcgFileRollWaveView(Context context) {
        super(context);
    }

    public EcgFileRollWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEcgFile(EcgFile ecgFile) {
        stopShow();
        this.ecgFile = ecgFile;
        int sampleInterval = 1000/ecgFile.getFs();
        dataNumReadEachUpdate = (int)(Math.ceil((double) MIN_SHOW_INTERVAL /sampleInterval));
        interval = dataNumReadEachUpdate *sampleInterval;
        ecgFile.seekData(0);
    }

    public boolean isReplaying() {
        return replaying;
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
    public void showSecondLocation(int second) {
        showLocation(second*ecgFile.getFs());
    }

    // 显示指定数据位置信号
    public void showLocation(long numAtLocation) {
        if(numAtLocation > ecgFile.getDataNum()) {
            numAtLocation = ecgFile.getDataNum();
        } else if(numAtLocation < 0) {
            numAtLocation = 0;
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
