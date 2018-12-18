package com.cmtech.android.bledevice.ecgmonitor.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.BleDeviceUtil;
import com.cmtech.bmefile.exception.FileException;
import com.vise.log.ViseLog;
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
import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECGFILEDIR;
import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.WECHAT_DOWNLOAD_DIR;

public class EcgFileExplorerModel {
    private final File fileDir;

    private List<EcgFile> fileList = new ArrayList<>();

    private int selectIndex = -1;

    public int getSelectIndex() { return selectIndex; }

    public List<EcgFile> getFileList() { return fileList; }

    public List<EcgComment> getFileCommentList() {
        if(fileList.isEmpty() || selectIndex < 0 || selectIndex >= fileList.size()){
            return new ArrayList<>();
        } else {
            return fileList.get(selectIndex).getCommentList();
        }
    }

    private IEcgFileExplorerObserver observer;


    public EcgFileExplorerModel(File fileDir) throws IllegalArgumentException, IOException{
        if(fileDir == null || !fileDir.isDirectory()) {
            throw new IllegalArgumentException();
        }

        this.fileDir = fileDir;

        if(!fileDir.exists() && !fileDir.mkdir()) {
            throw new IOException("磁盘空间不足");
        }

        initFileList();
    }

    private void initFileList() {
        File[] files = BleDeviceUtil.listDirBmeFiles(ECGFILEDIR);
        fileList = createEcgFileList(files);
        sortFileList();
    }

    private List<EcgFile> createEcgFileList(File[] files) {
        List<EcgFile> ecgFileList = new ArrayList<>();
        for(File file : files) {
            EcgFile ecgFile = null;
            try {
                ecgFile = EcgFile.openBmeFile(file.getCanonicalPath());
                ecgFileList.add(ecgFile);
            } catch (IOException e) {
                ecgFileList.remove(ecgFile);
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

    private void sortFileList() {
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
        selectIndex = index;
        if(observer != null) {
            observer.update();
        }
    }

    // 播放选中的文件
    public void replaySelectedFile() {
        if(selectIndex >= 0 && selectIndex < fileList.size()) {
            String fileName = fileList.get(selectIndex).getFileName();

            if(observer != null) {
                observer.replay(fileName);
            }
        }
    }

    // 重新加载所选文件
    public void reloadSelectedFile() {
        if(selectIndex >= 0 && selectIndex < fileList.size()) {
            String fileName = fileList.get(selectIndex).getFileName();
            EcgFile ecgFile = null;
            try {
                ecgFile = EcgFile.openBmeFile(fileName);
                fileList.set(selectIndex, ecgFile);
                ViseLog.e(ecgFile.getCommentString());
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

            sortFileList();
            selectIndex = fileList.size()-1;

            if(observer != null) {
                observer.update();
            }
        }
    }

    // 删除所选文件
    public void deleteSelectedFile() {
        if(selectIndex >= 0 && selectIndex < fileList.size()) {
            try {
                FileUtil.deleteFile(fileList.get(selectIndex).getFile());
                fileList.remove(selectIndex);

                if(selectIndex > fileList.size()-1) selectIndex = fileList.size()-1;
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

        boolean hasUpdated = false;

        for(File wxFile : wxFileList) {
            EcgFile tmpEcgFile = null;
            EcgFile toEcgFile = null;
            try {
                tmpEcgFile = EcgFile.openBmeFile(wxFile.getCanonicalPath());
                tmpEcgFile.close();

                File toFile = FileUtil.getFile(fileDir, wxFile.getName());

                if(toFile.exists()) {
                    toEcgFile = EcgFile.openBmeFile(toFile.getCanonicalPath());
                    if(mergeTwoEcgFileComments(tmpEcgFile, toEcgFile))
                        hasUpdated = true;
                    wxFile.delete();
                } else {
                    FileUtil.moveFile(wxFile, toFile);
                    toEcgFile = EcgFile.openBmeFile(toFile.getCanonicalPath());
                    fileList.add(toEcgFile);
                    hasUpdated = true;
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

        if(hasUpdated) {
            sortFileList();

            selectIndex = fileList.size() - 1;

            if (observer != null) {
                observer.update();
            }
        }
    }

    private boolean mergeTwoEcgFileComments(EcgFile srcFile, EcgFile destFile) {
        List<EcgComment> srcComments = srcFile.getEcgFileTail().getCommentList();
        List<EcgComment> destComments = destFile.getEcgFileTail().getCommentList();
        List<EcgComment> needAddComments = new ArrayList<>();

        for(EcgComment srcComment : srcComments) {
            for(EcgComment destComment : destComments) {
                if(!srcComment.equals(destComment)) {
                    needAddComments.add(srcComment);
                }
            }
        }

        if(needAddComments.isEmpty())
            return false;
        else {
            destFile.addComments(needAddComments);
            return true;
        }
    }

    // 用微信分享BME文件
    public void shareSelectFileThroughWechat() {
        if(selectIndex < 0 || selectIndex >= fileList.size()) return;

        EcgFile sharedFile = fileList.get(selectIndex);

        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(SHARE_FILE);
        String fileShortName = sharedFile.getFile().getName();
        sp.setTitle(fileShortName);
        sp.setText(fileShortName);

        Bitmap bmp= BitmapFactory.decodeResource(MyApplication.getContext().getResources(), R.mipmap.ic_kang);
        sp.setImageData(bmp);

        sp.setFilePath(sharedFile.getFileName());
        Platform wxPlatform = ShareSDK.getPlatform (Wechat.NAME);
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
