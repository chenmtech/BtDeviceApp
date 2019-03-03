package com.cmtech.android.bledevice.ecgmonitor.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.IEcgAppendix;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevice.core.BleDeviceUtil;
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
import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.WECHAT_DOWNLOAD_DIR;

/**
 * EcgFileExplorerModel: 心电文件浏览模型类
 * Created by bme on 2018/11/10.
 */

public class EcgFileExplorerModel {
    private final File fileDir; // 文件目录
    private final List<EcgFile> fileList; // 文件目录中包含的心电文件列表
    private int currentSelectIndex = -1; // 当前选择的文件在文件列表中的索引号
    private IEcgFileExplorerObserver observer; // 文件浏览器的观察者，主要给Activity用


    public int getCurrentSelectIndex() { return currentSelectIndex; }

    public List<EcgFile> getFileList() { return fileList; }

    public List<IEcgAppendix> getSelectedFileAppendixList() {
        if(fileList.isEmpty() || currentSelectIndex < 0 || currentSelectIndex >= fileList.size()){
            return new ArrayList<>();
        } else {
            return fileList.get(currentSelectIndex).getAppendixList();
        }
    }

    public int getSelectedFileSampleRate() {
        if(fileList.isEmpty() || currentSelectIndex < 0 || currentSelectIndex >= fileList.size()){
            return -1;
        } else {
            return fileList.get(currentSelectIndex).getFs();
        }
    }

    public EcgFileExplorerModel(File fileDir) throws IOException{
        if(fileDir == null || !fileDir.isDirectory()) {
            throw new IllegalArgumentException();
        }

        this.fileDir = fileDir;

        if(!fileDir.exists() && !fileDir.mkdir()) {
            throw new IOException("磁盘空间不足");
        }

        fileList = initEcgFileList(fileDir);
    }

    // 初始化文件目录中的EcgFile列表
    private List<EcgFile> initEcgFileList(File fileDir) {
        File[] files = BleDeviceUtil.listDirBmeFiles(fileDir);
        List<EcgFile> fileList = createEcgFileList(files);
        sortFileList(fileList);
        return fileList;
    }

    // 创建相应的EcgFile列表
    private List<EcgFile> createEcgFileList(File[] files) {
        List<EcgFile> ecgFileList = new ArrayList<>();
        for(File file : files) {
            EcgFile ecgFile = null;
            try {
                ecgFile = EcgFile.open(file.getCanonicalPath());
                ecgFileList.add(ecgFile);
            } catch (IOException e) {
                ViseLog.e("打开心电文件失败" + file);
            } finally {
                if(ecgFile != null) {
                    try {
                        ecgFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return ecgFileList;
    }

    // 给EcgFile文件列表按照最后修改时间排序
    private void sortFileList(List<EcgFile> fileList) {
        if(fileList.size() <= 1) return;

        Collections.sort(fileList, new Comparator<EcgFile>() {
            @Override
            public int compare(EcgFile o1, EcgFile o2) {
                return (int)(o1.getFile().lastModified() - o2.getFile().lastModified());
            }
        });
    }

    // 选中一个文件
    public void select(int index) {
        if(index < 0 || index > fileList.size()-1) return;

        currentSelectIndex = index;
        if(observer != null) {
            observer.update();
        }
    }

    // 播放选中的文件
    public void replaySelectedFile() {
        if(currentSelectIndex >= 0 && currentSelectIndex < fileList.size()) {
            String fileName = fileList.get(currentSelectIndex).getFileName();

            if(observer != null) {
                observer.replay(fileName);
            }
        }
    }

    // 重新加载所选文件
    public void reloadSelectedFile() {
        if(currentSelectIndex >= 0 && currentSelectIndex < fileList.size()) {
            String fileName = fileList.get(currentSelectIndex).getFileName();
            EcgFile ecgFile = null;
            try {
                ecgFile = EcgFile.open(fileName);
                fileList.set(currentSelectIndex, ecgFile);
                ViseLog.w(ecgFile.getAppendixString());
            } catch (IOException e) {
                ViseLog.e(e);
                return;
            } finally {
                if(ecgFile != null) {
                    try {
                        ecgFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            sortFileList(fileList);
            currentSelectIndex = fileList.size()-1;

            if(observer != null) {
                observer.update();
            }
        }
    }

    // 删除所选文件
    public void deleteSelectedFile() {
        if(currentSelectIndex >= 0 && currentSelectIndex < fileList.size()) {
            try {
                FileUtil.deleteFile(fileList.get(currentSelectIndex).getFile());
                fileList.remove(currentSelectIndex);

                if(currentSelectIndex > fileList.size()-1) currentSelectIndex = fileList.size()-1;
                if(observer != null) {
                    observer.update();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 从微信导入文件
    public void importFromWechat() {
        File wxFileDir = new File(WECHAT_DOWNLOAD_DIR);
        File[] wxFileList = BleDeviceUtil.listDirBmeFiles(wxFileDir);

        boolean updated = false;

        for(File wxFile : wxFileList) {
            EcgFile tmpEcgFile = null;
            EcgFile toEcgFile = null;
            try {
                tmpEcgFile = EcgFile.open(wxFile.getCanonicalPath());
                tmpEcgFile.close();

                File toFile = FileUtil.getFile(fileDir, wxFile.getName());

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
                        toEcgFile.saveFileTail();
                        toEcgFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if(updated) {
            sortFileList(fileList);

            currentSelectIndex = fileList.size() - 1;

            if (observer != null) {
                observer.update();
            }
        }
    }

    // 融合EcgFile的附加留言
    private boolean mergeEcgFileAppendix(EcgFile srcFile, EcgFile destFile) {
        List<IEcgAppendix> srcComments = srcFile.getEcgFileTail().getAppendixList();
        List<IEcgAppendix> destComments = destFile.getEcgFileTail().getAppendixList();
        List<IEcgAppendix> needAddComments = new ArrayList<>();

        for(IEcgAppendix srcComment : srcComments) {
            for(IEcgAppendix destComment : destComments) {
                if(!srcComment.equals(destComment)) {
                    needAddComments.add(srcComment);
                }
            }
        }

        if(needAddComments.isEmpty())
            return false;
        else {
            destFile.addAppendix(needAddComments);
            return true;
        }
    }

    // 用微信分享BME文件
    public void shareSelectedFileThroughWechat() {
        if(currentSelectIndex < 0 || currentSelectIndex >= fileList.size()) return;

        EcgFile sharedFile = fileList.get(currentSelectIndex);

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

    // 登记心电文件浏览器观察者
    public void registerEcgFileExplorerObserver(IEcgFileExplorerObserver observer) {
        this.observer = observer;
    }

    // 删除心电文件浏览器观察者
    public void removeEcgFileExplorerObserver() {
        observer = null;
    }

}
