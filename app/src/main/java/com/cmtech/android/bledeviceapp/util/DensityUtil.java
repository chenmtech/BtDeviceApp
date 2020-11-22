package com.cmtech.android.bledeviceapp.util;

import android.content.Context;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.util
 * ClassName:      DensityUtil
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/11/22 下午5:28
 * UpdateUser:     更新者
 * UpdateDate:     2020/11/22 下午5:28
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class DensityUtil {

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
