package com.cmtech.android.bledeviceapp.global;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.cmtech.android.bledeviceapp.interfac.ITextSpeakable;
import com.vise.log.ViseLog;

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
public class SystemTTS extends UtteranceProgressListener implements ITextSpeakable {
    private final Context mContext;
    private TextToSpeech textToSpeech;

    SystemTTS(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void speak(int strId) {
        speak(mContext.getResources().getString(strId));
    }

    public void speak(String playText) {
        textToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        ViseLog.e("don't support the language in the TTS.");
                    } else {
                        textToSpeech.setPitch(1.0f);
                        textToSpeech.setSpeechRate(1.0f);
                        textToSpeech.setOnUtteranceProgressListener(SystemTTS.this);
                        textToSpeech.speak(playText, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                    }
                }
            }
        });
    }

    public void stopSpeak() {
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public void onStart(String utteranceId) {

    }

    @Override
    public void onDone(String utteranceId) {
        if(textToSpeech != null) {
            textToSpeech.shutdown();
        }
    }

    @Override
    public void onError(String utteranceId) {
        if(textToSpeech != null) {
            textToSpeech.shutdown();
        }
    }
}
