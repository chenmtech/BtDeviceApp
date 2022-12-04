package com.cmtech.android.bledeviceapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import java.util.Arrays;

public class ScanEcgView extends ScanSignalView {
    public static final float SECOND_PER_GRID = 0.04f; // 横向每个小栅格代表的秒数，对应于走纸速度
    public static final float MV_PER_GRID = 0.1f; // 纵向每个小栅格代表的mV
    public static final int PIXEL_PER_GRID = 10; // 每个小栅格包含的像素个数

    private static final Paint ANN_PAINT = new Paint();
    private Rect bound = new Rect();

    private String annDescription = "";

    public ScanEcgView(Context context) {
        super(context);

        ANN_PAINT.setTextSize(40);
        ANN_PAINT.setColor(Color.WHITE);
        ANN_PAINT.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));
    }

    public ScanEcgView(Context context, AttributeSet attrs) {
        super(context, attrs);

        ANN_PAINT.setTextSize(40);
        ANN_PAINT.setColor(Color.WHITE);
        ANN_PAINT.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));
    }

    public void setup(int waveNum, int sampleRate, int[] gain) {
        float[] mvPerGrid = new float[waveNum];
        Arrays.fill(mvPerGrid, MV_PER_GRID);
        super.setup(waveNum, sampleRate, gain, SECOND_PER_GRID, mvPerGrid, PIXEL_PER_GRID);
    }

    public void setup(int sampleRate, int gain) {
        setup(1, sampleRate, new int[]{gain});
    }

    // 显示一条注解
    public void showAnnotation(String annDescription) {
        this.annDescription = annDescription;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!annDescription.equals("")) {
            waveCanvas.drawLine(curX,0,curX,viewHeight, ANN_PAINT);
            ANN_PAINT.getTextBounds(annDescription, 0, annDescription.length(), bound);
            waveCanvas.drawText(annDescription, curX-bound.width(), viewHeight-10, ANN_PAINT);
            annDescription = "";
        }
        super.onDraw(canvas);
    }
}
