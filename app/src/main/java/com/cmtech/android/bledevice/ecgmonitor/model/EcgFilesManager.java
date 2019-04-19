package com.cmtech.android.bledevice.ecgmonitor.model;

import android.os.Handler;
import android.os.Looper;

import com.cmtech.android.bledevice.core.BleDeviceUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.R;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;

import static cn.sharesdk.framework.Platform.SHARE_FILE;
import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.WECHAT_DOWNLOAD_DIR;

/**
  *
  * ClassName:      EcgFilesManager
  * Description:    Ecg文件管理器
  * Author:         chenm
  * CreateDate:     2019/4/12 上午8:03
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/12 上午8:03
  * UpdateRemark:   画了类图，根据类图优化了接口函数
  * Version:        1.0
 */

class EcgFilesManager {

    public interface OnEcgFilesChangeListener {
        void onSelectFileChanged(EcgFile ecgFile);
        void onFileListChanged(List<EcgFile> fileList);
    }

    private final List<EcgFile> fileList = new Vector<>(); // 文件目录中包含的心电文件列表
    private final List<EcgFile> unmodifiedFileList = Collections.unmodifiableList(fileList);

    private OnEcgFilesChangeListener listener;

    EcgFilesManager() {

    }

    synchronized void setListener(OnEcgFilesChangeListener listener) {
        this.listener = listener;
    }

    // 添加一个文件
    synchronized boolean add(EcgFile file) {
        if(file == null) {
            throw new IllegalArgumentException("The file is null");
        }

        boolean isAdded = false;

        if(!fileList.contains(file)) {
            fileList.add(file);

            sortFilesAsCreateTime(fileList);

            notifyFileListChanged();

            if(fileList.size() == 1) {
                notifySelectFileChanged(file);
            }

            isAdded = true;
        }

        return isAdded;
    }

    // 选中一个文件
    synchronized void select(EcgFile file) {
        if(fileList.contains(file)) {
            notifySelectFileChanged(file);
        }
    }

    // 删除一个文件
    synchronized void delete(EcgFile file) {
        if(file == null) {
            throw new IllegalArgumentException();
        }

        try {
            int index = fileList.indexOf(file);

            if(index != -1) {
                FileUtil.deleteFile(file.getFile());
                if(fileList.remove(file)) {
                    notifyFileListChanged();
                }

                index = (index > fileList.size() - 1) ? fileList.size() - 1 : index;
                select(fileList.get(index));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 从微信导入文件到ecgFileDir目录
    synchronized void importToFromWechat(File ecgFileDir) {
        File wxFileDir = new File(WECHAT_DOWNLOAD_DIR);

        File[] wxFileList = BleDeviceUtil.listDirBmeFiles(wxFileDir);

        boolean updated = false;

        for(File wxFile : wxFileList) {
            EcgFile tmpEcgFile = null;
            EcgFile toEcgFile = null;
            try {
                tmpEcgFile = EcgFile.open(wxFile.getCanonicalPath());
                tmpEcgFile.close();

                File toFile = FileUtil.getFile(ecgFileDir, wxFile.getName());

                if(toFile.exists()) {
                    toEcgFile = EcgFile.open(toFile.getCanonicalPath());
                    if(mergeEcgFileAppendix(tmpEcgFile, toEcgFile))
                        updated = true;
                    wxFile.delete();
                } else {
                    FileUtil.moveFile(wxFile, toFile);
                    toEcgFile = EcgFile.open(toFile.getCanonicalPath());
                    add(toEcgFile);
                    updated = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(tmpEcgFile != null) {
                    try {
                        tmpEcgFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(toEcgFile != null) {
                    try {
                        toEcgFile.save();
                        toEcgFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if(updated) {
            sortFilesAsCreateTime(fileList);

            select(fileList.get(fileList.size() - 1));
        }
    }

    synchronized void close() {
        for(EcgFile file : fileList) {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fileList.clear();
    }


    private void notifyFileListChanged() {
        if(listener != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    listener.onFileListChanged(unmodifiedFileList);
                }
            });
        }
    }

    private void notifySelectFileChanged(final EcgFile selectFile) {
        if(listener != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    listener.onSelectFileChanged(selectFile);
                }
            });
        }
    }

    // 按照创建时间给EcgFile文件列表排序
    private void sortFilesAsCreateTime(List<EcgFile> fileList) {
        if(fileList.size() <= 1) return;

        Collections.sort(fileList, new Comparator<EcgFile>() {
            @Override
            public int compare(EcgFile o1, EcgFile o2) {
                return (int)(o2.getCreateTime() - o1.getCreateTime());
            }
        });
    }

    // 融合EcgFile的附加留言
    private boolean mergeEcgFileAppendix(EcgFile srcFile, EcgFile destFile) {
        List<EcgNormalComment> srcComments = srcFile.getCommentList();
        List<EcgNormalComment> destComments = destFile.getCommentList();
        List<EcgNormalComment> needAddComments = new ArrayList<>();

        for(EcgNormalComment srcComment : srcComments) {
            for(EcgNormalComment destComment : destComments) {
                if(!srcComment.equals(destComment)) {
                    needAddComments.add(srcComment);
                }
            }
        }

        if(needAddComments.isEmpty())
            return false;
        else {
            destFile.addComment(needAddComments);
            return true;
        }
    }

}
