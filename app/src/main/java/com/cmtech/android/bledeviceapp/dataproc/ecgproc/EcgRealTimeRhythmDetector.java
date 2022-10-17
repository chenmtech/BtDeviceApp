package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

import static com.cmtech.android.bledeviceapp.dataproc.ecgproc.EcgRhythmDetectItem.AF_LABEL;
import static com.cmtech.android.bledeviceapp.dataproc.ecgproc.EcgRhythmDetectItem.NOISE_LABEL;
import static com.cmtech.android.bledeviceapp.dataproc.ecgproc.EcgRhythmDetectItem.NSR_LABEL;
import static com.cmtech.android.bledeviceapp.dataproc.ecgproc.EcgRhythmDetectItem.OTHER_LABEL;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledevice.hrm.model.HrmDevice;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.preproc.ResampleFrom250To300;
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

public class EcgRealTimeRhythmDetector {
    private static final int SAMPLE_RATE = 300;

    private static final int ECG_SIGNAL_TIME_LENGTH = 20;

    private static final int DETECT_INTERVAL_TIME_LENGTH = 10;

    private static final int SIGNAL_BUFFER_LENGTH = ECG_SIGNAL_TIME_LENGTH*SAMPLE_RATE;

    private static final int DETECT_INTERVAL_BUFFER_START = DETECT_INTERVAL_TIME_LENGTH*SAMPLE_RATE;

    private final HrmDevice device;

    // ECG重采样
    private final ResampleFrom250To300 resample;

    // 心律检测器，由一个ORT机器学习模型构建ORT会话，由该会话来完成心律异常检测
    private OrtSession rhythmDetector;

    //
    private final float[] buf = new float[SIGNAL_BUFFER_LENGTH];

    private int pos = 0;

    private final List<Short> dataBuf = Collections.synchronizedList(new LinkedList<>());

    // 数据处理服务
    private ExecutorService procService;

    private final Map<Integer, Integer> labelMap = new HashMap<>() {{
        put(0, NSR_LABEL);
        put(1, AF_LABEL);
        put(2, OTHER_LABEL);
        put(3, NOISE_LABEL);
    }};

    public EcgRealTimeRhythmDetector(HrmDevice device, int modelId) {
        this.device = device;
        resample = new ResampleFrom250To300();
        rhythmDetector = createOrtSession(device.getContext(), modelId);
        start();
    }

    public void reset() {
        resample.reset();
        dataBuf.clear();
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
                            device.updateRhythmInfo(item);
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
