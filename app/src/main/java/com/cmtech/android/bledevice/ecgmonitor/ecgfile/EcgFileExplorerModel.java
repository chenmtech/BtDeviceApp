package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorDevice;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.model.BleDeviceUtil;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;

import static cn.sharesdk.framework.Platform.SHARE_FILE;

public class EcgFileExplorerModel {

    private static final String WXIMPORT_DIR = Environment.getExternalStorageDirectory().getPath()+"/tencent/MicroMsg/Download";

    private final File fileDir;

    private List<EcgFile> fileList = new ArrayList<>();

    public List<EcgFile> getFileList() { return fileList; }

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
        File[] files = BleDeviceUtil.listDirBmeFiles(EcgMonitorDevice.ECGFILEDIR);
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return (int)(file.lastModified() - t1.lastModified());
            }
        });
        fileList = createEcgFileList(files);
    }

    private List<EcgFile> createEcgFileList(File[] files) {
        List<EcgFile> ecgFileList = new ArrayList<>();
        for(File file : files) {
            try {
                EcgFile ecgFile = EcgFile.openBmeFile(file.getCanonicalPath());
                ecgFileList.add(ecgFile);
                ecgFile.close();
            } catch (Exception e) {
                continue;
            }
        }
        return ecgFileList;
    }

    public void importFromWeixin() {
        File wxFileDir = new File(WXIMPORT_DIR);
        File[] wxFileList = BleDeviceUtil.listDirBmeFiles(wxFileDir);

        for(File wxFile : wxFileList) {
            try {
                EcgFile tmpFile = EcgFile.openBmeFile(wxFile.getCanonicalPath());
                tmpFile.close();

                File toFile = FileUtil.getFile(fileDir, wxFile.getName());
                if(toFile.exists()) {
                    fileList.remove(toFile);
                    toFile.delete();
                }

                FileUtil.moveFile(wxFile, toFile);
                tmpFile = EcgFile.openBmeFile(toFile.getCanonicalPath());
                tmpFile.close();
                fileList.add(tmpFile);

                if(observer != null) {
                    observer.updateFileList(fileList.size()-1);
                }
            } catch (Exception e) {
                continue;
            }
        }
    }

    // 用微信分享BME文件
    private void shareThroughWechat(int fileIndex) {
        EcgFile selectedFile = fileList.get(fileIndex);

        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(SHARE_FILE);
        sp.setTitle(selectedFile.getFile().getName());
        sp.setText(selectedFile.getFile().getName());

        Bitmap bmp= BitmapFactory.decodeResource(MyApplication.getContext().getResources(), R.mipmap.ic_cmiot_16);
        sp.setImageData(bmp);

        sp.setFilePath(selectedFile.getFileName());
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

    public void openFile(int fileIndex) {
        String fileName = fileList.get(fileIndex).getFileName();
        EcgFileExplorerActivity activity = (EcgFileExplorerActivity)observer;
        Intent intent = new Intent(activity, EcgFileReplayActivity.class);
        intent.putExtra("fileName", fileName);
        activity.startActivity(intent);
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
