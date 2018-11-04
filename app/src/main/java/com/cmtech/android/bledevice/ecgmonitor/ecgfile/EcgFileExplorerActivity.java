package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import android.content.Intent;
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

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorDevice;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
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

public class EcgFileExplorerActivity extends AppCompatActivity {
    private static final String TAG = "EcgFileExplorerActivity";
    private static final String WXIMPORT_DIR = Environment.getExternalStorageDirectory().getPath()+"/tencent/MicroMsg/Download";

    private List<File> fileList = new ArrayList<>();
    private EcgFileAdapter fileAdapter;
    private RecyclerView rvFileList;

    private EcgFile selectedFile;

    private List<EcgFileComment> commentList = new ArrayList<>();
    private EcgReportAdapter reportAdapter;
    private RecyclerView rvReportList;

    private Button btnEcgShare;
    private Button btnImportFromWX;
    private Button btnOpenFile;

    private TextView tvCreatedTime;
    private TextView tvMacAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgfile_explorer);

        tvCreatedTime = findViewById(R.id.tv_ecgfile_createtime);
        tvMacAddress = findViewById(R.id.tv_ecgfile_macaddress);

        if(!EcgMonitorDevice.ECGFILEDIR.exists()) {
            EcgMonitorDevice.ECGFILEDIR.mkdir();
        }

        File[] files = BleDeviceUtil.listDirBmeFiles(EcgMonitorDevice.ECGFILEDIR);
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return (int)(file.lastModified() - t1.lastModified());
            }
        });
        fileList = new ArrayList<>(Arrays.asList(files));

        rvFileList = findViewById(R.id.rv_ecgfile_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvFileList.setLayoutManager(layoutManager);
        rvFileList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        fileAdapter = new EcgFileAdapter(fileList, this);
        rvFileList.setAdapter(fileAdapter);
        fileAdapter.notifyDataSetChanged();
        if(fileAdapter.getItemCount() > 1)
            rvFileList.smoothScrollToPosition(fileAdapter.getItemCount()-1);

        rvReportList = findViewById(R.id.rv_ecgfile_report);
        LinearLayoutManager reportLayoutManager = new LinearLayoutManager(this);
        rvReportList.setLayoutManager(reportLayoutManager);
        rvReportList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        reportAdapter = new EcgReportAdapter(commentList);
        rvReportList.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        if(reportAdapter.getItemCount() > 1)
            rvReportList.smoothScrollToPosition(reportAdapter.getItemCount()-1);


        btnEcgShare = findViewById(R.id.btn_ecgfile_share);
        btnEcgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareBmeFileThroughWechat();
            }
        });

        btnImportFromWX = findViewById(R.id.btn_ecgfile_import);
        btnImportFromWX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deselectFile();

                File wxFileDir = new File(WXIMPORT_DIR);
                try {
                    File[] wxFileList = BleDeviceUtil.listDirBmeFiles(wxFileDir);
                    for(File wxFile : wxFileList) {
                        File toFile = FileUtil.getFile(EcgMonitorDevice.ECGFILEDIR, wxFile.getName());

                        for(File file : fileList) {
                            if(file.getName().equalsIgnoreCase(toFile.getName())) {
                                fileList.remove(file);
                            }
                        }

                        if(toFile.exists()) toFile.delete();

                        FileUtil.moveFile(wxFile, toFile);
                        fileList.add(toFile);
                        fileAdapter.setSelectItem(-1);
                        fileAdapter.notifyDataSetChanged();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnOpenFile = findViewById(R.id.btn_ecgfile_open);
        btnOpenFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedFile == null) return;

                String fileName;
                try {
                    fileName = selectedFile.getFileName();
                    selectedFile.close();
                    Intent intent = new Intent(EcgFileExplorerActivity.this, EcgFileReplayActivity.class);
                    intent.putExtra("fileName", fileName);
                    startActivity(intent);
                } catch (FileException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void deleteFile(File file) {
        deselectFile();

        try {
            FileUtil.deleteFile(file);
            fileList.remove(file);
            fileAdapter.notifyDataSetChanged();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                Toast.makeText(EcgFileExplorerActivity.this, "分享成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                Toast.makeText(EcgFileExplorerActivity.this, "分享错误", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel(Platform platform, int i) {
                Toast.makeText(EcgFileExplorerActivity.this, "分享取消", Toast.LENGTH_SHORT).show();
            }
        });
        // 执行分享
        wxPlatform.share(sp);
    }

    private void deselectFile() {
        if(selectedFile != null) {
            try {
                selectedFile.close();
                selectedFile = null;
                tvCreatedTime.setText("");
                tvMacAddress.setText("");
                commentList.clear();
                reportAdapter.notifyDataSetChanged();
            } catch (FileException e) {
                e.printStackTrace();
            }
        }
    }

    public void selectFile(File file) {
        if(selectedFile != null) {
            deselectFile();
        }

        try {
            selectedFile = EcgFile.openBmeFile(file.getCanonicalPath());
            tvCreatedTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(selectedFile.getEcgFileHead().getFileCreatedTime()));
            tvMacAddress.setText(selectedFile.getEcgFileHead().getMacAddress());
            commentList.addAll(selectedFile.getEcgFileHead().getCommentList());
            reportAdapter.notifyDataSetChanged();
            selectedFile.close();
        } catch (FileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
