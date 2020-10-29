package com.cmtech.android.bledeviceapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.cmtech.android.bledeviceapp.data.record.ISignalRecord;
import com.vise.log.ViseLog;
import com.vise.utils.handler.HandlerUtil;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static com.vise.utils.handler.HandlerUtil.runOnUiThread;

/**
 * RollRecordView: 用于播放信号记录的卷轴滚动式的波形显示视图
 * Created by bme on 2018/12/06.
 */

public class RollRecordView extends RollWaveView {
    private static final int MIN_TIME_INTERVAL = 30;          // 最小更新显示的时间间隔，ms，防止更新太快导致程序阻塞

    private ISignalRecord record; // 要播放的信号记录
    private boolean showing = false; // 是否正在播放
    private int curIndex = 0; // 当前读取记录文件中的第几个数据
    private int dataNumReadEachUpdate = 1; // 每次更新显示时需要读取的数据个数

    private final Runnable updateViewRunnable = new Runnable() {
        @Override
        public void run() {
            showView();
            if(listener != null) {
                listener.onDataLocationUpdated(curIndex, curIndex/record.getSampleRate());
            }
        }
    };

    // 定时周期显示任务
    private class ShowTask extends TimerTask {
        @Override
        public void run() {
            try {
                if(record.isEOD()) {
                    stopShow();
                } else {
                    // 读出数据
                    for (int i = 0; i < dataNumReadEachUpdate; i++, curIndex++) {
                        addData(record.readData(), false);
                        if (record.isEOD()) {
                            break;
                        }
                    }
                    runOnUiThread(updateViewRunnable);
                }
            } catch (IOException e) {
                stopShow();
            }
        }
    }
    private Timer showTimer; // 定时器

    private final GestureDetector gestureDetector;
    private final GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            if(showing) {
                stopShow();
            } else {
                startShow();
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float v, float v1) {
            showAt((long)(curIndex +v));
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

    public RollRecordView(Context context) {
        super(context);
        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);
    }

    public RollRecordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    public void setup(ISignalRecord record, float zeroLocation, float secondPerGrid, float mvPerGrid, int pixelPerGrid) {
        setRecord(record);
        int pixelPerData = Math.round(pixelPerGrid / (secondPerGrid * record.getSampleRate())); // 计算横向分辨率
        float valuePerPixel = record.getCaliValue() * mvPerGrid / pixelPerGrid; // 计算纵向分辨率
        setResolution(pixelPerData, valuePerPixel);
        setPixelPerGrid(pixelPerGrid);
        setZeroLocation(zeroLocation);
        resetView(true);
    }

    public void setRecord(ISignalRecord record) {
        if(record == null) {
            throw new IllegalArgumentException();
        }
        stopShow();
        this.record = record;
        curIndex = 0;
        record.seekData(curIndex);

        int sampleInterval = 1000/ record.getSampleRate();
        dataNumReadEachUpdate = (int)(Math.ceil((double) MIN_TIME_INTERVAL /sampleInterval));
    }

    public boolean isShowing() {
        return showing;
    }

    public void startShow() {
        if(!showing && record != null) {
            ViseLog.e("启动RollRecordView");
            if(record.isEOD()) {
                curIndex = 0;
                record.seekData(curIndex);
                resetView(true);
            }

            int sampleInterval = 1000/ record.getSampleRate();
            int timeInterval = dataNumReadEachUpdate * sampleInterval;
            if(timeInterval == 0) {
                throw new IllegalArgumentException();
            }

            showTimer = new Timer();
            showTimer.scheduleAtFixedRate(new ShowTask(), timeInterval, timeInterval);
            showing = true;

            if(listener != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onShowStateUpdated(true);
                    }
                });
            }
        }
    }

    public void stopShow() {
        if(showing) {
            ViseLog.e("停止RollRecordView");
            showTimer.cancel();
            showTimer = null;
            HandlerUtil.removeRunnable(updateViewRunnable);
            showing = false;
            if(listener != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onShowStateUpdated(false);
                    }
                });
            }
        }
    }

    // 显示指定秒数的信号
    public void showAtSecond(int second) {
        showAt(second * record.getSampleRate());
    }

    // 显示指定数据位置信号
    public void showAt(long location) {
        if(record == null) return;
        if(location < 0) return;

        if(location >= record.getDataNum()) {
            location = record.getDataNum()-1;
        }

        long begin = location - getDataNumInView();
        if(begin < 0) {
            begin = 0;
        }

        record.seekData((int)begin);
        clearData();
        while(begin++ <= location) {
            try {
                addData(record.readData(), false);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        showView();
        curIndex = (int)location;

        if(listener != null) {
            listener.onDataLocationUpdated(curIndex, curIndex/record.getSampleRate());
        }
    }

}
