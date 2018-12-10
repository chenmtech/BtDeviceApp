package com.cmtech.android.bledevice.ecgmonitor.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgCommentAdapter;
import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgFileAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgFileExplorerModel;
import com.cmtech.android.bledevice.ecgmonitor.model.IEcgFileExplorerObserver;
import com.cmtech.android.bledeviceapp.R;

import java.io.IOException;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECGFILEDIR;



public class EcgFileExplorerActivity extends AppCompatActivity implements IEcgFileExplorerObserver {
    private static final String TAG = "EcgFileExplorerActivity";

    private static EcgFileExplorerModel model;      // 文件浏览模型实例

    // 文件列表
    private EcgFileAdapter fileAdapter;
    private RecyclerView rvFileList;

    // 留言列表
    private EcgCommentAdapter reportAdapter;
    private RecyclerView rvReportList;

    // 工具条
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgfile_explorer);

        // 创建ToolBar
        toolbar = findViewById(R.id.tb_ecgexplorer);
        setSupportActionBar(toolbar);

        try {
            if(ECGFILEDIR != null)
                model = new EcgFileExplorerModel(ECGFILEDIR);
        } catch (IllegalArgumentException e) {
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
        model.registerEcgFileExplorerObserver(this);

        rvFileList = findViewById(R.id.rv_ecgexplorer_file);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvFileList.setLayoutManager(linearLayoutManager);
        rvFileList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        fileAdapter = new EcgFileAdapter(model);
        rvFileList.setAdapter(fileAdapter);

        rvReportList = findViewById(R.id.rv_ecgexplorer_comment);
        LinearLayoutManager reportLayoutManager = new LinearLayoutManager(this);
        rvReportList.setLayoutManager(reportLayoutManager);
        rvReportList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        reportAdapter = new EcgCommentAdapter(model.getFileCommentList(), null);
        rvReportList.setAdapter(reportAdapter);

        if(!model.getFileList().isEmpty()) {
            model.select(model.getFileList().size()-1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                // 回放心电信号返回
                if(resultCode == RESULT_OK) {
                    boolean updated = data.getBooleanExtra("updated", false);
                    if(updated) model.reloadSelectedFile();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ecgexplorer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.explorer_update:
                importFromWechat();
                break;

            case R.id.explorer_delete:
                deleteSelectedFile();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        model.removeEcgFileExplorerObserver();
    }

    private void importFromWechat() {
        model.importFromWechat();
    }

    public void deleteSelectedFile() {
        if(model.getSelectIndex() >= 0 && model.getSelectIndex() < model.getFileList().size()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("删除Ecg信号");
            builder.setMessage("确定删除该Ecg信号吗？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    model.deleteSelectedFile();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
        }
    }

    @Override
    public void update() {
        fileAdapter.notifyDataSetChanged();
        if(model.getSelectIndex() >= 0 && model.getSelectIndex() < model.getFileList().size())
            rvFileList.smoothScrollToPosition(model.getSelectIndex());

        reportAdapter.updateCommentList(model.getFileCommentList());
        if(model.getFileCommentList().size() > 0)
            rvReportList.smoothScrollToPosition(model.getFileCommentList().size()-1);
    }

    @Override
    public void replay(String fileName) {
        Intent intent = new Intent(EcgFileExplorerActivity.this, EcgFileReplayActivity.class);
        intent.putExtra("fileName", fileName);
        startActivityForResult(intent, 1);
    }
}
