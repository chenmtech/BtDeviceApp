package com.cmtech.android.bledeviceapp.util;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

public class KeyBoardUtil {
    /**
     * 自动关闭软键盘
     * @param activity
     */
    public static void closeKeybord(Activity activity) {
        InputMethodManager imm =  (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null) {
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }
}
