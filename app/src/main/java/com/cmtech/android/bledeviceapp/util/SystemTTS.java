package com.cmtech.android.bledeviceapp.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.util
 * ClassName:      SystemTTS
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/3/24 上午7:32
 * UpdateUser:     更新者
 * UpdateDate:     2020/3/24 上午7:32
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class SystemTTS extends UtteranceProgressListener implements TTS, TextToSpeech.OnUtteranceCompletedListener {
    private Context mContext;
    private static SystemTTS singleton;
    private TextToSpeech textToSpeech; // 系统语音播报类
    private boolean isSuccess = true;

    public static SystemTTS getInstance(Context context) {
        if (singleton == null) {
            synchronized (SystemTTS.class) {
                if (singleton == null) {
                    singleton = new SystemTTS(context);
                }
            }
        }
        return singleton;
    }

    private SystemTTS(Context context) {
        this.mContext = context.getApplicationContext();
        textToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                //系统语音初始化成功
                if (i == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.CHINA);
                    textToSpeech.setPitch(1.0f);// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                    textToSpeech.setSpeechRate(1.0f);
                    textToSpeech.setOnUtteranceProgressListener(SystemTTS.this);
                    textToSpeech.setOnUtteranceCompletedListener(SystemTTS.this);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        //系统不支持中文播报
                        isSuccess = false;
                    }
                }

            }
        });
    }

    public void speak(int strId) {
        speak(mContext.getResources().getString(strId));
    }

    public void speak(String playText) {
        if (!isSuccess) {
            return;
        }
        if (textToSpeech != null) {
            textToSpeech.speak(playText, TextToSpeech.QUEUE_ADD, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        }
    }

    public void stopSpeak() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    public boolean isSpeaking() {
        if(textToSpeech == null) return false;
        return textToSpeech.isSpeaking();
    }


    //播报完成回调
    @Override
    public void onUtteranceCompleted(String utteranceId) {

    }

    @Override
    public void onStart(String utteranceId) {

    }

    @Override
    public void onDone(String utteranceId) {
    }

    @Override
    public void onError(String utteranceId) {

    }
}
