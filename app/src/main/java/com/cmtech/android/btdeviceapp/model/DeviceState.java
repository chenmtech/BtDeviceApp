package com.cmtech.android.btdeviceapp.model;

/**
 * Created by bme on 2018/3/14.
 */

public enum DeviceState {

    INIT(0, "等待连接"),
    SCAN_ERROR(1, "扫描超时"),
    CONNECTING(2, "连接中"),
    CONNECT_ERROR(3, "连接错误"),
    CONNECT_SUCCESS(4, "连接成功");


    private int code;  //对应括号里面第一个值,即 0 或 1
    private String description; //对应括号里面第二个值,即不显示 或  显示
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
    private DeviceState(int code, String description) {
        this.code = code;
        this.description = description;
    }

    //通过code,即 0 或 1 获取对应 enum 对象
    public static DeviceState getByCode(Integer code){
        DeviceState state = null;
        for(int i = 0; i < DeviceState.values().length; i++){
            state = DeviceState.values()[i];
            if(code == state.getCode()){
                break;
            }
        }
        return state;
    }
}
