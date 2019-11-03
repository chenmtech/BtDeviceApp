package com.cmtech.android.bledevice.ecgmonitor.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgRecord;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.viewcomponent.ColorRollWaveView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.cmtech.android.bledevice.ecgmonitor.view.ScanEcgView.MV_PER_GRID;
import static com.cmtech.android.bledevice.ecgmonitor.view.ScanEcgView.PIXEL_PER_GRID;
import static com.cmtech.android.bledevice.ecgmonitor.view.ScanEcgView.SECOND_PER_GRID;
import static com.vise.utils.handler.HandlerUtil.runOnUiThread;

/**
 * EcgFileRollWaveView: 用于播放心电记录的卷轴滚动式的波形显示视图
 * Created by bme on 2018/12/06.
 */

public class RollEcgRecordWaveView extends ColorRollWaveView {
    private static final int MIN_SHOW_INTERVAL = 30;          // 最小更新显示的时间间隔，ms，防止更新太快导致程序阻塞

    private EcgRecord ecgRecord; // 要播放的Ecg文件
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
                        if(ecgRecord.isEOD()) {
                            stopShow();
                        } else {
                            // 读出数据
                            for (int i = 0; i < dataNumReadEachUpdate; i++, num++) {
                                cacheData.add(ecgRecord.readInt());
                                cacheMarked.add(false);
                                if (ecgRecord.isEOD()) {
                                    break;
                                }
                            }
                            showData(cacheData, cacheMarked);
                            cacheData.clear();
                            cacheMarked.clear();
                            if(listener != null) {
                                listener.onDataLocationUpdated(num, ecgRecord.getSampleRate());
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

    private GestureDetector gestureDetector;
    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            if(isStart()) {
                stopShow();
            } else {
                startShow();
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float v, float v1) {
            showAt((long)(num+v));
            return true;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    };

    public RollEcgRecordWaveView(Context context) {
        super(context);
        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);
    }

    public RollEcgRecordWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    public void setEcgRecord(EcgRecord ecgRecord) {
        stopShow();
        this.ecgRecord = ecgRecord;
        int sampleInterval = 1000/ecgRecord.getSampleRate();
        dataNumReadEachUpdate = (int)(Math.ceil((double) MIN_SHOW_INTERVAL /sampleInterval));
        interval = dataNumReadEachUpdate *sampleInterval;
        ecgRecord.seekData(0);
        num = 0;

        intialShowSetup();
    }

    private void intialShowSetup() {
        int value1mV = ecgRecord.getCaliValue();
        int pixelPerData = Math.round(PIXEL_PER_GRID / (SECOND_PER_GRID * ecgRecord.getSampleRate())); // 计算横向分辨率
        float valuePerPixel = value1mV * MV_PER_GRID / PIXEL_PER_GRID; // 计算纵向分辨率
        setResolution(pixelPerData, valuePerPixel);
        setPixelPerGrid(PIXEL_PER_GRID);
        clearData();
        initialize();
    }

    public boolean isStart() {
        return replaying;
    }

    public void startShow() {
        if(!replaying && ecgRecord != null) {
            if(ecgRecord.isEOD()) {
                ecgRecord.seekData(0);
                clearData();
                num = 0;
            }
            showTimer = new Timer();
            showTimer.scheduleAtFixedRate(new ShowTask(), interval, interval);
            replaying = true;
            if(listener != null) {
                listener.onShowStateUpdated(true);
            }
        }
    }

    public void stopShow() {
        if(replaying) {
            showTimer.cancel();
            showTimer = null;
            replaying = false;
            if(listener != null) {
                listener.onShowStateUpdated(false);
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
    public void showAtSecond(int second) {
        showAt(second* ecgRecord.getSampleRate());
    }

    // 显示指定数据位置信号
    public void showAt(long location) {
        if(ecgRecord == null) return;

        if(location >= ecgRecord.getDataNum()) {
            location = ecgRecord.getDataNum()-1;
        } else if(location < 0) {
            location = 0;
        }
        long begin = location - getDataNumXDirection();
        if(begin < 0) {
            begin = 0;
        }

        ecgRecord.seekData((int)begin);
        clearData();
        while(begin++ <= location) {
            try {
                addData(ecgRecord.readInt(), false);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        drawDataOnForeCanvas();
        invalidate();

        num = (int)location;

        if(listener != null) {
            listener.onDataLocationUpdated(num, ecgRecord.getSampleRate());
        }
    }

}
