package com.cmtech.android.bledeviceapp.model;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      WebResponse
 * Description:    网络操作的响应类
 * Author:         chenm
 * CreateDate:     2020/10/29 上午6:15
 * UpdateUser:     更新者
 * UpdateDate:     2020/10/29 上午6:15
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class WebResponse {
    // 响应代码
    private int code;

    // 响应消息
    private String msg;

    // 响应数据
    private Object data;

    public WebResponse(int code) {
        this(code, "", null);
    }

    public WebResponse(int code, Object data) {
        this(code, "", data);
    }

    public WebResponse(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Object getData() {
        return data;
    }

/*    public void setCode(int code) {
        this.code = code;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }*/

    public void set(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
