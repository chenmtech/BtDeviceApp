package com.cmtech.android.bledevice.ecgmonitor.model;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.cmtech.android.bledevice.core.BleDeviceUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrInfoObject;
import com.cmtech.android.bledeviceapp.R;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import static com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFileHead.MACADDRESS_CHAR_NUM;
import static com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgSignalProcessor.SECOND_IN_HR_FILTER;

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

    private final File ecgFileDir; // Ecg文件路径

    private final List<File> fileList;

    // 锁保护
    private final List<EcgFile> ecgFileList = new ArrayList<>(); // 文件目录中包含的心电文件列表

    private final List<EcgFile> unmodifiedFileList = Collections.unmodifiableList(ecgFileList);

    // 锁保护
    private EcgFile selectFile; // 选中的EcgFile

    private OnEcgFilesChangeListener listener;

    EcgFilesManager(File ecgFileDir) {
        this.ecgFileDir = ecgFileDir;

        File[] files = BleDeviceUtil.listDirBmeFiles(ecgFileDir); // 列出所有bme文件

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String f1 = o1.getName();
                String f2 = o2.getName();
                long createTime1 = Long.parseLong(f1.substring(MACADDRESS_CHAR_NUM, f1.length()-4));
                long createTime2 = Long.parseLong(f2.substring(MACADDRESS_CHAR_NUM, f2.length()-4));
                if(createTime1 == createTime2) return 0;
                return (createTime2 > createTime1) ? 1 : -1;
            }
        });

        fileList = new ArrayList<>(Arrays.asList(files));
    }

    List<File> getFileList() {
        return fileList;
    }

    void setListener(OnEcgFilesChangeListener listener) {
        this.listener = listener;
    }

    // 添加一个文件
    synchronized boolean add(EcgFile file) {
        if(file == null) {
            return false;
        }

        boolean isAdded = false;

        if(!ecgFileList.contains(file)) {
            ecgFileList.add(file);

            sortFilesAsCreateTime(ecgFileList);

            notifyFileListChanged();

            if(ecgFileList.size() == 1) {
                select(file);
            }

            isAdded = true;
        }

        return isAdded;
    }

    // 选中一个文件
    synchronized void select(EcgFile file) {
        if(file == null) {
            selectFile = null;

            notifySelectFileChanged(null);

        } else if(ecgFileList.contains(file)) {
            selectFile = file;

            notifySelectFileChanged(file);

        }
    }

    // 删除选中文件
    synchronized void deleteSelectFile(final Context context) {
        if(selectFile != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setTitle("删除心电信号");

            builder.setMessage("确定删除该心电信号吗？");

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    delete(selectFile);
                }
            });

            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            builder.show();
        }
    }

    // ******这个功能还没有完成
    // 从微信导入文件到ecgFileDir目录
    synchronized void importFromWechat() {
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
            sortFilesAsCreateTime(ecgFileList);

            select(ecgFileList.get(ecgFileList.size() - 1));
        }
    }

    synchronized void saveSelectFileComment() throws IOException{
        if(selectFile != null) {

            selectFile.save();

        }
    }

    synchronized EcgHrInfoObject getSelectFileHrInfo() {
        if(selectFile != null) {
            return new EcgHrInfoObject(selectFile.getHrList(), SECOND_IN_HR_FILTER);
        }
        return null;
    }

    // 通过微信分享一个文件
    synchronized void shareSelectFileThroughWechat(final Context context) {
        if(selectFile == null) return;

        Platform.ShareParams sp = new Platform.ShareParams();

        sp.setShareType(SHARE_FILE);

        String fileShortName = selectFile.getFile().getName();

        sp.setTitle(fileShortName);

        sp.setText(fileShortName);

        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_kang);

        sp.setImageData(bmp);

        sp.setFilePath(selectFile.getFileName());

        Platform wxPlatform = ShareSDK.getPlatform(Wechat.NAME);

        wxPlatform.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                Toast.makeText(context, "分享成功", Toast.LENGTH_SHORT).show();
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

        wxPlatform.share(sp);
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


    // 删除一个文件
    private void delete(EcgFile file) {
        if(file == null) {
            throw new IllegalArgumentException();
        }

        try {
            int index = ecgFileList.indexOf(file);

            if(index != -1) {
                FileUtil.deleteFile(file.getFile());

                fileList.remove(file.getFile());

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

    private void notifyFileListChanged() {
        if(listener != null) {
            listener.onFileListChanged(unmodifiedFileList);
        }
    }

    private void notifySelectFileChanged(final EcgFile file) {
        if(listener != null) {
            listener.onSelectFileChanged(file);
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
