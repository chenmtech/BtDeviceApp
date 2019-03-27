package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgAppendix;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.vise.log.ViseLog;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * EcgFileTail: 心电文件中的尾部类
 * Created by bme on 2019/1/19.
 */

public class EcgFileTail {
    private List<Integer> hrArray = new ArrayList<>();

    private List<EcgAppendix> appendixList = new ArrayList<>(); // 附加信息列表

    public EcgFileTail() {

    }

    /**
     * 从数据输入流读取
     * @param raf：数据输入流
     * @return 是否成功读取
     */
    public boolean readFromStream(RandomAccessFile raf) {
        try {
            raf.seek(raf.length() - 8);
            long tailEndPointer = raf.getFilePointer();
            long tailLength = ByteUtil.reverseLong(raf.readLong());
            long appendixLength = tailLength - 8;
            raf.seek(tailEndPointer - appendixLength);

            // 读心率数据
            int hrLength = ByteUtil.reverseInt(raf.readInt());
            for(int i = 0; i < hrLength; i++) {
                hrArray.add(ByteUtil.reverseInt(raf.readInt()));
            }
            // 读留言信息
            while (raf.getFilePointer() < tailEndPointer) {
                EcgAppendix appendix = new EcgAppendix();
                if(appendix.readFromStream(raf)) {
                    addAppendix(appendix);
                }
            }
            /*// 按留言时间排序
            Collections.sort(appendixList, new Comparator<IEcgAppendix>() {
                @Override
                public int compare(IEcgAppendix o1, IEcgAppendix o2) {
                    return (int)(o1.getCreateTime() - o2.getCreateTime());
                }
            });*/
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * 写出到数据输出流当前指针指向的位置
     * @param raf：数据输出流
     * @return 是否成功写出
     */
    public boolean writeToStream(RandomAccessFile raf) {
        try {
            long filePointer = raf.getFilePointer();
            long length = length();
            raf.setLength(raf.getFilePointer() + length);
            raf.seek(filePointer);

            // 写心率信息
            raf.writeInt(ByteUtil.reverseInt(hrArray.size()));
            for(int hr : hrArray) {
                raf.writeInt(ByteUtil.reverseInt(hr));
            }

            // 写附加信息
            for(EcgAppendix appendix : appendixList) {
                if(!appendix.writeToStream(raf)) {
                    return false;
                }
            }
            // 最后写入附加信息总长度
            raf.writeLong(ByteUtil.reverseLong(length));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[心电文件尾："
                + "留言数：" + appendixList.size() + ";"
                + "留言：" + Arrays.toString(appendixList.toArray()) + "]";
    }

    // 添加附加信息
    public void addAppendix(EcgAppendix appendix) {
        appendixList.add(appendix);
    }

    // 删除附加信息
    public void deleteAppendix(EcgAppendix appendix) {
        appendixList.remove(appendix);
    }

    // 获取附加信息列表
    public List<EcgAppendix> getAppendixList() { return appendixList; }

    // 获取附加信息数
    public int getAppendixNum() {
        return appendixList.size();
    }

    /**
     * 获取EcgFileTail字节长度：所有留言长度 + 尾部长度（long 8字节）
      */
    public int length() {
        int length = 4 + 4*hrArray.size(); // 心率数据长度

        for(EcgAppendix appendix : appendixList) {
            length += appendix.length();
        }

        return length + 8; // "加8"是指包含最后的附加信息长度long类型
    }
}
