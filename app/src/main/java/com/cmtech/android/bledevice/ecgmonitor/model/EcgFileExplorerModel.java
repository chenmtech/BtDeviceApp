package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.core.BleDeviceUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrRecorder;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.bmefile.BmeFileHead30;
import com.vise.log.ViseLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * EcgFileExplorerModel: 心电文件浏览模型类
 * Created by bme on 2018/11/10.
 */

public class EcgFileExplorerModel implements EcgFileListManager.OnSelectFileChangedListener{
    private static final float DEFAULT_SECOND_PER_HGRID = 0.04f; // 缺省横向每个栅格代表的秒数，对应于走纸速度
    private static final float DEFAULT_MV_PER_VGRID = 0.1f; // 缺省纵向每个栅格代表的mV，对应于灵敏度
    private static final int DEFAULT_PIXEL_PER_GRID = 10; // 缺省每个栅格包含的像素个数

    private final File ecgFileDir; // 文件浏览目录

    private final File[] allEcgFiles;

    private EcgFile selectFile; // 选中的EcgFile

    private EcgFileListManager filesManager; // 文件列表管理器

    private IEcgFileExplorerListener listener; // 文件浏览监听器

    private EcgHrRecorder hrRecorder;

    private int pixelPerGrid = DEFAULT_PIXEL_PER_GRID; // 每小格的像素个数
    private int hPixelPerData = Math.round(pixelPerGrid / (DEFAULT_SECOND_PER_HGRID * 125)); // 计算横向分辨率; // 横向分辨率
    private float vValuePerPixel = 65535 * DEFAULT_MV_PER_VGRID / pixelPerGrid; // 计算纵向分辨率; // 纵向分辨率

    private int totalSecond; // 信号总的秒数

    private class OpenFileRunnable implements Runnable {
        private File[] files;

        OpenFileRunnable(File[] files) {
            this.files = files;
        }

        @Override
        public void run() {
            EcgFile ecgFile = null;

            try {
                for(File file : files) {
                    try {
                        ecgFile = EcgFile.open(file.getCanonicalPath());
                        filesManager.addFile(ecgFile);
                        ViseLog.e(ecgFile.toString());
                    } catch (IOException e) {
                        ViseLog.e("To open ecg file is wrong." + file);
                    } finally {
                        if (ecgFile != null) {
                            try {
                                ecgFile.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } finally {
                ViseLog.e("open file thread is interrupted.");
            }
        }
    }

    public EcgFileExplorerModel(File ecgFileDir, IEcgFileExplorerListener listener) throws IOException{
        if(ecgFileDir == null || !ecgFileDir.isDirectory()) {
            throw new IllegalArgumentException();
        }

        this.ecgFileDir = ecgFileDir;


        if(!ecgFileDir.exists() && !ecgFileDir.mkdir()) {
            throw new IOException("磁盘空间不足");
        }

        allEcgFiles = BleDeviceUtil.listDirBmeFiles(ecgFileDir); // 列出所有bme文件

        filesManager = new EcgFileListManager(this);

        this.listener = listener;

        hrRecorder = new EcgHrRecorder(listener);
    }

    public void openAllFiles() {
        Thread t = new Thread(new OpenFileRunnable(allEcgFiles));
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSelectFileChanged(EcgFile ecgFile) {
        selectFile = ecgFile;

        initReplayPara(selectFile);

        if(EcgFileExplorerModel.this.listener != null) {
            EcgFileExplorerModel.this.listener.onUpdateEcgFileList();
        }
    }

    public int getFileNumber() {
        return filesManager.getFileNumber();
    }

    public EcgFile getFile(int index) {
        if(index < 0 || index >= getFileNumber()) return null;

        return filesManager.getFile(index);
    }

    public int getSelectIndex() { return filesManager.getSelectIndex(); }

    public EcgFile getSelectFile() {
        return selectFile;
    }
    public int getSelectFileSampleRate() {
        return selectFile.getSampleRate();
    }
    public int getPixelPerGrid() { return pixelPerGrid; }
    public int gethPixelPerData() { return hPixelPerData; }
    public float getvValuePerPixel() { return vValuePerPixel; }
    public int getTotalSecond() {
        return totalSecond;
    }


    // 获取选中文件的留言列表
    public List<EcgNormalComment> getSelectFileCommentList() {
        if(selectFile == null)
            return new ArrayList<>();
        else {
            User account = AccountManager.getInstance().getAccount();
            boolean found = false;
            for(EcgNormalComment appendix : selectFile.getCommentList()) {
                if(appendix.getCreator().equals(account)) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                selectFile.addComment(EcgNormalComment.createDefaultComment());
            }
            return selectFile.getCommentList();
        }
    }

    public void select(int index) {
        filesManager.select(index);
    }

    // 选中一个文件
    public void select(EcgFile ecgFile) {
        filesManager.select(ecgFile);
    }

    // 删除选中文件
    public void deleteSelectFile() {
        filesManager.deleteSelectFile();
    }

    // 从微信导入文件
    public void importFromWechat() {
        filesManager.importToFromWechat(ecgFileDir);
    }

    // 通过微信分享选中文件
    public void shareSelectFileThroughWechat() {
        filesManager.shareSelectFileThroughWechat();
    }

    // 初始化回放参数
    private void initReplayPara(final EcgFile ecgFile) {
        if(ecgFile == null) return;

        int sampleRate = ecgFile.getSampleRate();
        totalSecond = ecgFile.getDataNum()/sampleRate;
        int value1mV = ((BmeFileHead30)ecgFile.getBmeFileHead()).getCalibrationValue();
        hPixelPerData = Math.round(pixelPerGrid / (DEFAULT_SECOND_PER_HGRID * sampleRate)); // 计算横向分辨率
        vValuePerPixel = value1mV * DEFAULT_MV_PER_VGRID / pixelPerGrid; // 计算纵向分辨率
    }

    // 保存留言信息
    public void saveAppendix() {
        if(selectFile != null) {
            try {
                selectFile.save();
            } catch (IOException e) {
                ViseLog.e("保存留言错误。");
            }
        }
    }

    // 删除心电文件浏览器观察者
    public void removeListener() {
        listener = null;
    }

    public void updateHrInfo() {
        if(selectFile != null) {
            hrRecorder.setHrList(selectFile.getHrList());
            hrRecorder.updateHrInfo(10, 5);
        }
    }

}
