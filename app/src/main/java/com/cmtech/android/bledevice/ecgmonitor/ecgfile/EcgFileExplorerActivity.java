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

    private List<EcgFile> fileList = new ArrayList<>();
    private EcgFileAdapter fileAdapter;
    private RecyclerView rvFileList;

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
        fileList = createEcgFileList(files);


        rvFileList = findViewById(R.id.rv_ecgfile_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvFileList.setLayoutManager(layoutManager);
        rvFileList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        fileAdapter = new EcgFileAdapter(fileList, this);
        rvFileList.setAdapter(fileAdapter);
        fileAdapter.notifyDataSetChanged();


        rvReportList = findViewById(R.id.rv_ecgfile_report);
        LinearLayoutManager reportLayoutManager = new LinearLayoutManager(this);
        rvReportList.setLayoutManager(reportLayoutManager);
        rvReportList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        reportAdapter = new EcgReportAdapter(commentList);
        rvReportList.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        if(reportAdapter.getItemCount() >= 1)
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
                //deselectFile();

                File wxFileDir = new File(WXIMPORT_DIR);
                File[] wxFileList = BleDeviceUtil.listDirBmeFiles(wxFileDir);

                for(File wxFile : wxFileList) {
                    try {
                        EcgFile tmpFile = EcgFile.openBmeFile(wxFile.getCanonicalPath());
                        tmpFile.close();

                        File toFile = FileUtil.getFile(EcgMonitorDevice.ECGFILEDIR, wxFile.getName());

                        for(EcgFile file : fileList) {
                            if(file.getFile().getName().equalsIgnoreCase(toFile.getName())) {
                                fileList.remove(file);
                            }
                        }

                        if(toFile.exists()) toFile.delete();

                        FileUtil.moveFile(wxFile, toFile);
                        tmpFile = EcgFile.openBmeFile(toFile.getCanonicalPath());

                        fileList.add(tmpFile);
                        tmpFile.close();

                        fileAdapter.setSelectItem(fileList.size()-1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (FileException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        btnOpenFile = findViewById(R.id.btn_ecgfile_open);
        btnOpenFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fileAdapter.getSelectItem() == -1) return;

                String fileName = fileList.get(fileAdapter.getSelectItem()).getFileName();
                Intent intent = new Intent(EcgFileExplorerActivity.this, EcgFileReplayActivity.class);
                intent.putExtra("fileName", fileName);
                startActivity(intent);
            }
        });


        if(fileAdapter.getItemCount() >= 1) {
            rvFileList.smoothScrollToPosition(fileAdapter.getItemCount() - 1);
            fileAdapter.setSelectItem(fileAdapter.getItemCount() - 1);
        }

    }

    public void deleteFile(EcgFile file) {
        deselectFile();

        try {
            FileUtil.deleteFile(file.getFile());
            fileList.remove(file);
            fileAdapter.notifyDataSetChanged();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 用微信分享BME文件
    private void shareBmeFileThroughWechat() {
        if(fileAdapter.getSelectItem() == -1) return;

        EcgFile selectedFile = fileList.get(fileAdapter.getSelectItem());

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
        tvCreatedTime.setText("");
        tvMacAddress.setText("");
        commentList.clear();
        reportAdapter.notifyDataSetChanged();
    }

    public void selectFile(EcgFile file) {
        deselectFile();

        tvCreatedTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(file.getEcgFileHead().getFileCreatedTime()));
        tvMacAddress.setText(file.getEcgFileHead().getMacAddress());
        commentList.clear();
        commentList.addAll(file.getEcgFileHead().getCommentList());
        reportAdapter.notifyDataSetChanged();
        if(reportAdapter.getItemCount() >= 1)
            rvReportList.smoothScrollToPosition(reportAdapter.getItemCount()-1);
    }

    private List<EcgFile> createEcgFileList(File[] files) {
        List<EcgFile> ecgFileList = new ArrayList<>();
        for(File file : files) {
            try {
                EcgFile ecgFile = EcgFile.openBmeFile(file.getCanonicalPath());
                ecgFileList.add(ecgFile);
                ecgFile.close();
            } catch (FileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ecgFileList;
    }

}
