package com.cmtech.android.bledevice.ecgmonitor.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgCommentAdapter;
import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgFileAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgFileExplorerModel;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgHrHistogramChart;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgHrLineChart;
import com.cmtech.android.bledevice.ecgmonitor.model.IEcgAppendixOperator;
import com.cmtech.android.bledevice.ecgmonitor.model.IEcgFileExplorerListener;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrRecorder;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgFileRollWaveView;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import java.io.IOException;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECG_FILE_DIR;

/**
 * EcgFileExplorerActivity: 心电文件浏览Activity
 * Created by bme on 2018/11/10.
 */

public class EcgFileExplorerActivity extends AppCompatActivity implements IEcgFileExplorerListener, EcgFileRollWaveView.IEcgFileRollWaveViewListener, IEcgAppendixOperator {
    private static final String TAG = "EcgFileExplorerActivity";

    private EcgFileExplorerModel fileExploreModel;      // 文件浏览器模型实例

    private EcgFileRollWaveView signalView; // signalView

    private EcgFileAdapter fileAdapter; // 文件Adapter

    private RecyclerView rvFiles; // 文件RecycleView

    private EcgCommentAdapter commentAdapter; // 留言Adapter

    private RecyclerView rvComments; // 留言RecycleView

    private TextView tvTotalTime; // 总时长

    private TextView tvCurrentTime; // 当前播放的信号的时刻

    private SeekBar sbReplay; // 播放条

    private ImageButton btnSwitchReplayState; // 转换回放状态

    private EcgHrHistogramChart hrHistChart; // 心率直方图

    private EcgHrLineChart hrLineChart; // 心率折线图

    private TextView tvAverageHr; // 平均心率

    private TextView tvMaxHr; // 最大心率

    private LinearLayout signalLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgfile_explorer);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_ecgexplorer);
        setSupportActionBar(toolbar);

        if(ECG_FILE_DIR == null) {
            throw new IllegalStateException();
        }

        try {
            fileExploreModel = new EcgFileExplorerModel(ECG_FILE_DIR, this);
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }

        signalLayout = findViewById(R.id.ecgfile_ecgsignal_layout);

        rvFiles = findViewById(R.id.rv_ecgfile_list);
        LinearLayoutManager fileLayoutManager = new LinearLayoutManager(this);
        fileLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvFiles.setLayoutManager(fileLayoutManager);
        rvFiles.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        fileAdapter = new EcgFileAdapter(fileExploreModel);
        rvFiles.setAdapter(fileAdapter);

        rvComments = findViewById(R.id.rv_ecgcomment_list);
        LinearLayoutManager commentLayoutManager = new LinearLayoutManager(this);
        commentLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvComments.setLayoutManager(commentLayoutManager);
        rvComments.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        commentAdapter = new EcgCommentAdapter(fileExploreModel.getSelectFileCommentList(), this);
        rvComments.setAdapter(commentAdapter);

        signalView = findViewById(R.id.rwv_ecgview);
        signalView.setListener(this);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);

        btnSwitchReplayState = findViewById(R.id.ib_ecgreplay_startandstop);
        btnSwitchReplayState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(signalView.isReplaying()) {
                    stopReplay();
                } else {
                    startReplay();
                }
            }
        });

        sbReplay = findViewById(R.id.sb_ecgfile);

        sbReplay.setEnabled(false);
        sbReplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    signalView.showAtSecond(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        hrHistChart = findViewById(R.id.chart_hr_histogram);

        hrLineChart = findViewById(R.id.linechart_hr);

        tvAverageHr = findViewById(R.id.tv_average_hr_value);

        tvMaxHr = findViewById(R.id.tv_max_hr_value);


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

        fileExploreModel.removeListener();
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
        signalView.setRes(xRes, yRes);
        signalView.setGridWidth(viewGridWidth);
        signalView.setZeroLocation(zerolocation);
        signalView.clearData();
        signalView.initView();
    }

    private void startReplay() {
        signalView.startShow();
    }

    private void stopReplay() {
        signalView.stopShow();
    }


    @Override
    public void onUpdateEcgFileList() {
        fileAdapter.notifyDataSetChanged();

        if(fileExploreModel.getSelectFile() != null) {
            rvFiles.smoothScrollToPosition(fileExploreModel.getSelectIndex());
            EcgFile selectFile = fileExploreModel.getSelectFile();
            ViseLog.e(selectFile);

            signalView.setEcgFile(selectFile);
            initEcgView(fileExploreModel.gethPixelPerData(), fileExploreModel.getvValuePerPixel(), fileExploreModel.getPixelPerGrid(), 0.5);

            tvCurrentTime.setText(DateTimeUtil.secToTime(0));
            tvTotalTime.setText(DateTimeUtil.secToTime(fileExploreModel.getTotalSecond()));
            sbReplay.setMax(fileExploreModel.getTotalSecond());

            List<EcgNormalComment> commentList = fileExploreModel.getSelectFileCommentList();
            commentAdapter.setCommentList(commentList);
            commentAdapter.notifyDataSetChanged();
            if(commentList.size() > 0)
                rvComments.smoothScrollToPosition(0);

            startReplay();

            if(selectFile.getDataNum() == 0) {
                signalLayout.setVisibility(View.GONE);
            } else {
                signalLayout.setVisibility(View.VISIBLE);
            }

            fileExploreModel.updateHrInfo();
        }
    }

    @Override
    public void onUpdateEcgHrInfo(List<Integer> filteredHrList, List<EcgHrRecorder.HrHistogramElement<Float>> normHistogram, int maxHr, int averageHr) {
        hrHistChart.update(normHistogram);
        tvAverageHr.setText(String.valueOf(averageHr));
        tvMaxHr.setText(String.valueOf(maxHr));

        hrLineChart.showLineChart(filteredHrList, "心率时序图", Color.BLUE);
        Drawable drawable = getResources().getDrawable(R.drawable.hr_linechart_fade);
        hrLineChart.setChartFillDrawable(drawable);
    }

    /**
     * EcgFileRollWaveView.IEcgFileRollWaveViewObserver接口函数
     */
    @Override
    public void onUpdateShowState(boolean isReplay) {
        if(isReplay) {
            btnSwitchReplayState.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_ecg_pause_32px));
            sbReplay.setEnabled(false);
        } else {
            btnSwitchReplayState.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_ecg_play_32px));
            sbReplay.setEnabled(true);
        }
    }

    @Override
    public void onUpdateDataLocation(long dataLocation) {
        int second = (int)(dataLocation/fileExploreModel.getSelectFileSampleRate());
        tvCurrentTime.setText(String.valueOf(DateTimeUtil.secToTime(second)));
        sbReplay.setProgress(second);
    }

    /**
     * IEcgAppendixOperator接口函数
     */
    @Override
    public void deleteAppendix(final EcgNormalComment appendix) {
        if(signalView.isReplaying())
            stopReplay();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除一条Ecg附加信息");
        builder.setMessage("确定删除该Ecg附加信息吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //fileReplayModel.deleteComment(appendix);
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
    public void saveAppendix() {
        fileExploreModel.saveAppendix();
    }

}
