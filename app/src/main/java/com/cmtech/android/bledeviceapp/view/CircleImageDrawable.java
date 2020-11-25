package com.cmtech.android.bledeviceapp.view;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.vise.log.ViseLog;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.view
 * ClassName:      CircleDrawable
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/11/26 上午5:33
 * UpdateUser:     更新者
 * UpdateDate:     2020/11/26 上午5:33
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class CircleImageDrawable extends Drawable {
    private Paint mPaint;
    private int mWidth;
    private int mHeight;
    private int radius;
    private Bitmap mBitmap ;

    public CircleImageDrawable(Bitmap bitmap)
    {
        mBitmap = bitmap ;
        BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setShader(bitmapShader);
        mWidth = mBitmap.getWidth();
        mHeight = mBitmap.getHeight();
        radius = Math.min(mWidth, mHeight)/2;
    }

    @Override
    public void draw(Canvas canvas)
    {
        canvas.drawCircle(mWidth / 2, mWidth / 2, radius, mPaint);
    }

    @Override
    public int getIntrinsicWidth()
    {
        return mWidth;
    }

    @Override
    public int getIntrinsicHeight()
    {
        return mHeight;
    }

    @Override
    public void setAlpha(int alpha)
    {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf)
    {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity()
    {
        return PixelFormat.TRANSLUCENT;
    }
}
