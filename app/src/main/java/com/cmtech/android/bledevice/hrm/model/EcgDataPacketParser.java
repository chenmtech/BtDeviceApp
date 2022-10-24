package com.cmtech.android.bledevice.hrm.model;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledeviceapp.dataproc.ISignalFilter;
import com.cmtech.android.bledeviceapp.util.UnsignedUtil;
import com.vise.log.ViseLog;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;

/**
  *
  * ClassName:      EcgDataPacketParser
  * Description:    心电数据包解析器，包括从数据包解析出心电信号，并进行初步的预滤波处理
  * Author:         chenm
  * CreateDate:     2019-06-25 05:17
  * UpdateUser:     chenm
  * UpdateDate:     2020-03-06 05:17
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EcgDataPacketParser {
    //------------------------------------------------------常量
    // 最大包编号，每个数据包前面一个字节数值代表包的编号，用于防止数据包丢失
    private static final int MAX_PACKET_NUM = 255;

    // 无效数据包编号
    private static final int INVALID_PACKET_NUM = -1;


    //------------------------------------------------------实例变量
    // 包含解析器的HRM设备
    private final HrmDevice device;

    // 下一个数据包的正确编号
    private int nextPackNum = INVALID_PACKET_NUM;

    // 心电信号的预滤波器
    private final ISignalFilter preFilter;

    // 数据包的解析服务
    private ExecutorService parseService;

    //------------------------------------------------构造器
    public EcgDataPacketParser(HrmDevice device) {
        if(device == null) {
            throw new NullPointerException("The device including the ecg data package parser is null.");
        }

        this.device = device;
        preFilter = new EcgSignalPreFilter(device.getSampleRate());
    }


    //------------------------------------------------------公有方法
    // 重置
    public void reset() {
        preFilter.design(device.getSampleRate());
    }

    // 启动
    public void start() {
        if(ExecutorUtil.isDead(parseService)) {
            nextPackNum = 0;
            parseService = ExecutorUtil.newSingleExecutor("MT_Ecg_Data_Packet_Parse");
            ViseLog.e("The ecg data packet parser started.");
        } else {
            throw new IllegalStateException("The ecg data packet parser's executor is not stopped and can't be restarted.");
        }
    }

    // 停止
    public void stop() {
        ExecutorUtil.shutdownNowAndAwaitTerminate(parseService);
        ViseLog.e("The ecg data packet parser stopped.");
    }

    // 解析数据包
    public void parse(final byte[] packet) {
        if(!ExecutorUtil.isDead(parseService)) {
            parseService.execute(new Runnable() {
                @Override
                public void run() {
                    parsePacket(packet);
                }
            });
        }
    }

    //---------------------------------------------私有方法
    private void parsePacket(byte[] packet) {
        // 获取第一个字节的数据包编号
        int packageNum = UnsignedUtil.getUnsignedByte(packet[0]);

        // 包号是正确的
        if (packageNum == nextPackNum) {
            int[] pack = parseGoodPacket(packet);
            ViseLog.i("Packet No." + packageNum + ": " + Arrays.toString(pack));
            nextPackNum = (nextPackNum == MAX_PACKET_NUM) ? 0 : nextPackNum + 1;
        }
        // 坏包，表示存在数据丢失，强制断开设备连接
        else if (nextPackNum != INVALID_PACKET_NUM) {
            ViseLog.e("Some ecg data packet is lost.");
            nextPackNum = INVALID_PACKET_NUM;
            device.disconnect(false);
        }
        // 否则为无效包，不用管
    }

    private int[] parseGoodPacket(byte[] packet) {
        int[] pack = new int[(packet.length-1) / 2];
        int j = 0;
        for (int i = 1; i < packet.length; i=i+2, j++) {
            pack[j] = (short) ((0xff & packet[i]) | (0xff00 & (packet[i+1] << 8)));
            // 先用预滤波器进行滤波处理
            int ecg = (int) preFilter.filter(pack[j]);
            // 让设备处理信号，包括显示、记录和心电信号异常检测等处理
            device.processEcgSignal(ecg);
        }
        // 返回数据包只是为了调试
        return pack;
    }
}
