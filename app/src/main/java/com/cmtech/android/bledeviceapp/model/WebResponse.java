package com.cmtech.android.bledeviceapp.model;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      WebResponse
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/10/29 上午6:15
 * UpdateUser:     更新者
 * UpdateDate:     2020/10/29 上午6:15
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class WebResponse {
    private int code;
    private String msg;
    private Object content;

    public WebResponse(int code, Object content) {
        this(code, "", content);
    }

    public WebResponse(int code, String msg, Object content) {
        this.code = code;
        this.msg = msg;
        this.content = content;
    }

    public int getCode() {
        return code;
    }

    public Object getContent() {
        return content;
    }

    public String getMsg() {
        return msg;
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

    public void set(int code, String msg, Object content) {
        this.code = code;
        this.msg = msg;
        this.content = content;
    }
}
