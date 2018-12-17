package com.cmtech.bmefile;

/**
 * BmeFileDataType: Bme文件可保存的数据类型
 * created by chenm, 2018-02-12
 */

public enum BmeFileDataType {
	INT32(0),    //int
    UINT8(1),    //unsigned char    
	DOUBLE(5),   //double
	UNKNOWN(-1); //未知

    private int code;

    BmeFileDataType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    // 获取类型字节长度
    public int getTypeLength() {
        switch (code) {
            case 0:
                return 4;
            case 1:
                return 1;
            case 5:
                return 8;
            default:
                return -1;
        }
    }

    // 由code获取对应的BmeFileDataType
    public static BmeFileDataType getFromCode(int code) {
        for(BmeFileDataType ele : BmeFileDataType.values()) {
            if(ele.code == code) {
                return ele;
            }
        }
        return UNKNOWN;
    }
}
