package com.cmtech.android.bledeviceapp.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BmeFileUtil {
    // 列出目录中的所有.bme文件
    public static List<File> listDirBmeFiles(File fileDir) {
        if(fileDir == null || !fileDir.exists()) return null;

        return new ArrayList<>(Arrays.asList(fileDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".bme");
            }
        })));
    }
}
