package com.cmtech.android.bledevice.ecgmonitor.model;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.EcgHrStatisticsInfoAnalyzer;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.BmeFileUtil;
import com.vise.log.ViseLog;
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
import static com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.EcgSignalProcessor.HR_FILTER_SECOND;

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

public class EcgFilesManager {
    private final List<EcgFile> ecgFileList = new ArrayList<>(); // 文件目录中包含的心电文件列表
    private final List<EcgFile> unmodifiedFileList = Collections.unmodifiableList(ecgFileList);
    private final OnEcgFilesChangedListener listener; // ECG文件改变监听器
    private EcgFile selectedFile; // 选中的EcgFile

    public interface OnEcgFilesChangedListener {
        void onFileSelected(EcgFile ecgFile);
        void onFileListChanged(List<EcgFile> fileList);
    }

    EcgFilesManager(OnEcgFilesChangedListener listener) {
        this.listener = listener;
    }

    // 打开文件
    void openFile(File file) throws IOException{
        EcgFile ecgFile = EcgFile.open(file.getCanonicalPath());
        if(!add(ecgFile)) {
            ecgFile.close();
            return;
        }

        ViseLog.e(ecgFile.toString());
    }

    // 添加文件
    private synchronized boolean add(EcgFile file) {
        if(file == null) {
            return false;
        }

        boolean isAdded = false;
        if(!ecgFileList.contains(file)) {
            ecgFileList.add(file);
            sortFilesByCreatedTime(ecgFileList);
            notifyFileListChanged();
            if(ecgFileList.size() == 1) {
                select(file);
            }
            isAdded = true;
        }
        return isAdded;
    }

    // 选中文件
    synchronized void select(EcgFile file) {
        if(file == null) {
            selectedFile = null;
            notifySelectedFileChanged(null);
        } else if(ecgFileList.contains(file)) {
            selectedFile = file;
            notifySelectedFileChanged(file);
        }
    }

    // 删除选中的文件
    synchronized void deleteSelectedFile(final Context context) {
        if(selectedFile != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("删除心电记录").setMessage("确定删除该心电记录吗？");

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    delete(selectedFile);
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
        }
    }

    // 删除文件
    private synchronized void delete(EcgFile file) {
        if(file == null) {
            return;
        }

        try {
            int index = ecgFileList.indexOf(file);
            if(index != -1) {
                FileUtil.deleteFile(file.getFile());
                if(ecgFileList.remove(file)) {
                    notifyFileListChanged();
                }
                index = (index > ecgFileList.size() - 1) ? ecgFileList.size() - 1 : index;
                select((index < 0) ? null : ecgFileList.get(index));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized void saveSelectedFileComment() throws IOException{
        if(selectedFile != null) {
            selectedFile.saveFileTail();
        }
    }

    synchronized EcgHrStatisticsInfoAnalyzer getSelectedFileHrStatisticsInfo() {
        if(selectedFile != null) {
            return new EcgHrStatisticsInfoAnalyzer(selectedFile.getHrList(), HR_FILTER_SECOND);
        }
        return null;
    }

    // ******这个功能还没有完成
    // 从srcDir导入文件到destDir
    synchronized void importFiles(File srcDir, File destDir) {
        File[] fileList = BmeFileUtil.listDirBmeFiles(srcDir);
        boolean changed = false;
        for(File srcFile : fileList) {
            EcgFile srcEcgFile = null;
            EcgFile destEcgFile = null;
            try {
                srcEcgFile = EcgFile.open(srcFile.getCanonicalPath());
                srcEcgFile.close();
                File destFile = FileUtil.getFile(destDir, srcFile.getName());
                if(destFile.exists()) {
                    destEcgFile = EcgFile.open(destFile.getCanonicalPath());
                    if(copyComments(srcEcgFile, destEcgFile)) {
                        changed = true;
                        destEcgFile.saveFileTail();
                    }
                    destEcgFile.close();
                } else {
                    FileUtil.copyFile(srcFile, destFile);
                    changed = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(srcEcgFile != null) {
                    try {
                        srcEcgFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(destEcgFile != null) {
                    try {
                        destEcgFile.saveFileTail();
                        destEcgFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // 拷贝文件留言
    private boolean copyComments(EcgFile srcFile, EcgFile destFile) {
        List<EcgNormalComment> srcComments = srcFile.getCommentList();
        List<EcgNormalComment> destComments = destFile.getCommentList();

        boolean update = false;
        boolean needAdd = true;
        for(EcgNormalComment srcComment : srcComments) {
            for(EcgNormalComment destComment : destComments) {
                if(srcComment.getCreator().equals(destComment.getCreator()) && srcComment.getModifyTime() <= destComment.getModifyTime()) {
                    needAdd = false;
                    break;
                }
            }
            if(needAdd) {
                destFile.addComment(srcComment);
                update = true;
            }
            needAdd = true;
        }
        return update;
    }

    // 通过微信分享选中文件
    synchronized void shareSelectedFileThroughWechat(final Context context) {
        if(selectedFile == null) return;
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(SHARE_FILE);
        String fileShortName = selectedFile.getFile().getName();
        sp.setTitle(fileShortName);
        sp.setText(fileShortName);
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_kang);
        sp.setImageData(bmp);
        sp.setFilePath(selectedFile.getFileName());
        Platform platform = ShareSDK.getPlatform(Wechat.NAME);
        platform.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                Toast.makeText(context, "微信分享成功", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                Toast.makeText(context, "分享错误", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancel(Platform platform, int i) {
                Toast.makeText(context, "分享被取消", Toast.LENGTH_SHORT).show();
            }
        });
        platform.share(sp);
    }

    synchronized void close() {
        for(EcgFile file : ecgFileList) {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ecgFileList.clear();
    }

    private void notifyFileListChanged() {
        if(listener != null) {
            listener.onFileListChanged(unmodifiedFileList);
        }
    }

    private void notifySelectedFileChanged(final EcgFile file) {
        if(listener != null) {
            listener.onFileSelected(file);
        }
    }

    // 按照创建时间给EcgFile文件列表排序
    private void sortFilesByCreatedTime(List<EcgFile> fileList) {
        if(fileList.size() <= 1) return;
        Collections.sort(fileList, new Comparator<EcgFile>() {
            @Override
            public int compare(EcgFile o1, EcgFile o2) {
                return (int)(o2.getCreatedTime() - o1.getCreatedTime());
            }
        });
    }
}
