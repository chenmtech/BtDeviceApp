package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

import static com.cmtech.android.bledeviceapp.util.DateTimeUtil.INVALID_TIME;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

/**
 * 心电信号中心律异常的实时检测器
 * 该检测器默认输入心电信号的采样率为250Hz
 * 心电信号会先重采样为300Hz，因为默认机器学习模型训练集的信号都是300Hz的
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
    public interface IEcgRhythmDetectCallback {
        // 心律异常信息更新
        void onRhythmInfoUpdated(EcgRhythmDetectItem item);
    }

    //------------------------------------------------------------实例变量

    // 信号数据缓存
    private final float[] sigBuf = new float[SIGNAL_BUFFER_LENGTH];

    // 信号数据缓存的当前位置
    private int pos = 0;

    // ECG信号重采样器，采样频率从250Hz变为300Hz
    private final ResampleFrom250To300 resample;

    // 心律异常检测器，由一个ORT机器学习模型构建的ORT会话，由该会话来完成心律异常检测
    private final OrtSession rhythmDetectModel;

    // 检测模型输出标签与全局标签的映射关系
    private final Map<Integer, Integer> labelMap;

    // 数据处理多线程服务
    private ExecutorService procService;

    // 检测结果回调
    private final IEcgRhythmDetectCallback callback;

    //--------------------------------------------------------------构造器

    /**
     * 构造一个心电信号的心律异常检测器，并启动检测
     * @param modelId 采用的机器学习模型资源文件ID
     * @param modelLabelMap 模型输出的心律异常结果标签与全局标签之间的映射关系
     * @param callback 检测结果回调
     */
    public EcgRealTimeRhythmDetector(int modelId, Map<Integer, Integer> modelLabelMap, IEcgRhythmDetectCallback callback) throws OrtException {
        this.callback = callback;
        this.labelMap = modelLabelMap;
        resample = new ResampleFrom250To300();
        rhythmDetectModel = createOrtSession(modelId);
        startDetect();
    }

    //----------------------------------------------公有方法
    /**
     * 重置检测器
     */
    public void reset() {
        stopDetect();
        resample.reset();
        Arrays.fill(sigBuf, (short) 0);
        pos = 0;
        startDetect();
    }

    /**
     * 处理一个心电信号
     * @param ecgSignal 心电信号值
     */
    public void process(short ecgSignal) {
        if(!ExecutorUtil.isDead(procService)) {
            procService.execute(new Runnable() {
                @Override
                public void run() {
                    postprocess(ecgSignal, INVALID_TIME);
                }
            });
        }
    }

    public void postprocess(short ecgSignal, long curTime) {
        // 先做重采样，获取输出信号，并将信号放入缓存
        List<Short> resampleSignal = resample.process(ecgSignal);
        for(short sig : resampleSignal) {
            sigBuf[pos++] = sig;
        }

        // 判断信号缓存是否已满，如果是，进行检测
        if(pos == SIGNAL_BUFFER_LENGTH) {
            try {
                // 用模型进行检测，输出结果
                int label = detectRhythm(sigBuf);
                // 通过标签映射，获取应用定义的异常标签值
                label = labelMap.get(label);
                // 生成检测条目
                long startTime;
                if(curTime == INVALID_TIME)
                    startTime = new Date().getTime() - ECG_SIGNAL_TIME_LENGTH*1000;
                else
                    startTime = curTime - ECG_SIGNAL_TIME_LENGTH*1000;
                EcgRhythmDetectItem item = new EcgRhythmDetectItem(startTime, label);
                // 用回调处理检测条目
                if(callback != null)
                    callback.onRhythmInfoUpdated(item);
                // 更新信号缓存和位置
                System.arraycopy(sigBuf, DETECT_INTERVAL_BUFFER_START, sigBuf, 0,
                        SIGNAL_BUFFER_LENGTH-DETECT_INTERVAL_BUFFER_START);
                pos = DETECT_INTERVAL_BUFFER_START;
            } catch (OrtException e) {
                e.printStackTrace();
                pos = 0;
            }
        }
    }

    /**
     * 关闭检测器。一旦关闭，不能调用任何方法。
     */
    public void close() {
        stopDetect();

        try {
            if(rhythmDetectModel != null)
                rhythmDetectModel.close();
        } catch (OrtException e) {
            e.printStackTrace();
        }
    }


    //-----------------------------------------------私有方法
    /**
     * 创建一个ORT模型的会话，该模型用来实现心律异常检测
     * @param modelId 模型资源ID
     * @return ORT会话
     */
    private OrtSession createOrtSession(int modelId) throws OrtException {
        BufferedInputStream buf = null;
        try {
            InputStream inputStream = MyApplication.getContext().getResources().openRawResource(modelId);
            byte[] modelOrt = new byte[inputStream.available()];
            buf = new BufferedInputStream((inputStream));
            int readLen = buf.read(modelOrt, 0, modelOrt.length);
            if(readLen == modelOrt.length)
                return OrtEnvironment.getEnvironment().createSession(modelOrt);
        } catch (IOException e) {
            e.printStackTrace();
            ViseLog.e(e);
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

    /**
     * 用模型检测一段心电信号是否包含异常
     * @param data 输入的一段心电信号数据
     * @return 模型输出的检测结果标签值
     * @throws OrtException 抛出ORT异常
     */
    private int detectRhythm(float[] data) throws OrtException {
        if(rhythmDetectModel == null)
            throw new OrtException("无效模型");

        // 将数据打包
        FloatBuffer buf = FloatBuffer.wrap(data);
        // 输入张量形状
        long[] shape = new long[]{1, data.length, 1};
        // 创建张量
        OnnxTensor t1 = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), buf, shape);
        // 创建模型输入
        Map<String, OnnxTensor> inputs = new HashMap<String, OnnxTensor>();
        inputs.put("input_ecg", t1);
        // 运行模型，获取检测结果
        OrtSession.Result rlt = rhythmDetectModel.run(inputs);
        OnnxTensor v = (OnnxTensor) rlt.get(0);
        // 获取输出检测概率值
        float[] out_prob = ((float[][]) v.getValue())[0];
        ViseLog.e(Arrays.toString(out_prob));
        // 概率最大的位置对应于模型输出的标签值
        Pair<Integer, Float> result = MathUtil.floatMax(out_prob);
        return result.first;
    }

    /**
     * 启动检测
     */
    private void startDetect() {
        if(ExecutorUtil.isDead(procService)) {
            procService = ExecutorUtil.newSingleExecutor("MT_Ecg_Rhythm_Detect");
            ViseLog.e("The ecg real time rhythm detection started.");
        } else {
            throw new IllegalStateException("The ecg real time rhythm detection's executor is not stopped and can't be restarted.");
        }
    }

    /**
     * 停止检测
     */
    private void stopDetect() {
        ExecutorUtil.shutdownNowAndAwaitTerminate(procService);
        ViseLog.e("The ecg real time rhythm detection stopped.");
    }
}
