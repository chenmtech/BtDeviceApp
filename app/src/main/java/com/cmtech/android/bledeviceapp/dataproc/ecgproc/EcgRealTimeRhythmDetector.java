package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.cmtech.android.bledeviceapp.dataproc.ecgproc.preproc.ResampleFrom250To300;
import com.cmtech.android.bledeviceapp.util.MathUtil;
import com.vise.log.ViseLog;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class EcgRealTimeRhythmDetector {
    private static final int SAMPLE_RATE = 300;

    private static final int ECG_SIGNAL_TIME_LENGTH = 20;

    private static final int DETECT_INTERVAL_TIME_LENGTH = 10;

    private static final int SIGNAL_BUFFER_LENGTH = ECG_SIGNAL_TIME_LENGTH*SAMPLE_RATE;

    private static final Map<Integer, String> DETECT_RESULT = new HashMap<>(){{
        put(0, "窦性心律");
        put(1, "房颤");
        put(2, "非房颤异常");
        put(3, "噪声");
    }};

    // ECG重采样
    private final ResampleFrom250To300 resample;

    // 心律检测器，由一个ORT机器学习模型构建ORT会话，由该会话来完成心律异常检测
    private OrtSession rhythmDetector;

    //
    private final float[] buf = new float[SIGNAL_BUFFER_LENGTH];

    private int pos = 0;

    public EcgRealTimeRhythmDetector(Context context, int modelId) {
        resample = new ResampleFrom250To300();

        rhythmDetector = createOrtSession(context, modelId);
    }

    public void reset() {
        resample.reset();
        Arrays.fill(buf, (short) 0);
        pos = 0;
    }

    public String process(short ecgSignal) {
        List<Short> resampleSignal = resample.process(ecgSignal);
        for(short n : resampleSignal) {
            buf[pos++] = n;
        }

        / 这里需要多线程处理，否则有问题
        if(pos == SIGNAL_BUFFER_LENGTH) {
            pos = 0;
            try {
                float[] y_prob = detectRhythm(buf);
                ViseLog.e(Arrays.toString(y_prob));
                Pair<Integer, Float> result = MathUtil.floatMax(y_prob);
                return DETECT_RESULT.get(result.first);
            } catch (OrtException e) {
                e.printStackTrace();
            }
        }
        return null;
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
     * @param context 上下文，用于获取模型资源文件
     * @param modelId 模型资源ID
     * @return ORT会话
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

    private float[] detectRhythm(float[] data) throws OrtException {
        FloatBuffer buf = FloatBuffer.wrap(data);
        long[] shape = new long[]{1, data.length, 1};
        OnnxTensor t1 = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), buf, shape);
        Log.i("Dev",t1.toString());
        Map<String, OnnxTensor> inputs = new HashMap<String, OnnxTensor>();
        inputs.put("input_ecg", t1);
        OrtSession.Result rlt = rhythmDetector.run(inputs);
        OnnxTensor v = (OnnxTensor) rlt.get(0);
        return ((float[][]) v.getValue())[0];
    }

}
