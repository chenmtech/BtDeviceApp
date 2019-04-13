package com.cmtech.android.bledevice.ecgmonitor.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cmtech.android.bledevice.core.BleDeviceUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;

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

    private final List<EcgFile> fileList = Collections.synchronizedList(new ArrayList<EcgFile>()); // 文件目录中包含的心电文件列表

    private final OnEcgFilesChangeListener listener;


    EcgFilesManager(OnEcgFilesChangeListener listener) {
        if(listener == null) {
            throw new IllegalArgumentException();
        }

        this.listener = listener;
    }

    // 添加一个文件
    void add(EcgFile file) {
        if(file != null && !fileList.contains(file)) {
            fileList.add(file);

            sortFilesUsingCreateTime(fileList);

            listener.onFileListChanged(fileList);
        }
    }

    // 选中一个文件
    void select(EcgFile file) {
        if(file == null || file.isActive()) return;

        int index = fileList.indexOf(file);
        if(index != -1) {
            try {
                file = EcgFile.open(file.getFileName());

                listener.onSelectFileChanged(file);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    file.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    // 删除一个文件
    void delete(EcgFile file) {
        if(file != null) {
            try {
                int index = fileList.indexOf(file);

                if(index != -1) {
                    FileUtil.deleteFile(file.getFile());
                    if(fileList.remove(file)) {
                        listener.onFileListChanged(fileList);
                    }

                    index = (index > fileList.size() - 1) ? fileList.size() - 1 : index;
                    select(fileList.get(index));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 用微信分享一个文件
    void shareThroughWechat(EcgFile file) {
        if(file == null) return;

        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(SHARE_FILE);
        String fileShortName = file.getFile().getName();
        sp.setTitle(fileShortName);
        sp.setText(fileShortName);
        Bitmap bmp = BitmapFactory.decodeResource(MyApplication.getContext().getResources(), R.mipmap.ic_kang);
        sp.setImageData(bmp);
        sp.setFilePath(file.getFileName());

        Platform wxPlatform = ShareSDK.getPlatform(Wechat.NAME);
        wxPlatform.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                //Toast.makeText(EcgFileExplorerActivity.this, "分享成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                //Toast.makeText(EcgFileExplorerActivity.this, "分享错误", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel(Platform platform, int i) {
                //Toast.makeText(EcgFileExplorerActivity.this, "分享取消", Toast.LENGTH_SHORT).show();
            }
        });
        // 执行分享
        wxPlatform.share(sp);
    }

    // 从微信导入文件到ecgFileDir目录
    void importToFromWechat(File ecgFileDir) {
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
            sortFilesUsingCreateTime(fileList);

            select(fileList.get(fileList.size() - 1));
        }
    }


    // 按照创建时间给EcgFile文件列表排序
    private void sortFilesUsingCreateTime(List<EcgFile> fileList) {
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
