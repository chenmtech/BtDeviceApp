package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.cmtech.android.bledevice.record.ISignalRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.vise.utils.handler.HandlerUtil.runOnUiThread;

/**
 * RollSignalRecordWaveView: 用于播放信号记录的卷轴滚动式的波形显示视图
 * Created by bme on 2018/12/06.
 */

public class RollSignalRecordWaveView extends RollWaveView {
    private static final int MIN_SHOW_INTERVAL = 30;          // 最小更新显示的时间间隔，ms，防止更新太快导致程序阻塞

    private ISignalRecord signalRecord; // 要播放的信号记录
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
                        if(signalRecord.isEOD()) {
                            stopShow();
                        } else {
                            // 读出数据
                            for (int i = 0; i < dataNumReadEachUpdate; i++, num++) {
                                cacheData.add(signalRecord.readData());
                                cacheMarked.add(false);
                                if (signalRecord.isEOD()) {
                                    break;
                                }
                            }
                            showData(cacheData);
                            cacheData.clear();
                            cacheMarked.clear();
                            if(listener != null) {
                                listener.onDataLocationUpdated(num, signalRecord.getSampleRate());
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

    public RollSignalRecordWaveView(Context context) {
        super(context);
        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);
    }

    public RollSignalRecordWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    public void setSignalRecord(ISignalRecord signalRecord) {
        stopShow();
        this.signalRecord = signalRecord;
        int sampleInterval = 1000/signalRecord.getSampleRate();
        dataNumReadEachUpdate = (int)(Math.ceil((double) MIN_SHOW_INTERVAL /sampleInterval));
        interval = dataNumReadEachUpdate *sampleInterval;
        signalRecord.seekData(0);
        num = 0;
    }

    public boolean isStart() {
        return replaying;
    }

    public void startShow() {
        if(!replaying && signalRecord != null && interval > 0) {
            if(signalRecord.isEOD()) {
                signalRecord.seekData(0);
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
        showAt(second* signalRecord.getSampleRate());
    }

    // 显示指定数据位置信号
    public void showAt(long location) {
        if(signalRecord == null) return;

        if(location >= signalRecord.getDataNum()) {
            location = signalRecord.getDataNum()-1;
        } else if(location < 0) {
            location = 0;
        }
        long begin = location - getDataNumInView();
        if(begin < 0) {
            begin = 0;
        }

        signalRecord.seekData((int)begin);
        clearData();
        while(begin++ <= location) {
            try {
                addData(signalRecord.readData());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        drawDataOnForeCanvas();
        invalidate();

        num = (int)location;

        if(listener != null) {
            listener.onDataLocationUpdated(num, signalRecord.getSampleRate());
        }
    }

}
