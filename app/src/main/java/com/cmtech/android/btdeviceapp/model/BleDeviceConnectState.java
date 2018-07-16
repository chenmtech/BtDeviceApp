package com.cmtech.android.btdeviceapp.model;

/**
 * Created by bme on 2018/3/14.
 */

public enum BleDeviceConnectState {
    CONNECT_WAITING(0, "等待连接"),
    CONNECT_SCANFAILURE(1, "扫描超时"),
    CONNECT_CONNECTING(2, "连接中"),
    CONNECT_CONNECTFAILURE(3, "连接错误"),
    CONNECT_CONNECTTIMEOUT(4, "连接超时"),
    CONNECT_DISCONNECT(5, "连接断开"),
    CONNECT_DISCONNECTING(6, "连接断开中"),
    CONNECT_SUCCESS(7, "已连接");


    private int code;                   //对应括号里面第一个值
    private String description;         //对应括号里面第二个值
    //如果括号里还有其它定义的值,可定义相应的变量与之对应即可

    //各个变量的getter,setter方法,注意这里是 public 公共的修饰符啊
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    //enum 构造方法,注意这里是 private 私有的修饰符啊
    private BleDeviceConnectState(int code, String description) {
        this.code = code;
        this.description = description;
    }

    //通过code,即 0 或 1 获取对应 enum 对象
    public static BleDeviceConnectState getByCode(Integer code){
        BleDeviceConnectState state = null;
        for(int i = 0; i < BleDeviceConnectState.values().length; i++){
            state = BleDeviceConnectState.values()[i];
            if(code == state.getCode()){
                break;
            }
        }
        return state;
    }
}
