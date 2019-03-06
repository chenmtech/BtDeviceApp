package com.cmtech.android.bledevice.ecgmonitor.view;

import android.content.Context;
import android.util.AttributeSet;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.view.ColorRollWaveView;

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

public class EcgFileRollWaveView extends ColorRollWaveView {
    private static final int MIN_SHOW_INTERVAL = 30;          // 最小更新显示的时间间隔，ms，防止更新太快导致程序阻塞

    private EcgFile ecgFile; // 要播放的Ecg文件
    private boolean replaying = false; // 是否正在播放
    private int num = 0; // 当前读取的文件中的第几个数据
    private int interval = 0; // 每次更新显示的时间间隔，为采样间隔的整数倍
    private int dataNumReadEachUpdate = 1; // 每次更新显示时需要读取的数据个数
    private final List<Integer> cacheData = new ArrayList<>(); // 每次更新显示时需要读取的数据缓存
    private final List<Boolean> cacheMarked = new ArrayList<>(); // 标记缓存
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
                                cacheMarked.add(false);
                                if (ecgFile.isEOD()) {
                                    break;
                                }
                            }
                            showData(cacheData, cacheMarked);
                            cacheData.clear();
                            cacheMarked.clear();
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
    public interface IEcgFileRollWaveViewObserver {
        void updateShowState(boolean replaying); // 更新显示状态
        void updateDataLocation(long dataLocation); // 更新当前数据位置
    }
    private IEcgFileRollWaveViewObserver observer; // 观察者


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
    public void showAtInSecond(int second) {
        showAt(second*ecgFile.getFs());
    }

    // 显示指定数据位置信号
    public void showAt(long location) {
        if(location > ecgFile.getDataNum()) {
            location = ecgFile.getDataNum();
        } else if(location < 0) {
            location = 0;
        }
        long begin = location - getDataNumXDirection();
        if(begin < 0) {
            begin = 0;
        }

        ecgFile.seekData((int)begin);
        clearData();
        while(begin++ <= location) {
            try {
                addData(ecgFile.readInt(), false);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        drawDataOnForeCanvas();
        invalidate();

        num = (int)location;

        if(observer != null) {
            observer.updateDataLocation(num);
        }

    }

    // 登记心电回放观察者
    public void registerEcgFileRollWaveViewObserver(IEcgFileRollWaveViewObserver observer) {
        this.observer = observer;
    }

    // 删除心电回放观察者
    public void removeEcgFileRollWaveViewObserver() {
        observer = null;
    }
}
