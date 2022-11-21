package com.cmtech.android.bledeviceapp.data.record;

import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_DOC;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;
import static com.cmtech.android.bledeviceapp.global.AppConstant.SUPPORT_RECORD_TYPES;
import static com.cmtech.android.bledeviceapp.util.DateTimeUtil.INVALID_TIME;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.*;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.cmtech.android.bledeviceapp.model.WebServiceAsyncTask;
import com.cmtech.android.bledeviceapp.data.report.EcgReport;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.cmtech.android.bledeviceapp.interfac.IWebOperation;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.ContactPerson;
import com.cmtech.android.bledeviceapp.model.WebResponse;
import com.vise.utils.file.FileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.data.record
 * ClassName:      BasicRecord
 * Description:    各种记录类的共同基类
 * Author:         chenm
 * CreateDate:     2020/4/2 下午2:44
 * UpdateUser:     chenm
 * UpdateDate:     2020/6/10 下午2:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public abstract class BasicRecord extends LitePalSupport implements IJsonable, IWebOperation {
    //-----------------------------------------常量
    // 缺省记录版本号
    public static final String DEFAULT_RECORD_VER = "1.0";

    // 缺省报告内容
    public static final String DEFAULT_REPORT_CONTENT = "";

    // 缺省报告版本号
    public static final String DEFAULT_REPORT_VER = "1.0.0";

    // 缺省报告提供者
    public static final String DEFAULT_REPORT_PROVIDER = "";

    // 信号文件的保存路径
    public static final File SIG_FILE_PATH = DIR_DOC;

    // 诊断报告状态
    public static final int REPORT_DONE = 0; // 已完成

    public static final int REPORT_WAITING = 1; // 等待处理


    //-----------------------------------------实例变量

    // ID号
    private int id;

    // 记录拥有者账户ID
    private int accountId = INVALID_ID;

    // 创建时间
    private long createTime;

    // 创建设备地址
    private final String devAddress;

    // 记录版本号
    private String ver = DEFAULT_RECORD_VER;

    // 记录创建者账户ID
    private int creatorId = INVALID_ID;

    // 备注
    private String comment = "";

    // 信号长度秒数
    private int sigSecond = 0;

    // 是否需要上传，当记录刚刚生成或者它的内容已经被修改时，就需要上传到服务器端。
    private boolean needUpload = true;


    // ----------------------------------------------与诊断报告相关字段
    // 报告版本号
    private String reportVer = DEFAULT_REPORT_VER;

    // 报告提供者
    private String reportProvider = DEFAULT_REPORT_PROVIDER;

    // 报告产生时间
    private long reportTime = INVALID_TIME;

    // 报告内容
    private String reportContent = DEFAULT_REPORT_CONTENT;

    // 报告状态
    private int reportStatus = REPORT_DONE;


    //------------------------------------------------不需要保存到数据库中的属性
    // 记录类型
    @Column(ignore = true)
    private final RecordType type;

    // 信号保存的文件实例
    @Column(ignore = true)
    protected RecordFile sigFile;


    //-----------------------------------------静态函数

    /**
     * 从本地数据库读取满足条件的记录信息
     * @param type：记录类型，如果是ALL，则包含所有记录类型
     * @param accountId：记录拥有者ID
     * @param fromTime：起始时间
     * @param filterStr：过滤的字符串，在备注中包含的字符串
     * @param num：记录条数
     * @return 查询到的记录对象列表
     */
    public static List<? extends BasicRecord> readRecordsFromLocalDb(RecordType type, int accountId, long fromTime, String filterStr, int num) {
        List<RecordType> types = new ArrayList<>();
        if(type == RecordType.ALL) {
            for(RecordType t : SUPPORT_RECORD_TYPES) {
                if(t != RecordType.ALL) {
                    types.add(t);
                }
            }
        }
        else
            types.add(type);

        List<BasicRecord> records = new ArrayList<>();
        for(RecordType t : types) {
            Class<? extends BasicRecord> recordClass = t.getRecordClass();
            if (recordClass != null) {
                if(TextUtils.isEmpty(filterStr)) {
                    records.addAll(LitePal.where("accountId = ? and createTime < ?",
                                    ""+accountId, ""+fromTime)
                            .order("createTime desc").limit(num).find(recordClass, true));
                } else {
                    records.addAll(LitePal.where("accountId = ? and createTime < ? and comment like ?",
                                    ""+accountId, ""+fromTime, "%"+filterStr+"%")
                            .order("createTime desc").limit(num).find(recordClass, true));
                }
            }
        }
        if(records.isEmpty()) return null;

        Collections.sort(records, new Comparator<BasicRecord>() {
            @Override
            public int compare(BasicRecord o1, BasicRecord o2) {
                int rlt = 0;
                if(o2.getCreateTime() > o1.getCreateTime()) rlt = 1;
                else if(o2.getCreateTime() < o1.getCreateTime()) rlt = -1;
                return rlt;
            }
        });
        return records.subList(0, Math.min(records.size(), num));
    }

    /**
     * 从服务器端获取满足条件的记录信息，保存到本地数据库中，记录信息不包含信号文件数据
     * @param context
     * @param num：获取记录数
     * @param filterStr：过滤的字符串
     * @param fromTime：起始时间
     * @param callback：返回回调
     */
    public static void downloadRecords(Context context, RecordType type, int accountId, int num, String filterStr, long fromTime, ICodeCallback callback) {
        BasicRecord record = RecordFactory.create(type, DEFAULT_RECORD_VER, accountId, INVALID_TIME, null);
        if(record == null) {
            return;
        }

        new WebServiceAsyncTask(context, "获取记录中，请稍等。", CMD_DOWNLOAD_RECORDS, new Object[]{num, filterStr, fromTime}, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                String msg = response.getMsg();
                if(code == RCODE_SUCCESS) {
                    JSONArray jsonArr = (JSONArray) response.getData();
                    if(jsonArr != null) {
                        for (int i = 0; i < jsonArr.length(); i++) {
                            try {
                                JSONObject json = (JSONObject) jsonArr.get(i);
                                if (json == null) continue;
                                RecordType type = RecordType.fromCode(json.getInt("recordTypeCode"));
                                String ver = json.getString("ver");
                                int accountId = json.getInt("accountId");
                                long createTime = json.getLong("createTime");
                                String devAddress = json.getString("devAddress");
                                BasicRecord record = RecordFactory.create(type, ver, accountId, createTime, devAddress);
                                if (record != null) {
                                    record.fromJson(json);
                                    record.setNeedUpload(false);
                                    record.saveIfNotExist("accountId = ? and createTime = ? and devAddress = ?", "" + record.getAccountId(), "" + record.getCreateTime(), record.getDevAddress());
                                }
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
                if(callback != null)
                    callback.onFinish(code, msg);
            }
        }).execute(record);
    }




    //-----------------------------------------构造器

    /**
     * 创建记录
     * @param type 记录类型
     * @param ver 记录版本
     * @param creatorId 记录创建者ID
     * @param createTime 记录创建时间
     * @param devAddress 记录创建的设备蓝牙地址
     */
    BasicRecord(RecordType type, String ver, int creatorId, long createTime, String devAddress) {
        this.type = type;
        this.ver = ver;
        this.createTime = createTime;
        this.devAddress = devAddress;
        this.accountId = creatorId;
        this.creatorId = creatorId;
    }

    //-----------------------------------------实例方法
    public int getId() {
        return id;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver= ver;
    }

    public RecordType getType() {
        return type;
    }

    public int getTypeCode() {
        return type.getCode();
    }

    public String getName() {
        return accountId + createTime + devAddress;
    }

    public int getAccountId() {
        return accountId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getDevAddress() {
        return devAddress;
    }

    public int getCreatorId() {
        return creatorId;
    }

    // 获取记录创建者的昵称或ID
    public String getCreatorNickNameOrId() {
        // 创建者就是用户本人
        if(MyApplication.getAccountId() == creatorId) {
            return  MyApplication.getAccount().getNickNameOrId();
        }
        // 创建者是别的联系人
        else {
            ContactPerson cp = MyApplication.getAccount().getContactPerson(creatorId);
            if(cp == null || TextUtils.isEmpty(cp.getNickName())) {
                return "ID:"+creatorId;
            } else {
                return cp.getNickName();
            }
        }
    }

    // 获取创建者头像文件名字符串
    public String getCreatorIcon() {
        if(MyApplication.getAccountId() == creatorId) {
            return  MyApplication.getAccount().getIcon();
        } else {
            ContactPerson cp = MyApplication.getAccount().getContactPerson(creatorId);
            if(cp == null) {
                return "";
            } else {
                return cp.getIcon();
            }
        }
    }

    /**
     * 在输出诊断报告中产生创建者昵称
     * @return 创建者昵称
     */
    public String getCreatorNickNameInOutputReport() {
        Account account = MyApplication.getAccount();
        if(account == null || account.getAccountId() != creatorId) {
            return  "匿名";
        } else {
            String nickName = account.getNickName();
            String userName = account.getUserName();
            if("".equals(nickName)) {
                int length = userName.length();
                return userName.substring(0, length/3) + "*" + userName.substring(length*2/3, length);
            }
            return nickName;
        }
    }

    public String getCreatorNoteInOutputReport() {
        Account account = MyApplication.getAccount();
        if(account == null || account.getAccountId() != creatorId) {
            return  "匿名";
        } else {
            return account.getNote();
        }
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getSigSecond() {
        return sigSecond;
    }

    public void setSigSecond(int sigSecond) {
        this.sigSecond = sigSecond;
    }

    public String getReportVer() {
        return reportVer;
    }

    public String getReportProvider() {
        return reportProvider;
    }

    public long getReportTime() {
        return reportTime;
    }

    public String getReportContent() {
        return reportContent;
    }

    public int getReportStatus() {
        return reportStatus;
    }

    public void setReport(EcgReport report) {
        reportVer = report.getVer();
        reportProvider = report.getReportProvider();
        reportTime = report.getReportTime();
        reportContent = report.getReportContent();
        reportStatus = report.getReportStatus();
    }

    public boolean needUpload() {
        return needUpload;
    }

    public void setNeedUpload(boolean needUpload) {
        this.needUpload = needUpload;
    }

    // 获取信号文件名：设备地址（去掉:）+创建时间
    public String getSigFileName() {
        return getDevAddress().replace(":", "")+getCreateTime();
    }

    /**
     * 创建信号文件
     * @param bytePerDatum 信号的每个数据有几个字节构成
     */
    public void createSigFile(int bytePerDatum) throws IOException{
        sigFile = new RecordFile(getSigFileName(), bytePerDatum, "c");
    }

    // 打开信号文件
    public void openSigFile(int bytePerDatum) {
        try {
            sigFile = new RecordFile(getSigFileName(), bytePerDatum, "o");
        } catch (IOException e) {
            e.printStackTrace();
            sigFile = null;
        }
    }

    // 关闭信号文件
    public void closeSigFile() {
        if(sigFile != null) {
            try {
                sigFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        creatorId = json.getInt("creatorId");
        comment = json.getString("comment");
        sigSecond = json.getInt("sigSecond");
        reportVer = json.getString("reportVer");
        reportProvider = json.getString("reportProvider");
        reportTime = json.getLong("reportTime");
        reportContent = json.getString("reportContent");
        reportStatus = json.getInt("reportStatus");
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = new JSONObject();
        json.put("recordTypeCode", type.getCode());
        json.put("accountId", accountId);
        json.put("createTime", createTime);
        json.put("devAddress", devAddress);
        json.put("creatorId", creatorId);
        json.put("ver", ver);
        json.put("comment", comment);
        json.put("sigSecond", sigSecond);
        json.put("reportVer", reportVer);
        json.put("reportProvider", reportProvider);
        json.put("reportTime", reportTime);
        json.put("reportContent", reportContent);
        json.put("reportStatus", reportStatus);
        return json;
    }

    // 没有信号文件
    public boolean noSignalFile() {
        return (sigFile==null);
    }

    // 是否到达信号末尾
    public boolean isEOD() {
        if(sigFile != null) {
            try {
                return sigFile.isEof();
            } catch (IOException e) {
                e.printStackTrace();
                return true;
            }
        } else {
            return true;
        }
    }

    // 在信号文件中定位
    public void seek(int pos) {
        if(sigFile!= null) {
            try {
                sigFile.seek(pos);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 获取信号数据个数
    public int getDataNum() {
        if(sigFile == null) return 0;
        return sigFile.size();
    }

    /**
     * 向一个联系人分享本记录
     * @param context 上下文
     * @param contactId 联系人ID
     * @param callback 回调
     */
    public void share(Context context, int contactId, ICodeCallback callback) {
        new WebServiceAsyncTask(context, "分享记录中，请稍等。", CMD_SHARE_RECORD, new Object[]{contactId}, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                if(callback != null)
                    callback.onFinish(response.getCode(), response.getMsg());
            }
        }).execute(this);
    }

    /**
     * 上传记录信息
     * @param context 上下文
     * @param callback 回调
     */
    @Override
    public void upload(Context context, ICodeCallback callback) {
        new WebServiceAsyncTask(context, "上传记录中，请稍等。", CMD_UPLOAD_RECORD, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                String msg = response.getMsg();
                if(code == RCODE_SUCCESS) {
                    setNeedUpload(false);
                    save();
                }
                if(callback != null)
                    callback.onFinish(code, msg);
            }
        }).execute(this);
    }

    /**
     * 下载记录信息
     * @param context
     * @param callback
     */
    @Override
    public void download(Context context, String showStr, ICodeCallback callback) {
        new WebServiceAsyncTask(context, showStr, CMD_DOWNLOAD_RECORD, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                String msg = response.getMsg();
                if (code == RCODE_SUCCESS) {
                    JSONObject data = (JSONObject) response.getData();
                    if(data != null) {
                        try {
                            fromJson(data);
                            setNeedUpload(false);
                            save();
                        } catch (JSONException e) {
                            code = RCODE_DATA_ERR;
                            msg = "数据错误";
                        }
                    }
                }
                if(callback != null)
                    callback.onFinish(code, msg);
            }
        }).execute(this);
    }

    /**
     * 删除记录
     * @param context
     * @param callback
     */
    @Override
    public void delete(Context context, ICodeCallback callback) {
        Class<? extends BasicRecord> recordClass = getClass();
        new WebServiceAsyncTask(context, "删除记录中，请稍等。", CMD_DELETE_RECORD, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                String msg = response.getMsg();
                //if(code == RETURN_CODE_SUCCESS) {
                    File sigFile = FileUtil.getFile(BasicRecord.SIG_FILE_PATH, getSigFileName());
                    if(sigFile.exists()) {
                        sigFile.delete();
                    }
                    LitePal.delete(recordClass, getId());
                    Toast.makeText(context, "记录已删除", Toast.LENGTH_SHORT).show();
                //}
                if(callback != null)
                    callback.onFinish(code, msg);
            }
        }).execute(this);
    }

    @NonNull
    @Override
    public String toString() {
        return id + "-" + type + "-" + ver + "-" + accountId + "-" + createTime + "-" + devAddress + "-" + creatorId + "-" + comment + "-" + sigSecond + "-" + needUpload + "-" + reportContent;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        BasicRecord other = (BasicRecord) otherObject;
        return getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
