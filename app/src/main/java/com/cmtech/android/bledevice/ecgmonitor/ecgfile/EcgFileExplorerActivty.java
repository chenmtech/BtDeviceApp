package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecgmonitor.EcgFileAdapter;
import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.EcgReplayActivity;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.model.BleDeviceUtil;
import com.cmtech.dsp.exception.FileException;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
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

public class EcgFileExplorerActivty extends AppCompatActivity {
    private static final String TAG = "EcgFileExplorerActivty";

    private List<File> fileList = new ArrayList<>();
    private EcgFileAdapter fileAdapter;
    private RecyclerView rvFileList;
    private EcgFile selectedFile;

    private Button btnEcgShare;
    private Button btnImportFromWX;
    private Button btnEcgDelete;

    private TextView tvCurComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgfile_explorer);

        if(!EcgMonitorDevice.ECGFILEDIR.exists()) {
            EcgMonitorDevice.ECGFILEDIR.mkdir();
        }

        File[] files = BleDeviceUtil.listDirBmeFiles(EcgMonitorDevice.ECGFILEDIR);
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return file.getName().compareTo(t1.getName());
            }
        });
        fileList = new ArrayList<>(Arrays.asList(files));

        rvFileList = findViewById(R.id.rvEcgFileList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvFileList.setLayoutManager(layoutManager);
        rvFileList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        fileAdapter = new EcgFileAdapter(fileList, null);
        rvFileList.setAdapter(fileAdapter);
        fileAdapter.notifyDataSetChanged();

        btnEcgShare = findViewById(R.id.btn_ecgreplay_share);
        btnEcgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareBmeFileThroughWechat();
            }
        });

        btnImportFromWX = findViewById(R.id.btn_ecgreplay_import);
        btnImportFromWX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //deselectFile();
                File wxFileDir = new File(Environment.getExternalStorageDirectory().getPath()+"/tencent/MicroMsg/Download");
                try {
                    File[] wxFileList = BleDeviceUtil.listDirBmeFiles(wxFileDir);
                    for(File file : wxFileList) {
                        File toFile = FileUtil.getFile(EcgMonitorDevice.ECGFILEDIR, file.getName());

                        for(File file1 : fileList) {
                            if(file1.getName().equalsIgnoreCase(toFile.getName())) {
                                fileList.remove(file1);
                            }
                        }

                        if(toFile.exists()) toFile.delete();

                        FileUtil.moveFile(file, toFile);
                        fileList.add(toFile);
                        fileAdapter.setSelectItem(-1);
                        fileAdapter.notifyDataSetChanged();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnEcgDelete = findViewById(R.id.btn_ecgreplay_delete);
        btnEcgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (selectedFile != null) {
                        File tmpFile = selectedFile.getFile();

                        FileUtil.deleteFile(tmpFile);
                        fileList.remove(fileAdapter.getSelectItem());
                        fileAdapter.notifyDataSetChanged();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    // 用微信分享BME文件
    private void shareBmeFileThroughWechat() {
        if(selectedFile == null) return;

        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(SHARE_FILE);
        sp.setTitle(selectedFile.getFile().getName());
        sp.setText(selectedFile.getFile().getName());
        Bitmap bmp= BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cmiot_16);
        sp.setImageData(bmp);
        sp.setFilePath(selectedFile.getFileName());
        Platform wxPlatform = ShareSDK.getPlatform (Wechat.NAME);
        wxPlatform.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                Toast.makeText(EcgFileExplorerActivty.this, "分享成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                Toast.makeText(EcgFileExplorerActivty.this, "分享错误", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel(Platform platform, int i) {
                Toast.makeText(EcgFileExplorerActivty.this, "分享取消", Toast.LENGTH_SHORT).show();
            }
        });
        // 执行分享
        wxPlatform.share(sp);
    }

}
