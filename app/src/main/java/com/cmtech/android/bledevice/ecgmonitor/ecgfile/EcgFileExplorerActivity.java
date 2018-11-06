package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorDevice;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;
import com.vise.log.ViseLog;

public class EcgFileExplorerActivity extends AppCompatActivity implements IEcgFileExplorerObserver{
    private static final String TAG = "EcgFileExplorerActivity";

    private static EcgFileExplorerModel model;

    private EcgFileAdapter fileAdapter;
    private RecyclerView rvFileList;

    private EcgReportAdapter reportAdapter;
    private RecyclerView rvReportList;

    private Button btnEcgShare;
    private Button btnEcgDelete;
    private Button btnImportFromWX;
    private Button btnOpenFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgfile_explorer);

        try {
            model = new EcgFileExplorerModel(EcgMonitorDevice.ECGFILEDIR);
        } catch (IllegalArgumentException e) {
            finish();
        }
        model.registerEcgFileExplorerObserver(this);

        rvFileList = findViewById(R.id.rv_ecgfile_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvFileList.setLayoutManager(layoutManager);
        rvFileList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        fileAdapter = new EcgFileAdapter(model.getFileList(), this);
        rvFileList.setAdapter(fileAdapter);
        fileAdapter.notifyDataSetChanged();
        if(fileAdapter.getItemCount() >= 1)
            rvFileList.smoothScrollToPosition(fileAdapter.getItemCount()-1);


        rvReportList = findViewById(R.id.rv_ecgfile_report);
        LinearLayoutManager reportLayoutManager = new LinearLayoutManager(this);
        rvReportList.setLayoutManager(reportLayoutManager);
        rvReportList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        reportAdapter = new EcgReportAdapter(model.getFileCommentList());
        rvReportList.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        if(reportAdapter.getItemCount() >= 1)
            rvReportList.smoothScrollToPosition(reportAdapter.getItemCount()-1);

        btnImportFromWX = findViewById(R.id.btn_ecgfile_import);
        btnImportFromWX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model.importFromWeixin();
            }
        });

        btnEcgDelete = findViewById(R.id.btn_ecgfile_delete);
        btnEcgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSelectFile();
            }
        });

        btnEcgShare = findViewById(R.id.btn_ecgfile_share);
        btnEcgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fileAdapter.getSelectItem() == -1) return;

                model.shareSelectFileThroughWechat();
            }
        });

        btnOpenFile = findViewById(R.id.btn_ecgfile_open);
        btnOpenFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fileAdapter.getSelectItem() == -1) return;

                model.openSelectFile();
            }
        });

        if(model.getFileList().size() > 0) {
            selectFile(model.getFileList().size()-1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                // 登记设备返回
                if(resultCode == RESULT_OK) {
                    boolean updated = data.getBooleanExtra("updated", false);
                    if(updated) model.updateSelectFile();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        model.removeEcgFileExplorerObserver();
    }

    public void deleteSelectFile() {
        if(fileAdapter.getSelectItem() == -1) return;

        model.deleteSelectFile();
    }

    public void selectFile(int selectIndex) {
        model.selectFile(selectIndex);
    }


    @Override
    public void updateFileList() {
        fileAdapter.updateFileList(model.getFileList());
        updateSelectFile();
    }

    @Override
    public void updateSelectFile() {
        fileAdapter.updateSelectItem(model.getSelectIndex());
        fileAdapter.notifyDataSetChanged();
        if(model.getSelectIndex() >= 0 && model.getSelectIndex() < model.getFileList().size())
            rvFileList.smoothScrollToPosition(model.getSelectIndex());
        reportAdapter.updateCommentList(model.getFileCommentList());
        reportAdapter.notifyDataSetChanged();
        if(model.getFileCommentList().size() > 0)
            rvReportList.smoothScrollToPosition(model.getFileCommentList().size()-1);
    }

    @Override
    public void openFile(String fileName) {
        Intent intent = new Intent(EcgFileExplorerActivity.this, EcgFileReplayActivity.class);
        intent.putExtra("fileName", fileName);
        startActivityForResult(intent, 1);
    }
}
