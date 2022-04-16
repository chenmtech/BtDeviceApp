package com.cmtech.android.bledeviceapp.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.vise.log.ViseLog;
import com.vise.utils.view.BitmapUtil;

import java.io.IOException;

public class MyBitmapUtil {
    public static Bitmap scaleToDp(String file, int dp) {
        int px = DensityUtil.dip2px(MyApplication.getContext(), dp);
        px = Math.min(px, 130);
        Bitmap bitmap = BitmapFactory.decodeFile(file);
        ViseLog.e(file + ":" + bitmap);
        int degree = getBitmapDegree(file);
        if(degree != 0) {
            bitmap = rotateBitmapByDegree(bitmap, degree);
        }

        return fitToSquare(bitmap, px);
    }

    public static Bitmap showToDp(String file, int dp) {
        int px = DensityUtil.dip2px(MyApplication.getContext(), dp);
        Bitmap bitmap = BitmapFactory.decodeFile(file);
        int degree = getBitmapDegree(file);
        if(degree != 0) {
            bitmap = rotateBitmapByDegree(bitmap, degree);
        }

        return fitToSquare(bitmap, px);
    }

    private static Bitmap fitToSquare(Bitmap bm, int px) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        Bitmap bitmap;
        int diff;
        if(width > height) {
            diff = width-height;
            bitmap = Bitmap.createBitmap(bm, diff/2, 0, height, height);
        } else if(height > width) {
            diff = height-width;
            bitmap = Bitmap.createBitmap(bm, 0, diff/2, width, width);
        } else {
            bitmap = bm;
        }
        return BitmapUtil.scaleImageTo(bitmap, px, px);
    }


    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    private static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }


    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm
     *            需要旋转的图片
     * @param degree
     *            旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }
}
