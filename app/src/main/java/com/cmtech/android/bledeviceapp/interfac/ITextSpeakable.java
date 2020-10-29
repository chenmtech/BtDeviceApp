package com.cmtech.android.bledeviceapp.interfac;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.util
 * ClassName:      TTS
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/3/24 上午7:34
 * UpdateUser:     更新者
 * UpdateDate:     2020/3/24 上午7:34
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public interface ITextSpeakable {
    void speak(String playText);
    void stopSpeak();
}
