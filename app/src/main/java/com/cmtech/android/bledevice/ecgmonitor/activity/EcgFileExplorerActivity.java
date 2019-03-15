package com.cmtech.android.bledevice.ecgmonitor.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgAppendixAdapter;
import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgFileAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgFileExplorerModel;
import com.cmtech.android.bledevice.ecgmonitor.model.IEcgAppendixOperator;
import com.cmtech.android.bledevice.ecgmonitor.model.IEcgFileExplorerObserver;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgAppendix;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgFileRollWaveView;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import java.io.IOException;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECG_FILE_DIR;

/**
 * EcgFileExplorerActivity: 心电文件浏览Activity
 * Created by bme on 2018/11/10.
 */

public class EcgFileExplorerActivity extends AppCompatActivity implements IEcgFileExplorerObserver, EcgFileRollWaveView.IEcgFileRollWaveViewObserver, IEcgAppendixOperator {
    private static final String TAG = "EcgFileExplorerActivity";

    private static EcgFileExplorerModel fileExploreModel;      // 文件浏览器模型实例
    private EcgFileRollWaveView ecgView; // ecgView
    private EcgFileAdapter fileAdapter; // 文件列表Adapter
    private RecyclerView rvFileList; // 文件列表RecycleView
    private EcgAppendixAdapter appendixAdapter; // 附加信息列表Adapter
    private RecyclerView rvAppendixList; // 附加信息列表RecycleView
    private TextView tvTotalTime; // 总时长
    private TextView tvCurrentTime; // 当前播放的信号的时刻
    private SeekBar sbReplay; // 播放条
    private ImageButton btnSwitchReplayState; // 转换回放状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgfile_explorer);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_ecgfile_explorer);
        setSupportActionBar(toolbar);

        if(ECG_FILE_DIR == null) {
            throw new IllegalStateException();
        }

        try {
            fileExploreModel = new EcgFileExplorerModel(ECG_FILE_DIR);
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
        fileExploreModel.registerEcgFileExplorerObserver(this);

        rvFileList = findViewById(R.id.rv_ecgexplorer_file);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvFileList.setLayoutManager(linearLayoutManager);
        rvFileList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        fileAdapter = new EcgFileAdapter(fileExploreModel);
        rvFileList.setAdapter(fileAdapter);

        rvAppendixList = findViewById(R.id.rv_ecgexplorer_comment);
        LinearLayoutManager reportLayoutManager = new LinearLayoutManager(this);
        reportLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvAppendixList.setLayoutManager(reportLayoutManager);
        rvAppendixList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        appendixAdapter = new EcgAppendixAdapter(fileExploreModel.getSelectFileAppendixList(), this, fileExploreModel.getSelectFileSampleRate());
        rvAppendixList.setAdapter(appendixAdapter);

        ecgView = findViewById(R.id.rwv_ecgview);
        ecgView.registerEcgFileRollWaveViewObserver(this);
        tvCurrentTime = findViewById(R.id.tv_ecgreplay_currenttime);
        tvTotalTime = findViewById(R.id.tv_ecgreplay_totaltime);

        btnSwitchReplayState = findViewById(R.id.ib_ecgreplay_startandstop);
        btnSwitchReplayState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ecgView.isReplaying()) {
                    stopReplay();
                } else {
                    startReplay();
                }
            }
        });

        sbReplay = findViewById(R.id.sb_ecgreplay);

        sbReplay.setEnabled(false);
        sbReplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    ecgView.showAtSecond(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        if(!fileExploreModel.getFileList().isEmpty()) {
            fileExploreModel.select(fileExploreModel.getFileList().size()-1);
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

            case R.id.explorer_share:
                shareFileThroughWechat();
                break;

        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(fileExploreModel.getSelectFile() != null) {
            try {
                fileExploreModel.getSelectFile().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fileExploreModel.removeEcgFileExplorerObserver();
    }

    private void importFromWechat() {
        fileExploreModel.importFromWechat();
    }

    public void deleteSelectedFile() {
        if(fileExploreModel.getSelectFile() != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("删除Ecg信号");
            builder.setMessage("确定删除该Ecg信号吗？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    fileExploreModel.deleteSelectFile();
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

    private void shareFileThroughWechat() {
        fileExploreModel.shareSelectFileThroughWechat();
    }

    private void initEcgView(int xRes, float yRes, int viewGridWidth, double zerolocation) {
        ecgView.setRes(xRes, yRes);
        ecgView.setGridWidth(viewGridWidth);
        ecgView.setZeroLocation(zerolocation);
        ecgView.clearData();
        ecgView.initView();
    }

    private void startReplay() {
        ecgView.startShow();
    }

    private void stopReplay() {
        ecgView.stopShow();
    }


    @Override
    public void updateEcgFileList() {
        fileAdapter.notifyDataSetChanged();

        if(fileExploreModel.getSelectFile() != null) {
            rvFileList.smoothScrollToPosition(fileExploreModel.getSelectIndex());
            EcgFile selectFile = fileExploreModel.getSelectFile();
            ViseLog.e(selectFile);

            ecgView.setEcgFile(selectFile);
            initEcgView(fileExploreModel.getxPixelPerData(), fileExploreModel.getyValuePerPixel(), fileExploreModel.getPixelPerGrid(), 0.5);

            tvCurrentTime.setText("00:00:00");
            tvTotalTime.setText(DateTimeUtil.secToTime(fileExploreModel.getTotalSecond()));
            sbReplay.setMax(fileExploreModel.getTotalSecond());

            appendixAdapter.update(selectFile.getAppendixList(), selectFile.getFs());
            if(selectFile.getAppendixList().size() > 0)
                rvAppendixList.smoothScrollToPosition(0);

            startReplay();
        }
    }

    /**
     * EcgFileRollWaveView.IEcgFileRollWaveViewObserver接口函数
     */
    @Override
    public void updateShowState(boolean isReplay) {
        if(isReplay) {
            btnSwitchReplayState.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_ecg_pause_32px));
            sbReplay.setEnabled(false);
        } else {
            btnSwitchReplayState.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_ecg_play_32px));
            sbReplay.setEnabled(true);
        }
    }

    @Override
    public void updateDataLocation(long dataLocation) {
        int second = (int)(dataLocation/fileExploreModel.getSampleRate());
        tvCurrentTime.setText(String.valueOf(DateTimeUtil.secToTime(second)));
        sbReplay.setProgress(second);
        fileExploreModel.setDataLocation(dataLocation);
    }

    /**
     * IEcgAppendixOperator接口函数
     */
    @Override
    public void deleteAppendix(final EcgAppendix appendix) {
        if(ecgView.isReplaying())
            stopReplay();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除一条Ecg附加信息");
        builder.setMessage("确定删除该Ecg附加信息吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //fileReplayModel.deleteAppendix(appendix);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    @Override
    public void saveAppendix(EcgAppendix appendix) {
        fileExploreModel.saveAppendix(appendix);
    }

}
