package com.cmtech.android.bledeviceapp.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.view
 * ClassName:      ScanSignalView
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/10/26 上午6:06
 * UpdateUser:     更新者
 * UpdateDate:     2020/10/26 上午6:06
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class ScanSignalView extends ScanWaveView {
    public ScanSignalView(Context context) {
        super(context);
    }

    public ScanSignalView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(int sampleRate, int caliValue, float zeroLocation, float secondPerGrid, float mvPerGrid, int pixelPerGrid) {
        int pixelPerData = Math.round(pixelPerGrid / (secondPerGrid * sampleRate)); // 计算横向分辨率
        float valuePerPixel = caliValue * mvPerGrid / pixelPerGrid; // 计算纵向分辨率

        setResolution(pixelPerData, valuePerPixel);
        setZeroLocation(zeroLocation);

        setPixelPerGrid(pixelPerGrid);

        resetView(true);
    }
}
