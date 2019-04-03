package com.cmtech.android.bledeviceapp.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteOrder;

import static java.nio.ByteOrder.BIG_ENDIAN;

public class DataIOUtil {
    public static String readFixedString(DataInput in, int size) throws IOException {
        StringBuilder b = new StringBuilder(size);
        int i = 0;
        boolean more = true;
        while(more && i < size) {
            char ch = in.readChar();
            i++;
            if(ch == 0) more = false;
            else b.append(ch);
        }
        in.skipBytes(2*(size-i));
        return b.toString();
    }

    public static void writeFixedString(DataOutput out, String s, int size) throws IOException {
        for(int i = 0; i < size; i++) {
            char ch = 0;
            if(i < s.length()) ch = s.charAt(i);
            out.writeChar(ch);
        }
    }

    public static int readInt(DataInput in, ByteOrder byteOrder) throws IOException{
        return (byteOrder == BIG_ENDIAN) ? in.readInt() : ByteUtil.reverseInt(in.readInt());
    }

    public static void writeInt(DataOutput out, int data, ByteOrder byteOrder) throws IOException{
        if ((byteOrder == BIG_ENDIAN)) {
            out.writeInt(data);
        } else {
            out.writeInt(ByteUtil.reverseInt(data));
        }
    }

    public static long readLong(DataInput in, ByteOrder byteOrder) throws IOException{
        return (byteOrder == BIG_ENDIAN) ? in.readLong() : ByteUtil.reverseLong(in.readLong());
    }

    public static void writeLong(DataOutput out, long data, ByteOrder byteOrder) throws IOException{
        if ((byteOrder == BIG_ENDIAN)) {
            out.writeLong(data);
        } else {
            out.writeLong(ByteUtil.reverseLong(data));
        }
    }

    public static double readDouble(DataInput in, ByteOrder byteOrder) throws IOException{
        return (byteOrder == BIG_ENDIAN) ? in.readDouble() : ByteUtil.reverseDouble(in.readDouble());
    }

    public static void writeDouble(DataOutput out, double data, ByteOrder byteOrder) throws IOException{
        if ((byteOrder == BIG_ENDIAN)) {
            out.writeDouble(data);
        } else {
            out.writeDouble(ByteUtil.reverseDouble(data));
        }
    }
}
