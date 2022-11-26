package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

/**
 * 信号注解类
 */
public class SignalAnnotation {
    // 位置
    private final int pos;

    // 注解符号字符串
    private final String symbol;

    public SignalAnnotation(int pos, String symbol) {
        this.pos = pos;
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getPos() {
        return pos;
    }
}
