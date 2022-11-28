package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

import static com.cmtech.android.bledeviceapp.data.record.AnnotationConstant.ANN_AFIB_SYMBOL;
import static com.cmtech.android.bledeviceapp.data.record.AnnotationConstant.ANN_NSR_SYMBOL;
import static com.cmtech.android.bledeviceapp.data.record.AnnotationConstant.ANN_OTHER_ARRHYTHMIA_SYMBOL;
import static com.cmtech.android.bledeviceapp.data.record.AnnotationConstant.ANN_SB_SYMBOL;
import static com.cmtech.android.bledeviceapp.data.record.AnnotationConstant.INVALID_ANN_SYMBOL;

import android.util.Pair;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledeviceapp.data.record.SignalAnnotation;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.util.MathUtil;
import com.vise.log.ViseLog;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

/**
 * 心电信号心律实时检测器1.1版本
 * 该版本检测器要求输入心电信号的采样率为250Hz
 * 因为使用的ML模型训练用到的信号数据集采样率为250Hz
 */
public class EcgRealTimeRhythmDetector11 implements IEcgRealTimeRhythmDetector{
    //--------------------------------------------------------------常量
    // 版本号
    private static final String VER = "1.1";

    // 提供者
    private static final String PROVIDER = "康明智联";

    // 信号采样率
    private static final int SIG_SR = 250;

    // 用于心律检测的信号时长，单位秒
    private static final int SIG_LEN = 10;

    // 存放信号的缓存长度
    private static final int BUF_LEN = SIG_LEN * SIG_SR;

    // 将模型输出label与ECG注解的映射关系
    private static final Map<Integer, String> LABEL_ANN_MAP = new HashMap<>() {{
        put(0, ANN_AFIB_SYMBOL);
        put(1, ANN_NSR_SYMBOL);
        put(2, ANN_SB_SYMBOL);
        put(3, ANN_OTHER_ARRHYTHMIA_SYMBOL);
    }};

    //------------------------------------------------------------实例变量
    // 信号数据缓存
    private final float[] sigBuf = new float[BUF_LEN];

    // 信号数据缓存的当前写入位置
    private int pos = 0;

    // 心律检测器，由一个ORT机器学习模型构建的ORT会话，由该会话来完成心律检测
    private final OrtSession rhythmDetectModel;

    // 数据处理多线程服务
    private ExecutorService procService;

    // 检测结果回调
    private final IEcgRhythmDetectCallback callback;

    //--------------------------------------------------------------构造器
    /**
     * 构造一个心电信号的心律检测器，并启动检测
     * @param modelId 使用的机器学习模型资源文件ID
     * @param callback 检测结果回调
     */
    public EcgRealTimeRhythmDetector11(int modelId, IEcgRhythmDetectCallback callback) throws OrtException {
        this.callback = callback;
        rhythmDetectModel = createOrtSession(modelId);
        if(rhythmDetectModel != null)
            startDetect();
    }

    @Override
    public String getVer() {
        return VER;
    }

    @Override
    public String getProvider() {
        return PROVIDER;
    }

    //----------------------------------------------公有方法
    /**
     * 重置检测器
     */
    @Override
    public void reset() {
        stopDetect();
        Arrays.fill(sigBuf, (float) 0.0);
        pos = 0;
        if(rhythmDetectModel != null)
            startDetect();
    }

    /**
     * 处理一个心电信号
     * @param ecgSignalmV 心电信号毫伏值
     */
    @Override
    public void process(float ecgSignalmV, int position) {
        if(!ExecutorUtil.isDead(procService)) {
            procService.execute(new Runnable() {
                @Override
                public void run() {
                    postprocess(ecgSignalmV, position);
                }
            });
        }
    }

    /**
     * 处理一个心电信号值，单线程版本
     * @param ecgSignalmV 心电信号毫伏值
     * @param position 该信号值在记录中的数据位置
     */
    public void postprocess(float ecgSignalmV, int position) {
        // 将信号放入缓存
        sigBuf[pos++] = ecgSignalmV;

        // 判断信号缓存是否已满，如果是，进行检测
        if(pos == BUF_LEN) {
            try {
                // 用模型进行检测，输出结果
                int label = detectRhythm(sigBuf);
                // 通过标签映射，获取应用定义的异常标签值
                String annSymbol = LABEL_ANN_MAP.get(label);
                if(!INVALID_ANN_SYMBOL.equals(annSymbol)) {
                    SignalAnnotation item = new SignalAnnotation(position-BUF_LEN+1, annSymbol);
                    // 用回调处理检测条目
                    if (callback != null)
                        callback.onRhythmInfoUpdated(item);
                }
                // 更新信号缓存和位置
                pos = 0;
            } catch (OrtException e) {
                e.printStackTrace();
                pos = 0;
            }
        }
    }

    /**
     * 销毁检测器。一旦销毁，不能调用任何方法。
     */
    @Override
    public void destroy() {
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
     * 创建一个ORT模型的会话，该模型用来实现心律检测
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
     * 用模型检测一段心电信号的心律
     * @param sig 输入的一段心电信号数据
     * @return 心律检测类型标签值
     * @throws OrtException 抛出ORT异常
     */
    private int detectRhythm(float[] sig) throws OrtException {
        if(rhythmDetectModel == null)
            throw new OrtException("无效模型");

        // 将数据打包
        FloatBuffer buf = FloatBuffer.wrap(sig);
        // 输入张量形状
        long[] shape = new long[]{1, sig.length, 1};
        // 创建张量
        OnnxTensor sigTensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), buf, shape);
        // 创建模型输入
        Map<String, OnnxTensor> inputs = new HashMap<String, OnnxTensor>();
        inputs.put("input_ecg", sigTensor);
        // 运行模型，预测检测结果
        OrtSession.Result rlt = rhythmDetectModel.run(inputs);
        OnnxTensor v = (OnnxTensor) rlt.get(0);
        // 获取模型输出的概率值
        float[] out_prob = ((float[][]) v.getValue())[0];
        ViseLog.e(Arrays.toString(out_prob));
        // 概率最大的位置就是模型输出的标签值
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
            throw new IllegalStateException("The ecg real time rhythm detection's executor can't be restarted.");
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
