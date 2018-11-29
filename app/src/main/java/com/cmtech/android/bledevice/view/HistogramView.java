package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class HistogramView extends View {
    private String NAME = "统计直方图";

    private List<Count> counts;
    private Paint paint;
    private float startX;
    private float space;
    private float width;
    private float max;

    public HistogramView(Context context) {
        super(context);
        init();
    }

    public HistogramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HistogramView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public class Count {
        private String name;
        private float number;
        private int color;

        public Count(String name, float number, int color) {
            this.name = name;
            this.number = number;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public float getNumber() {
            return number;
        }

        public int getColor() {
            return color;
        }
    }

    private void init() {
        counts = new ArrayList<>();
        Count count = new Count("一组", 7.0f, Color.GREEN);
        counts.add(count);
        count = new Count("二组", 0.0f, Color.GREEN);
        counts.add(count);
        count = new Count("三组", 11.0f, Color.GREEN);
        counts.add(count);
        count = new Count("四组", 13.0f, Color.GREEN);
        counts.add(count);
        count = new Count("二组", 9.0f, Color.GREEN);
        counts.add(count);
        count = new Count("三组", 11.0f, Color.GREEN);
        counts.add(count);
        count = new Count("四组", 13.0f, Color.GREEN);
        counts.add(count);
        count = new Count("二组", 9.0f, Color.GREEN);
        counts.add(count);
        count = new Count("三组", 11.0f, Color.GREEN);
        counts.add(count);
        count = new Count("四组", 13.0f, Color.GREEN);
        counts.add(count);
        count = new Count("二组", 9.0f, Color.GREEN);
        counts.add(count);
        count = new Count("三组", 11.0f, Color.GREEN);
        counts.add(count);
        count = new Count("四组", 13.0f, Color.GREEN);
        counts.add(count);
        count = new Count("二组", 9.0f, Color.GREEN);
        counts.add(count);
        count = new Count("三组", 11.0f, Color.GREEN);
        counts.add(count);
        count = new Count("四组", 13.0f, Color.GREEN);
        counts.add(count);
        count = new Count("二组", 9.0f, Color.GREEN);
        counts.add(count);
        count = new Count("三组", 11.0f, Color.GREEN);
        counts.add(count);
        count = new Count("四组", 13.0f, Color.GREEN);
        counts.add(count);
        max = Float.MIN_VALUE;
        for (Count c : counts) {
            max = Math.max(max, c.getNumber());
        }
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.BLUE);
        paint.setTextSize(72);
        canvas.drawText(NAME, (canvas.getWidth() - paint.measureText(NAME)) / 2, canvas.getHeight() * 0.7f, paint);

        canvas.translate(canvas.getWidth() * 0.1f, canvas.getHeight() * 0.6f); // 将画图原点移动直方图的原点位置
        //直方图的宽度
        width = (canvas.getWidth() * 0.6f - 100) / counts.size() * 0.6f;
        //直方图之间的间距
        space = (canvas.getWidth() * 0.9f - 100) / counts.size() * 0.2f;

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(0, 0, canvas.getWidth() * 0.8f, 0, paint);   // 画x轴
        canvas.drawLine(0, 0, 0, -canvas.getHeight() * 0.6f, paint); // 画y轴

        startX = 0f;

        paint.setTextSize(36);
        paint.setStyle(Paint.Style.FILL);
        for (Count count : counts) {
            paint.setColor(count.getColor());
            canvas.drawRect(startX + space, -(count.getNumber() / max * canvas.getHeight() * 0.6f), startX + space + width, 0, paint);
            paint.setColor(Color.BLUE);
            canvas.drawText(count.getName(), startX + space + (width - paint.measureText(count.getName())) / 2, 40, paint);
            startX += width + space;
        }
    }
}