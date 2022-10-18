package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

import static com.cmtech.android.bledeviceapp.dataproc.ecgproc.EcgRhythmDetectItem.OTHER_LABEL;

import android.util.Log;
import android.util.Pair;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.preproc.ResampleFrom250To300;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.util.MathUtil;
import com.vise.log.ViseLog;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

/**
 * 心电信号中是否存在心律异常的实时检测器
 */
public class EcgRealTimeRhythmDetector {
    //--------------------------------------------------------------常量
    // 信号采样率
    private static final int SAMPLE_RATE = 300;

    // 用于一次检测的信号时长
    private static final int ECG_SIGNAL_TIME_LENGTH = 20;

    // 检测间隔时长
    private static final int DETECT_INTERVAL_TIME_LENGTH = 10;

    // 存放信号的缓存长度
    private static final int SIGNAL_BUFFER_LENGTH = ECG_SIGNAL_TIME_LENGTH*SAMPLE_RATE;

    // 检测间隔对应的缓存长度
    private static final int DETECT_INTERVAL_BUFFER_START = DETECT_INTERVAL_TIME_LENGTH*SAMPLE_RATE;

    //------------------------------------------------------------内部接口
    // 心律异常检测结果回调接口
    public interface ICallback {
        // 心律异常信息更新
        void onRhythmDetectItemUpdated(EcgRhythmDetectItem item);
    }

    //------------------------------------------------------------实例变量
    // ECG信号重采样器，采样频率从250Hz变为300Hz
    private final ResampleFrom250To300 resample;

    // 心律异常检测器，由一个ORT机器学习模型构建的ORT会话，由该会话来完成心律异常检测
    private OrtSession rhythmDetector;

    //
    private final float[] buf = new float[SIGNAL_BUFFER_LENGTH];

    private int pos = 0;

    // 数据处理服务
    private ExecutorService procService;

    private final Map<Integer, Integer> labelMap;

    private final ICallback callback;

    public EcgRealTimeRhythmDetector(int modelId, Map<Integer, Integer> modelLabelMap, ICallback callback) {
        this.callback = callback;
        this.labelMap = modelLabelMap;
        resample = new ResampleFrom250To300();
        rhythmDetector = createOrtSession(modelId);
        start();
    }

    public void reset() {
        resample.reset();
        Arrays.fill(buf, (short) 0);
        pos = 0;
    }

    public void process(short ecgSignal) {
        if(!ExecutorUtil.isDead(procService)) {
            procService.execute(new Runnable() {
                @Override
                public void run() {
                    List<Short> resampleSignal = resample.process(ecgSignal);
                    for(short n : resampleSignal) {
                        if(pos==0) {
                            ViseLog.e("pos0:"+new Date().getTime());
                        }
                        buf[pos++] = n;
                    }

                    if(pos == SIGNAL_BUFFER_LENGTH) {
                        try {
                            int label = detectRhythm(buf);
                            try {
                                label = labelMap.get(label);
                            } catch (NullPointerException ex) {
                                label = OTHER_LABEL;
                            }
                            ViseLog.e("pos"+pos+":"+new Date().getTime());
                            long startTime = new Date().getTime() - ECG_SIGNAL_TIME_LENGTH*1000;
                            EcgRhythmDetectItem item =
                                    new EcgRhythmDetectItem(startTime, label);
                            if(callback != null)
                                callback.onRhythmDetectItemUpdated(item);

                            System.arraycopy(buf, DETECT_INTERVAL_BUFFER_START, buf, 0,
                                    SIGNAL_BUFFER_LENGTH-DETECT_INTERVAL_BUFFER_START);
                            pos = DETECT_INTERVAL_BUFFER_START;
                        } catch (OrtException e) {
                            e.printStackTrace();
                            pos = 0;
                        }
                    }
                }
            });
        }
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

        stop();
    }

    /**
     * 创建一个ORT模型的会话，该模型用来实现心律异常诊断
     * @param modelId 模型资源ID
     * @return ORT会话
     */
    private OrtSession createOrtSession(int modelId){
        BufferedInputStream buf = null;
        try {
            InputStream inputStream = MyApplication.getContext().getResources().openRawResource(modelId);
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

    private int detectRhythm(float[] data) throws OrtException {
        FloatBuffer buf = FloatBuffer.wrap(data);
        long[] shape = new long[]{1, data.length, 1};
        OnnxTensor t1 = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), buf, shape);
        Log.i("Dev",t1.toString());
        Map<String, OnnxTensor> inputs = new HashMap<String, OnnxTensor>();
        inputs.put("input_ecg", t1);
        OrtSession.Result rlt = rhythmDetector.run(inputs);
        OnnxTensor v = (OnnxTensor) rlt.get(0);
        float[] out_prob = ((float[][]) v.getValue())[0];
        ViseLog.e(Arrays.toString(out_prob));
        Pair<Integer, Float> result = MathUtil.floatMax(out_prob);
        return result.first;
    }

    // 启动
    private void start() {
        if(ExecutorUtil.isDead(procService)) {
            procService = ExecutorUtil.newSingleExecutor("MT_Ecg_Rhythm_Detect");
            ViseLog.e("The ecg real time rhythm detection started.");
        } else {
            throw new IllegalStateException("The ecg real time rhythm detection's executor is not stopped and can't be restarted.");
        }
    }

    // 停止
    private void stop() {
        ExecutorUtil.shutdownNowAndAwaitTerminate(procService);
        ViseLog.e("The ecg real time rhythm detection stopped.");
    }
}
