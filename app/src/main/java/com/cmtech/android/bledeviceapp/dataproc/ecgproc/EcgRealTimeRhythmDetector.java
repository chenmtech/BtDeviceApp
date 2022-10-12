package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class EcgRealTimeRhythmDetector {

    // 心律检测器，由一个ORT机器学习模型构建ORT会话，由该会话来完成心律异常检测
    private OrtSession rhythmDetector;

    public EcgRealTimeRhythmDetector(Context context, int modelId) {
        rhythmDetector = createOrtSession(context, modelId);
    }

    public void close() {
        if(rhythmDetector != null) {
            try {
                rhythmDetector.close();
                rhythmDetector = null;
            } catch (OrtException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建一个ORT模型的会话，该模型用来实现心律异常诊断
     * @param modelId：模型资源ID
     * @return：ORT会话
     */
    private OrtSession createOrtSession(Context context, int modelId){
        BufferedInputStream buf = null;
        try {
            InputStream inputStream = context.getResources().openRawResource(modelId);
            byte[] modelOrt = new byte[inputStream.available()];
            buf = new BufferedInputStream((inputStream));
            int readLen = buf.read(modelOrt, 0, modelOrt.length);
            if(readLen == modelOrt.length)
                return OrtEnvironment.getEnvironment().createSession(modelOrt);
        } catch (OrtException | IOException e) {
            e.printStackTrace();
        } finally {
            if(buf != null) {
                try {
                    buf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
