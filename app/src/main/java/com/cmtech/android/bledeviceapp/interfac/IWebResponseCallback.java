package com.cmtech.android.bledeviceapp.interfac;

import com.cmtech.android.bledeviceapp.model.WebResponse;

/**
 * 网络操作的响应回调
 */
public interface IWebResponseCallback {
    void onFinish(WebResponse response);
}
