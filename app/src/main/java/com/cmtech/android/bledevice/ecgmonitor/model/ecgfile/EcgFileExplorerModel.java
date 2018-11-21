package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.model.BleDeviceUtil;
import com.cmtech.bmefile.exception.FileException;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;

import static cn.sharesdk.framework.Platform.SHARE_FILE;
import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECGFILEDIR;

public class EcgFileExplorerModel {

    private static final String WXIMPORT_DIR = Environment.getExternalStorageDirectory().getPath()+"/tencent/MicroMsg/Download";

    private final File fileDir;

    private List<EcgFile> fileList = new ArrayList<>();

    private int selectIndex = -1;

    public int getSelectIndex() { return selectIndex; }

    public List<EcgFile> getFileList() { return fileList; }

    public List<EcgFileComment> getFileCommentList() {
        if(fileList.isEmpty() || selectIndex < 0 || selectIndex >= fileList.size()){
            return new ArrayList<>();
        } else {
            return fileList.get(selectIndex).getEcgFileHead().getCommentList();
        }
    }

    private IEcgFileExplorerObserver observer;


    public EcgFileExplorerModel(File fileDir) throws IllegalArgumentException{
        this.fileDir = fileDir;

        if(!fileDir.exists()) {
            fileDir.mkdir();
        }

        if(!fileDir.isDirectory() || !fileDir.exists()) {
            throw new IllegalArgumentException();
        }

        initFileList();
    }

    private void initFileList() {
        File[] files = BleDeviceUtil.listDirBmeFiles(ECGFILEDIR);
        fileList = createEcgFileList(files);
        sortFileList();
    }

    private void sortFileList() {
        if(fileList.size() <= 1) return;

        fileList.sort(new Comparator<EcgFile>() {
            @Override
            public int compare(EcgFile ecgFile, EcgFile t1) {
                return (int)(ecgFile.getFile().lastModified() - t1.getFile().lastModified());
            }
        });
    }

    private List<EcgFile> createEcgFileList(File[] files) {
        List<EcgFile> ecgFileList = new ArrayList<>();
        for(File file : files) {
            EcgFile ecgFile = null;
            try {
                ecgFile = EcgFile.openBmeFile(file.getCanonicalPath());
                ecgFileList.add(ecgFile);
            } catch (Exception e) {
                ecgFileList.remove(ecgFile);
            } finally {
                try {
                    if(ecgFile != null)
                        ecgFile.close();
                } catch (FileException e) {
                    e.printStackTrace();
                }
            }
        }
        return ecgFileList;
    }

    public void selectFile(int fileIndex) {
        selectIndex = fileIndex;
        if(observer != null) {
            observer.updateSelectFile();
        }
    }

    public void openSelectFile() {
        if(selectIndex >= 0 && selectIndex < fileList.size()) {
            String fileName = fileList.get(selectIndex).getFileName();

            if(observer != null) {
                observer.openFile(fileName);
            }
        }
    }

    public void updateSelectFile() {
        if(selectIndex >= 0 && selectIndex < fileList.size()) {
            String fileName = fileList.get(selectIndex).getFileName();
            EcgFile ecgFile = null;
            try {
                ecgFile = EcgFile.openBmeFile(fileName);
                fileList.set(selectIndex, ecgFile);
            } catch (FileException e) {
                e.printStackTrace();
                return;
            } finally {
                if(ecgFile != null) {
                    try {
                        ecgFile.close();
                    } catch (FileException e) {
                        e.printStackTrace();
                    }
                }
            }

            sortFileList();
            selectIndex = fileList.size()-1;

            if(observer != null) {
                observer.updateFileList();
            }
        }
    }

    public void deleteSelectFile() {
        if(selectIndex >= 0 && selectIndex < fileList.size()) {
            try {
                FileUtil.deleteFile(fileList.get(selectIndex).getFile());
                fileList.remove(selectIndex);

                if(selectIndex > fileList.size()-1) selectIndex = fileList.size()-1;
                if(observer != null) {
                    observer.updateFileList();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void importFromWeixin() {
        File wxFileDir = new File(WXIMPORT_DIR);
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
            } catch (Exception e) {
                fileList.remove(toEcgFile);
            } finally {
                try {
                    if(tmpEcgFile != null)
                        tmpEcgFile.close();
                    if(toEcgFile != null)
                        toEcgFile.close();
                } catch (FileException e) {
                    e.printStackTrace();
                }
            }
        }

        if(hasUpdated) {
            sortFileList();

            selectIndex = fileList.size() - 1;

            if (observer != null) {
                observer.updateFileList();
            }
        }
    }

    private boolean mergeTwoEcgFileComments(EcgFile srcFile, EcgFile destFile) {
        List<EcgFileComment> srcComments = srcFile.getEcgFileHead().getCommentList();
        List<EcgFileComment> destComments = destFile.getEcgFileHead().getCommentList();
        List<EcgFileComment> needAddComments = new ArrayList<>();

        for(EcgFileComment srcComment : srcComments) {
            for(EcgFileComment destComment : destComments) {
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
