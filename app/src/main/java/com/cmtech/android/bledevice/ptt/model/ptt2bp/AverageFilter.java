package com.cmtech.android.bledevice.ptt.model.ptt2bp;

public class AverageFilter {
    private final int length;
    private final int[] buffer;
    private int position;
    private long sum;

    public AverageFilter(int length) {
        this.length = length;
        buffer = new int[length];
        initialize();
    }

    public void initialize() {
        for(int i = 0; i < length; i++) {
            buffer[i] = 0;
        }
        position = 0;
        sum = 0;
    }

    public int filter(int data) {
        if(position < length) {
            buffer[position++] = data;
            sum += data;
            return (int)(sum/position);
        } else {
            sum -= buffer[0];
            sum += data;
            if (length - 1 >= 0) System.arraycopy(buffer, 1, buffer, 0, length - 1);
            buffer[length-1] = data;
            return (int)(sum/length);
        }
    }
}
