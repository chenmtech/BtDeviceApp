package com.cmtech.android.bledeviceapp.util;

import java.io.File;
import java.io.FilenameFilter;

public class BmeFileUtil {
    // 列出目录中的所有.bme文件
    public static File[] listDirBmeFiles(File fileDir) {
        if(fileDir == null || !fileDir.exists()) return null;

        return fileDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".bme");
            }
        });
    }
}
