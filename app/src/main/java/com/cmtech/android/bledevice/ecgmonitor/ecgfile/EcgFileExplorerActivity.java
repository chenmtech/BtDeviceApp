package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorDevice;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.FullyLinearLayoutManager;

public class EcgFileExplorerActivity extends AppCompatActivity implements IEcgFileExplorerObserver{
    private static final String TAG = "EcgFileExplorerActivity";

    private static EcgFileExplorerModel model;

    private EcgFileAdapter fileAdapter;
    private RecyclerView rvFileList;

    private EcgCommentAdapter reportAdapter;
    private RecyclerView rvReportList;

    // 工具条
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgfile_explorer);

        // 创建ToolBar
        toolbar = findViewById(R.id.tb_ecgfile_explorer);
        setSupportActionBar(toolbar);

        try {
            model = new EcgFileExplorerModel(EcgMonitorDevice.ECGFILEDIR);
        } catch (IllegalArgumentException e) {
            finish();
        }
        model.registerEcgFileExplorerObserver(this);

        rvFileList = findViewById(R.id.rv_ecgfile_list);
        FullyLinearLayoutManager linearLayoutManager = new FullyLinearLayoutManager(this);
        rvFileList.setNestedScrollingEnabled(false);
        rvFileList.setLayoutManager(linearLayoutManager);
        rvFileList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        fileAdapter = new EcgFileAdapter(model.getFileList(), this);
        rvFileList.setAdapter(fileAdapter);
        fileAdapter.notifyDataSetChanged();
        if(fileAdapter.getItemCount() >= 1) {
            rvFileList.smoothScrollToPosition(fileAdapter.getItemCount() - 1);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ScrollView scrollView = findViewById(R.id.sv_ecgfile);
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            }, 200);
        }

        rvReportList = findViewById(R.id.rv_ecgreplay_comment);
        LinearLayoutManager reportLayoutManager = new LinearLayoutManager(this);
        rvReportList.setLayoutManager(reportLayoutManager);
        rvReportList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        reportAdapter = new EcgCommentAdapter(model.getFileCommentList(), null);
        rvReportList.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        if(reportAdapter.getItemCount() >= 1)
            rvReportList.smoothScrollToPosition(reportAdapter.getItemCount()-1);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ecgfileexplorer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.explorer_update:
                model.importFromWeixin();
                break;

            case R.id.explorer_delete:
                deleteSelectFile();
                break;

            case R.id.explorer_share:
                if(fileAdapter.getSelectItem() != -1) return false;

                model.shareSelectFileThroughWechat();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        model.removeEcgFileExplorerObserver();
    }

    public void openSelectFile() {
        if(fileAdapter.getSelectItem() == -1) return;

        model.openSelectFile();
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
