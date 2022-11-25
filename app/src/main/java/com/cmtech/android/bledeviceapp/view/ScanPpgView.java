package com.cmtech.android.bledeviceapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.vise.log.ViseLog;

public class ScanPpgView extends ScanSignalView {
    public static final float SECOND_PER_GRID = 0.04f; // 横向每个小栅格代表的秒数，对应于走纸速度
    public static final float PHYSIC_PER_GRID = 0.1f; // 纵向每个小栅格代表的物理量，对应于灵敏度
    public static final int PIXEL_PER_GRID = 10; // 每个小栅格包含的像素个数

    private class MyGestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }
        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }
        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            setShowWave(!showWave);
            return false;
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float v, float v1) {
            float scale = v1/getHeight();
            setYScale(1-scale);
            ViseLog.e("height=" + getHeight() + "scale=" + scale + "yDist:" + v1);
            return false;
        }
        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    public ScanPpgView(Context context) {
        super(context);
        gestureDetector = new GestureDetector(context, new MyGestureListener());
        gestureDetector.setIsLongpressEnabled(false);
    }

    public ScanPpgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, new MyGestureListener());
        gestureDetector.setIsLongpressEnabled(false);
    }

    public void setup(int sampleRate, int gain) {
        super.setup(1, sampleRate, new int[]{gain}, SECOND_PER_GRID, PHYSIC_PER_GRID, PIXEL_PER_GRID);
    }

    private void setYScale(float yScale) {
        this.valuePerPixel[0] *= yScale;
    }
}
