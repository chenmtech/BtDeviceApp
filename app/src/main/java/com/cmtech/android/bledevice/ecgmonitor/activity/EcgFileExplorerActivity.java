package com.cmtech.android.bledevice.ecgmonitor.activity;

import android.content.DialogInterface;
import android.graphics.Color;
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
import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgFileListAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgFileExplorerModel;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgHrHistogramChart;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgHrLineChart;
import com.cmtech.android.bledevice.ecgmonitor.model.OnEcgCommentOperateListener;
import com.cmtech.android.bledevice.ecgmonitor.model.OnEcgFileExploreListener;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrRecorder;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgFileRollWaveView;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.bmefile.BmeFileHead30;
import com.vise.log.ViseLog;

import java.io.IOException;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECG_FILE_DIR;

/**
  *
  * ClassName:      EcgFileExplorerActivity
  * Description:    Ecg文件浏览Activity
  * Author:         chenm
  * CreateDate:     2018/11/10 下午5:34
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/12 下午5:34
  * UpdateRemark:   制作类图，优化代码
  * Version:        1.0
 */

public class EcgFileExplorerActivity extends AppCompatActivity implements OnEcgFileExploreListener, EcgFileRollWaveView.OnEcgFileRollWaveViewListener, OnEcgCommentOperateListener {
    private static final String TAG = "EcgFileExplorerActivity";

    private static final float DEFAULT_SECOND_PER_HGRID = 0.04f; // 缺省横向每个栅格代表的秒数，对应于走纸速度
    private static final float DEFAULT_MV_PER_VGRID = 0.1f; // 缺省纵向每个栅格代表的mV，对应于灵敏度
    private static final int DEFAULT_PIXEL_PER_GRID = 10; // 缺省每个栅格包含的像素个数

    private int secondInSignal; // 信号总的秒数

    private int sampleRate;

    private EcgFileExplorerModel model;      // 文件浏览器模型实例

    private EcgFileRollWaveView signalView; // signalView

    private EcgFileListAdapter fileAdapter; // 文件Adapter

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

    private LinearLayout hrLayout;

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
            model = new EcgFileExplorerModel(ECG_FILE_DIR, this);
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }

        signalLayout = findViewById(R.id.ecgfile_ecgsignal_layout);

        hrLayout = findViewById(R.id.ecgfile_ecghr_layout);

        rvFiles = findViewById(R.id.rv_ecgfile_list);
        LinearLayoutManager fileLayoutManager = new LinearLayoutManager(this);
        fileLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvFiles.setLayoutManager(fileLayoutManager);
        rvFiles.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        fileAdapter = new EcgFileListAdapter(model);
        rvFiles.setAdapter(fileAdapter);

        rvComments = findViewById(R.id.rv_ecgcomment_list);
        LinearLayoutManager commentLayoutManager = new LinearLayoutManager(this);
        commentLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvComments.setLayoutManager(commentLayoutManager);
        rvComments.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        commentAdapter = new EcgCommentAdapter(model.getSelectFileCommentList(), this);
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

        model.openAllFiles();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ecgexplorer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, null);
                finish();
                break;

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

        model.close();
    }

    private void importFromWechat() {
        model.importFromWechat();
    }

    private void deleteSelectedFile() {
        model.deleteSelectFile();
    }

    private void shareFileThroughWechat() {
        model.shareSelectFileThroughWechat();
    }



    private void startReplay() {
        signalView.startShow();
    }

    private void stopReplay() {
        signalView.stopShow();
    }


    @Override
    public void onSelectFileChanged(EcgFile selectFile) {
        fileAdapter.updateSelectFile(selectFile);

        if(selectFile != null) {
            ViseLog.e(selectFile);

            initForReplaySignal(selectFile);
            signalView.setEcgFile(selectFile);

            tvCurrentTime.setText(DateTimeUtil.secToTime(0));
            tvTotalTime.setText(DateTimeUtil.secToTime(secondInSignal));
            sbReplay.setMax(secondInSignal);

            List<EcgNormalComment> commentList = model.getSelectFileCommentList();
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

            if(selectFile.getHrList().size() == 0) {
                hrLayout.setVisibility(View.GONE);
            } else {
                hrLayout.setVisibility(View.VISIBLE);
            }

            model.updateHrInfo();
        } else {
            signalView.stopShow();

            signalLayout.setVisibility(View.GONE);

            hrLayout.setVisibility(View.GONE);
        }

    }

    @Override
    public void onFileListChanged(List<EcgFile> fileList) {
        fileAdapter.updateFileList(fileList);
    }

    @Override
    public void onEcgHrInfoUpdated(List<Short> filteredHrList, List<EcgHrRecorder.HrHistogramElement<Float>> normHistogram, short maxHr, short averageHr) {
        hrHistChart.update(normHistogram);
        tvAverageHr.setText(String.valueOf(averageHr));
        tvMaxHr.setText(String.valueOf(maxHr));

        hrLineChart.showLineChart(filteredHrList, "心率时序图", Color.BLUE);
    }

    @Override
    public void onShowStateUpdated(boolean isReplay) {
        if(isReplay) {
            btnSwitchReplayState.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_ecg_pause_32px));
            sbReplay.setEnabled(false);
        } else {
            btnSwitchReplayState.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_ecg_play_32px));
            sbReplay.setEnabled(true);
        }
    }

    @Override
    public void onDataLocationUpdated(long dataLocation) {
        int second = (int)(dataLocation/sampleRate);

        tvCurrentTime.setText(String.valueOf(DateTimeUtil.secToTime(second)));

        sbReplay.setProgress(second);
    }


    @Override
    public void onCommentSaved() {
        model.saveAppendix();
    }

    @Override
    public void onCommentDeleted(final EcgNormalComment comment) {
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

    // 初始化回放参数
    private void initForReplaySignal(final EcgFile ecgFile) {
        if(ecgFile == null) return;

        sampleRate = ecgFile.getSampleRate();

        secondInSignal = ecgFile.getDataNum()/sampleRate;

        initEcgView(ecgFile, 0.5);
    }

    private void initEcgView(EcgFile ecgFile, double zeroLocation) {
        int pixelPerGrid = DEFAULT_PIXEL_PER_GRID;
        int value1mV = ((BmeFileHead30)ecgFile.getBmeFileHead()).getCalibrationValue();
        int hPixelPerData = Math.round(pixelPerGrid / (DEFAULT_SECOND_PER_HGRID * sampleRate)); // 计算横向分辨率
        float vValuePerPixel = value1mV * DEFAULT_MV_PER_VGRID / pixelPerGrid; // 计算纵向分辨率

        signalView.setRes(hPixelPerData, vValuePerPixel);
        signalView.setGridWidth(pixelPerGrid);
        signalView.setZeroLocation(zeroLocation);
        signalView.clearData();
        signalView.initView();
    }
}
