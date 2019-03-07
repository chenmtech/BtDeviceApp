package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

import android.text.TextUtils;

import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.DataIOUtil;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

/**
 * EcgAppendix: 心电留言类
 * Created by bme on 2019/1/9.
 */

public class EcgAppendix{
    private static final int CONTENT_CHAR_NUM = 500; // 内容字符数

    private User creator = new User(); // 创建人
    private long modifyTime = -1; // 修改时间
    private String content = ""; // 内容

    public EcgAppendix() {

    }

    private EcgAppendix(User creator, long modifyTime) {
        this();
        try {
            this.creator = (User) creator.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        this.modifyTime = modifyTime;
    }

    public EcgAppendix(User creator, long modifyTime, String content) {
        this(creator, modifyTime);
        this.content = content;
    }

    /**
     * 用当前账户和当前时间创建默认留言
     * @return 默认留言对象
     */
    public static EcgAppendix createDefaultAppendix() {
        User creator = AccountManager.getInstance().getAccount();
        long modifyTime = new Date().getTime();
        return new EcgAppendix(creator, modifyTime);
    }

    public User getCreator() {
        return creator;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) { this.modifyTime = modifyTime;}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 添加留言内容
      */
    public void appendContent(String content) {
        if(TextUtils.isEmpty(this.content)) {
            this.content = content;
            return;
        }
        this.content += ('\n' + content);
    }

    /**
     * 从数据输入流读取
     * @param in：数据输入流
     * @return 是否成功读取
     */
    public boolean readFromStream(DataInput in) {
        try {
            // 读创建人
            creator.readFromStream(in);
            // 读修改时间
            modifyTime = ByteUtil.reverseLong(in.readLong());
            // 读留言内容
            content = DataIOUtil.readFixedString(CONTENT_CHAR_NUM, in);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * 写出到数据输出流
     * @param out：数据输出流
     * @return 是否成功写出
     */
    public boolean writeToStream(DataOutput out) {
        try {
            // 写创建人
            creator.writeToStream(out);
            // 写修改时间
            out.writeLong(ByteUtil.reverseLong(modifyTime));
            // 写留言内容
            DataIOUtil.writeFixedString(content, CONTENT_CHAR_NUM, out);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * 获取留言的字节长度
     * @return 字节长
     */
    public int length() {
        return  creator.length() + 8 + 2* CONTENT_CHAR_NUM;
    }

    @Override
    public String toString() {
        return creator.getUserName() + "@" + DateTimeUtil.timeToShortStringWithTodayYesterday(modifyTime) + ' ' + content;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;

        EcgAppendix other = (EcgAppendix)otherObject;

        // 只要手机号和修改时间相同，就认为是同一条留言
        return  (creator.getPhone().equals(other.creator.getPhone()) && (modifyTime == other.modifyTime));
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + creator.hashCode();
        result = 37*result + (int)(modifyTime^(modifyTime>>32));
        return result;
    }
}
