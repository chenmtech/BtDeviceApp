package com.cmtech.bmefile;

import com.cmtech.bmefile.exception.FileException;

import java.io.IOException;
import java.util.Arrays;

/**
 * BmeFileHeadFactory: Bme文件头工厂
 * created by chenm, 2018-02-17
 */

public class BmeFileHeadFactory {
    // 用版本号创建文件头
	public static BmeFileHead create(byte[] ver) throws IOException {
		if(Arrays.equals(ver, BmeFileHead10.VER)) {
			return new BmeFileHead10();
		} else if(Arrays.equals(ver, BmeFileHead20.VER)) {
            return new BmeFileHead20();
        } else if(Arrays.equals(ver, BmeFileHead30.VER)) {
		    return new BmeFileHead30();
		} else {
			throw new IOException(Arrays.toString(ver) + "不支持此文件版本");
		}
	}

	// 创建缺省文件头，即1.0版本的
	public static BmeFileHead createDefault() {
		return new BmeFileHead10();
	}

}
