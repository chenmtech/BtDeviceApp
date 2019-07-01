package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;

public interface OnEcgCommentOperateListener {
    void onCommentSaved(); // 保存留言

    void onCommentDeleted(EcgNormalComment comment); // 删除留言
}
