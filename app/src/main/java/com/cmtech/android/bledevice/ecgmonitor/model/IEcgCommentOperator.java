package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgComment;

public interface IEcgCommentOperator {
    // 删除一条留言
    void deleteComment(EcgComment comment);

    // 定位一条留言
    void locateComment(EcgComment comment);
}
