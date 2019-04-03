package com.cmtech.bmefile;

import java.util.Arrays;

/**
 * BmeFileHeadFactory: Bme文件头工厂
 * created by chenm, 2018-02-17
 */

class BmeFileHeadFactory {
    // 用版本号创建文件头
	static BmeFileHead createByVersionCode(byte[] ver) {
		if(Arrays.equals(ver, BmeFileHead10.VER)) {
			return new BmeFileHead10();
		} else if(Arrays.equals(ver, BmeFileHead20.VER)) {
            return new BmeFileHead20();
        } else if(Arrays.equals(ver, BmeFileHead30.VER)) {
		    return new BmeFileHead30();
		} else {
			throw new IllegalArgumentException();
		}
	}

	// 创建缺省文件头，即1.0版本的
	static BmeFileHead createDefault() {
		return new BmeFileHead10();
	}

}
