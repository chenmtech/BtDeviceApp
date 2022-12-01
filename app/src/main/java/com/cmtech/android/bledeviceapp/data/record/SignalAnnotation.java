package com.cmtech.android.bledeviceapp.data.record;

/**
 * 信号注解类
 */
public class SignalAnnotation {
    // 位置
    private final int pos;

    // 注解符号字符串
    private final String symbol;

    // 注解内容
    private final String content;

    public SignalAnnotation(int pos, String symbol) {
        this(pos, symbol, "");
    }

    public SignalAnnotation(int pos, String symbol, String content) {
        this.pos = pos;
        this.symbol = symbol;
        this.content = content;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getPos() {
        return pos;
    }

    public String getContent() {
        return content;
    }
}
