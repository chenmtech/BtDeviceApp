package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import android.util.Range;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgAppendixFactory;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgRestMarker;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.IEcgAppendix;
import com.cmtech.android.bledeviceapp.util.ByteUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgAppendixType.REST_MARKER;

/**
 * EcgFileTail: 心电文件中的尾部类
 * Created by bme on 2019/1/19.
 */

public class EcgFileTail {
    private List<IEcgAppendix> appendixList = new ArrayList<>(); // 附加留言列表
    private List<Range<Long>> markerRanges = new ArrayList<>(); // 数据位于标记当中的范围列表


    public EcgFileTail() {

    }

    public boolean readFromStream(RandomAccessFile raf) {
        try {
            raf.seek(raf.length() - 8);
            long tailEndPointer = raf.getFilePointer();
            long length = ByteUtil.reverseLong(raf.readLong());
            raf.seek(tailEndPointer - length);
            while (raf.getFilePointer() < tailEndPointer) {
                IEcgAppendix appendix = EcgAppendixFactory.readFromStream(raf);
                if(appendix != null) {
                    addAppendix(appendix);
                }
            }
            // 按留言时间排序
            Collections.sort(appendixList, new Comparator<IEcgAppendix>() {
                @Override
                public int compare(IEcgAppendix o1, IEcgAppendix o2) {
                    return (int)(o1.getCreateTime() - o2.getCreateTime());
                }
            });
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean writeToStream(RandomAccessFile raf) {
        try {
            long filePointer = raf.getFilePointer();
            long length = length();
            raf.setLength(raf.getFilePointer() + length);
            raf.seek(filePointer);

            // 写附加信息
            for(IEcgAppendix appendix : appendixList) {
                if(!EcgAppendixFactory.writeToStream(appendix, raf)) {
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
        return "[心电文件尾信息："
                + "留言数：" + appendixList.size() + ";"
                + "留言：" + Arrays.toString(appendixList.toArray()) + "]";
    }

    // 添加附加信息
    public void addAppendix(IEcgAppendix appendix) {
        appendixList.add(appendix);
        if(appendix.getType() == REST_MARKER) {
            EcgRestMarker marker = (EcgRestMarker) appendix;
            markerRanges.add(new Range<>(marker.getBeginLocation(), marker.getEndLocation()));
        }
    }

    // 删除附加信息
    public void deleteAppendix(IEcgAppendix appendix) {
        appendixList.remove(appendix);
        if(appendix.getType() == REST_MARKER) {
            EcgRestMarker marker = (EcgRestMarker) appendix;
            Range<Long> range = new Range<>(marker.getBeginLocation(), marker.getEndLocation());
            for(int i = 0; i < markerRanges.size(); i++) {
                if(markerRanges.get(i).equals(range)) {
                    markerRanges.remove(i);
                    break;
                }
            }
        }
    }

    // 获取附加信息列表
    public List<IEcgAppendix> getAppendixList() { return appendixList; }

    // 获取附加信息数
    public int getAppendixNum() {
        return appendixList.size();
    }

    // EcgFileTail字节长度：所有留言长度 + 尾部长度（long 8字节）
    public int length() {
        int length = 0;
        for(IEcgAppendix appendix : appendixList) {
            length += (appendix.length() + 4); // "加4"是指包含EcgAppendixType
        }
        return length + 8; // "加8"是指包含最后的附加信息长度long类型
    }

    // 判断指定的数据位置是否位于标记中
    public boolean isWithinMarker(long location) {
        for(Range<Long> range : markerRanges) {
            if(range.contains(location))
                return true;
        }
        return false;
    }
}
