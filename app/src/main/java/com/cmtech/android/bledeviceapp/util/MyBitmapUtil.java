package com.cmtech.android.bledeviceapp.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.vise.utils.view.BitmapUtil;

public class MyBitmapUtil {
    public static Bitmap scaleToDp(String file, int dp) {
        int px = DensityUtil.dip2px(MyApplication.getContext(), dp);
        Bitmap bitmap = BitmapFactory.decodeFile(file);
        if(bitmap.getWidth() > bitmap.getHeight()) {
            bitmap = BitmapUtil.scaleImageTo(bitmap, bitmap.getWidth()*px/bitmap.getHeight(), px);
        } else {
            bitmap = BitmapUtil.scaleImageTo(bitmap, px, bitmap.getHeight()*px/bitmap.getWidth());
        }
        return bitmap;
    }
}
