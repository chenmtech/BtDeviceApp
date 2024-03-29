package com.cmtech.android.bledeviceapp.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.vise.log.ViseLog;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * RollRecordView: 用于播放信号的卷轴滚动式的波形显示视图
 * Created by bme on 2018/12/06.
 */

public class RollRecordView extends RollWaveView {
    // 最小更新显示的时间间隔，ms，防止更新太快导致程序阻塞
    private static final int MIN_TIME_INTERVAL = 30;

    private static final int MSG_UPDATE_VIEW = 1;
    private static final int MSG_UPDATE_SHOW_STATE = 2;
    private static final int MSG_STOP_SHOW = 3;

    // 要显示的记录
    protected BasicRecord record;

    // 是否正在播放
    private boolean showing = false;

    // 当前读取记录文件中的第几个数据
    protected int curPos = 0;

    // 每次更新显示时需要读取的数据个数
    private int dataNumReadEachUpdate = 1;

    // 定时更新显示线程池
    private ScheduledExecutorService showExecutor;

    private final Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if(msg.what == MSG_UPDATE_VIEW) {
                int curIndex = msg.arg1;
                showView();
                if(listener != null) {
                    listener.onDataLocationUpdated(curIndex, curIndex/record.getSampleRate());
                }
            } else if(msg.what == MSG_UPDATE_SHOW_STATE) {
                boolean show = (msg.arg1 == 1);
                if(listener != null) {
                    listener.onShowStateUpdated(show);
                }
            } else if(msg.what == MSG_STOP_SHOW) {
                stopShow();
            }
            return true;
        }
    });

    // 周期定时显示任务
    private class ShowTask implements Runnable {
        @Override
        public void run() {
            try {
                if(record.isEOD()) {
                    Message.obtain(handler, MSG_STOP_SHOW).sendToTarget();
                } else {
                    // 读出数据
                    for (int i = 0; i < dataNumReadEachUpdate; i++, curPos++) {
                        addData(record.readData(), false);
                        if (record.isEOD()) {
                            break;
                        }
                    }
                    Message.obtain(handler, MSG_UPDATE_VIEW, curPos, 0).sendToTarget();
                }
            } catch (IOException e) {
                stopShow();
            }
        }
    }

    private GestureDetector gestureDetector;
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
            stopShow();
            showAt((long)(curPos +v));
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
        if(gestureDetector != null)
            gestureDetector.onTouchEvent(event);
        return true;
    }

    public void setup(BasicRecord record, float[] zeroLocs, float secPerGrid, float[] physicValuePerGrid, int pixelPerGrid) {
        assert record.getChannelNum() == zeroLocs.length;

        setRecord(record);

        // 计算横向分辨率
        int pixelPerData = Math.round(pixelPerGrid / (secPerGrid * record.getSampleRate())); // 计算横向分辨率

        // 计算纵向分辨率
        float[] aduPerPixel = new float[waveNum];
        List<Integer> gain = record.getGain();
        for(int i = 0; i < waveNum; i++)
            aduPerPixel[i] = gain.get(i) * physicValuePerGrid[i] / pixelPerGrid;

        setup(zeroLocs, aduPerPixel, pixelPerData, pixelPerGrid);
    }

    public void setup(BasicRecord record, float secPerGrid, float[] physicValuePerGrid, int pixelPerGrid) {
        // 用通道数计算零值位置
        float[] zeroLocs = new float[record.getChannelNum()];
        for(int i = 0; i < zeroLocs.length; i++)
            zeroLocs[i] = (1.0f+2*i) / (2*zeroLocs.length);

        setup(record, zeroLocs, secPerGrid, physicValuePerGrid, pixelPerGrid);
    }

    private void setRecord(BasicRecord record) {
        if(record == null) {
            throw new IllegalArgumentException();
        }
        stopShow();
        this.record = record;
        curPos = 0;
        record.seek(curPos);

        int sampleInterval = 1000/ record.getSampleRate();
        dataNumReadEachUpdate = (int)(Math.ceil((double) MIN_TIME_INTERVAL /sampleInterval));
    }

    public boolean isShowing() {
        return showing;
    }

    public void startShow() {
        if(!showing && record != null) {
            if(record.isEOD()) {
                curPos = 0;
                record.seek(curPos);
                clearData();
                resetView(true);
            }

            int sampleInterval = 1000/ record.getSampleRate();
            int timeInterval = dataNumReadEachUpdate * sampleInterval;
            if(timeInterval == 0) {
                throw new IllegalArgumentException();
            }

            showExecutor = Executors.newScheduledThreadPool(1);
            showExecutor.scheduleAtFixedRate(new ShowTask(), timeInterval, timeInterval, TimeUnit.MILLISECONDS);
            showing = true;

            if(listener != null) {
                Message.obtain(handler, MSG_UPDATE_SHOW_STATE, 1, 0).sendToTarget();
            }
        }
    }

    public void stopShow() {
        if(showing) {
            ViseLog.e("停止RollRecordView");

            ExecutorUtil.shutdownAndAwaitTerminate(showExecutor);

            handler.removeCallbacksAndMessages(null);

            showing = false;
            if(listener != null) {
                Message.obtain(handler, MSG_UPDATE_SHOW_STATE, 0, 0).sendToTarget();
            }
        }
    }

    // 显示指定秒数的信号
    public void showAtSecond(int second) {
        showAt((long) second * record.getSampleRate());
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

        record.seek((int)begin);
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
        curPos = (int)location;

        if(listener != null) {
            listener.onDataLocationUpdated(curPos, curPos /record.getSampleRate());
        }
    }

    public void setGestureDetector(GestureDetector detector) {
        gestureDetector = detector;
    }

}
