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
    private static final int MIN_TIME_INTERVAL = 30;          // 最小更新显示的时间间隔，ms，防止更新太快导致程序阻塞

    private ISignalRecord record; // 要播放的信号记录
    private boolean showing = false; // 是否正在播放
    private int curIndex = 0; // 当前读取记录文件中的第几个数据
    private int timeInterval = 0; // 每次更新显示的时间间隔，为采样间隔的整数倍
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
                        if(record.isEOD()) {
                            stop();
                        } else {
                            // 读出数据
                            for (int i = 0; i < dataNumReadEachUpdate; i++, curIndex++) {
                                cacheData.add(record.readData());
                                if (record.isEOD()) {
                                    break;
                                }
                            }
                            addData(cacheData, true);
                            cacheData.clear();
                            if(listener != null) {
                                listener.onDataLocationUpdated(curIndex, record.getSampleRate());
                            }
                        }
                    } catch (IOException e) {
                        stop();
                    }
                }
            });

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
                stop();
            } else {
                start();
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

    public void setRecord(ISignalRecord record) {
        if(record == null) {
            throw new IllegalArgumentException();
        }
        stop();
        this.record = record;
        int sampleInterval = 1000/ record.getSampleRate();
        dataNumReadEachUpdate = (int)(Math.ceil((double) MIN_TIME_INTERVAL /sampleInterval));
        timeInterval = dataNumReadEachUpdate *sampleInterval;
        if(timeInterval == 0) {
            throw new IllegalArgumentException();
        }
        record.seekData(0);
        curIndex = 0;
    }

    public boolean isShowing() {
        return showing;
    }

    public void start() {
        if(!showing && record != null) {
            if(record.isEOD()) {
                record.seekData(0);
                resetView(true);
                curIndex = 0;
            }
            showTimer = new Timer();
            showTimer.scheduleAtFixedRate(new ShowTask(), timeInterval, timeInterval);
            showing = true;
            if(listener != null) {
                listener.onShowStateUpdated(true);
            }
        }
    }

    public void stop() {
        if(showing) {
            showTimer.cancel();
            showTimer = null;
            showing = false;
            if(listener != null) {
                listener.onShowStateUpdated(false);
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
        List<Integer> list = new ArrayList<>();
        while(begin++ <= location) {
            try {
                list.add(record.readData());
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        clearData();
        addData(list, true);

        curIndex = (int)location;

        if(listener != null) {
            listener.onDataLocationUpdated(curIndex, record.getSampleRate());
        }
    }

}
