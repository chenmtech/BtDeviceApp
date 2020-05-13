package com.cmtech.android.bledevice.ecg.record.ecgcomment;

import com.cmtech.android.bledevice.ecg.enumeration.EcgCommentType;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.util.DataIOUtil;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * EcgNormalComment: 心电一般留言类
 * Created by bme on 2019/1/9.
 */

public class EcgNormalComment extends EcgComment {
    private static final int MODIFY_TIME_BYTE_NUM = 8;
    private static final int CONTENT_CHAR_NUM = 500; // 内容字符数

    private int id;
    private User creator; // 创建人
    private long modifyTime = -1; // 修改时间
    private String content = ""; // 内容

    private EcgNormalComment() {
    }

    private EcgNormalComment(User creator, long modifyTime) {
        this.creator = new User(creator);
        this.modifyTime = modifyTime;
    }

    /**
     * 用当前账户和当前时间创建默认留言
     * @return 默认留言对象
     */
    public static EcgNormalComment create() {
        User creator = AccountManager.getAccount();
        long modifyTime = new Date().getTime();
        return new EcgNormalComment(creator, modifyTime);
    }

    public User getCreator() {
        if(creator == null) {
            List<User> creators = LitePal.where("ecgnormalcomment_id = ?", String.valueOf(id)).find(User.class);
            if (!creators.isEmpty())
                creator = creators.get(0);
            else
                creator = null; //new User();
        }
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
    public void appendContent(String content) {
        this.content += content;
    }

    @Override
    public EcgCommentType getType() {
        return EcgCommentType.NORMAL_COMMENT;
    }

    /**
     * 从数据输入流读取
     * @param in：数据输入流
     */
    @Override
    public void readFromStream(DataInput in) throws IOException{
        //getCreator().readFromStream(in); // 读创建人
        modifyTime = in.readLong(); // 读修改时间
        content = DataIOUtil.readFixedString(in, CONTENT_CHAR_NUM); // 读留言内容
    }

    /**
     * 写出到数据输出流
     * @param out：数据输出流
     */
    @Override
    public void writeToStream(DataOutput out) throws IOException{
        //getCreator().writeToStream(out); // 写创建人
        out.writeLong(modifyTime); // 写修改时间
        DataIOUtil.writeFixedString(out, content, CONTENT_CHAR_NUM); // 写留言内容
    }

    /**
     * 获取留言的字节长度
     * @return 字节长
     */
    @Override
    public int length() {
        return  super.length() + /*getCreator().length() +*/ MODIFY_TIME_BYTE_NUM + 2* CONTENT_CHAR_NUM;
    }

    @Override
    public String toString() {
        return getCreator().getName() + "@" + DateTimeUtil.timeToShortStringWithTodayYesterday(modifyTime) + ' ' + content;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        EcgNormalComment other = (EcgNormalComment)otherObject;
        // 只要创建人和修改时间相同，就认为是同一条留言
        return  (getCreator().equals(other.getCreator()) && (modifyTime == other.modifyTime));
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + getCreator().hashCode();
        result = 37*result + (int)(modifyTime^(modifyTime>>32));
        return result;
    }

    @Override
    public boolean save() {
        ViseLog.e("comment save");
        if(creator != null) {
            creator.save();
        }
        return super.save();
    }

    @Override
    public int delete() {
        ViseLog.e("comment delete");
        getCreator().delete();
        return super.delete();
    }
}
