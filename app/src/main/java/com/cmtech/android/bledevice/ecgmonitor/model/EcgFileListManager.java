package com.cmtech.android.bledevice.ecgmonitor.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cmtech.android.bledevice.core.BleDeviceUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;

import static cn.sharesdk.framework.Platform.SHARE_FILE;
import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.WECHAT_DOWNLOAD_DIR;

public class EcgFileListManager {
    private final List<EcgFile> fileList = Collections.synchronizedList(new ArrayList<EcgFile>()); // 文件目录中包含的心电文件列表

    private EcgFile selectFile;

    public interface OnSelectFileChangedListener {
        void onSelectFileChanged(EcgFile ecgFile);
    }

    private final OnSelectFileChangedListener listener;



    public EcgFileListManager(OnSelectFileChangedListener listener) {
        this.listener = listener;
    }

    public void addFile(EcgFile ecgFile) {
        fileList.add(ecgFile);
    }

    // 按照创建时间给EcgFile文件列表排序
    private void sortFileList(List<EcgFile> fileList) {
        if(fileList.size() <= 1) return;

        Collections.sort(fileList, new Comparator<EcgFile>() {
            @Override
            public int compare(EcgFile o1, EcgFile o2) {
                return (int)(o1.getCreateTime() - o2.getCreateTime());
            }
        });
    }

    public EcgFile getSelectFile() {
        return selectFile;
    }

    public int getFileNumber() {
        return fileList.size();
    }

    public EcgFile getFile(int index) {
        if(index < 0 || index >= fileList.size()) return null;
        return fileList.get(index);
    }

    public int getSelectIndex() {
        return fileList.indexOf(selectFile);
    }

    public void select(int index) {
        if(index < 0 || index >= fileList.size()) select(null);
        else select(fileList.get(index));
    }

    // 选中某个文件
    public void select(EcgFile file) {
        if(file != null && file.equals(selectFile)) return;

        // 关闭之前的文件
        if (selectFile != null) {
            try {
                selectFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        selectFile = file;

        if(selectFile != null) {
            int index = fileList.indexOf(selectFile);
            if(index != -1) {
                // 打开当前选中的文件
                try {
                    selectFile = EcgFile.open(selectFile.getFileName());
                    fileList.set(index, selectFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (listener != null) listener.onSelectFileChanged(selectFile);
    }

    // 删除所选文件
    public void deleteSelectFile() {
        if(selectFile != null) {
            try {
                int index = fileList.indexOf(selectFile);

                if(index != -1) {
                    FileUtil.deleteFile(selectFile.getFile());
                    fileList.remove(selectFile);

                    index = (index > fileList.size() - 1) ? fileList.size() - 1 : index;
                    select(fileList.get(index));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 从微信导入文件
    public void importToFromWechat(File ecgFileDir) {
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
                    fileList.add(toEcgFile);
                    updated = true;
                }
            } catch (IOException e) {
                fileList.remove(toEcgFile);
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
            sortFileList(fileList);

            select(fileList.get(fileList.size() - 1));
        }
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

    // 用微信分享BME文件
    public void shareSelectFileThroughWechat() {
        if(selectFile == null) return;

        EcgFile sharedFile = selectFile;

        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(SHARE_FILE);
        String fileShortName = sharedFile.getFile().getName();
        sp.setTitle(fileShortName);
        sp.setText(fileShortName);
        Bitmap bmp = BitmapFactory.decodeResource(MyApplication.getContext().getResources(), R.mipmap.ic_kang);
        sp.setImageData(bmp);
        sp.setFilePath(sharedFile.getFileName());

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

}
