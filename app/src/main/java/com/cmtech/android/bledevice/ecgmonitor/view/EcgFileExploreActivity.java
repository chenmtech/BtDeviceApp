package com.cmtech.android.bledevice.ecgmonitor.view;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;

import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgCommentAdapter;
import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgFileListAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgFileExplorer;
import com.cmtech.android.bledevice.ecgmonitor.model.OpenedEcgFilesManager;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.EcgHrStatisticsInfo;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.HrStatisticProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.model.UserManager;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.bmefile.BmeFileHead30;
import com.vise.log.ViseLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECG_FILE_DIR;
import static com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.EcgSignalProcessor.HR_HISTOGRAM_BAR_NUM;

/**
  *
  * ClassName:      EcgFileExploreActivity
  * Description:    Ecg文件浏览Activity
  * Author:         chenm
  * CreateDate:     2018/11/10 下午5:34
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/12 下午5:34
  * UpdateRemark:   制作类图，优化代码
  * Version:        1.0
 */

public class EcgFileExploreActivity extends AppCompatActivity implements OpenedEcgFilesManager.OnOpenedEcgFilesListener, HrStatisticProcessor.OnHrStatisticInfoUpdatedListener, EcgFileRollWaveView.OnEcgFileRollWaveViewListener, EcgCommentAdapter.OnEcgCommentListener  {
    private static final String TAG = "EcgFileExploreActivity";

    private static final float DEFAULT_SECOND_PER_GRID = 0.04f; // 缺省横向每个栅格代表的秒数，对应于走纸速度
    private static final float DEFAULT_MV_PER_GRID = 0.1f; // 缺省纵向每个栅格代表的mV，对应于灵敏度
    private static final int DEFAULT_PIXEL_PER_GRID = 10; // 缺省每个栅格包含的像素个数
    private static final int DEFAULT_LOADED_FILENUM_EACH_TIMES = 5; // 缺省每次加载的文件数

    private EcgFileExplorer explorer;      // 文件浏览器实例
    private EcgFileRollWaveView signalView; // signalView
    private EcgFileListAdapter fileAdapter; // 文件Adapter
    private RecyclerView rvFiles; // 文件RecycleView
    private EcgCommentAdapter commentAdapter; // 留言Adapter
    private RecyclerView rvComments; // 留言RecycleView
    private TextView tvTotalTime; // 总时长
    private TextView tvCurrentTime; // 当前播放信号的时刻
    private SeekBar sbReplay; // 播放条
    private ImageButton btnSwitchReplayState; // 转换回放状态
    private EcgHrHistogramChart hrHistChart; // 心率直方图
    private EcgHrLineChart hrLineChart; // 心率折线图
    private TextView tvAverageHr; // 平均心率
    private TextView tvMaxHr; // 最大心率
    private LinearLayout signalLayout;
    private LinearLayout hrLayout;
    private TextView tvPromptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgfile_explorer);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_ecgfile_explorer);
        setSupportActionBar(toolbar);

        try {
            explorer = new EcgFileExplorer(ECG_FILE_DIR, this);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "心电记录目录错误。", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        signalLayout = findViewById(R.id.layout_ecgfile_ecgsignal);
        hrLayout = findViewById(R.id.layout_ecgfile_hr);

        rvFiles = findViewById(R.id.rv_ecgfile_list);
        LinearLayoutManager fileLayoutManager = new LinearLayoutManager(this);
        fileLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvFiles.setLayoutManager(fileLayoutManager);
        rvFiles.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        fileAdapter = new EcgFileListAdapter(this);
        rvFiles.setAdapter(fileAdapter);
        rvFiles.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //判断RecyclerView的状态 是空闲时，同时，是最后一个可见的ITEM时才加载
                if(newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem == fileAdapter.getItemCount()-1) {
                    if(explorer.loadNextFiles(DEFAULT_LOADED_FILENUM_EACH_TIMES) == 0) {
                        Toast.makeText(EcgFileExploreActivity.this, "无信号可载入。", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if(layoutManager != null)
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            }
        });

        rvComments = findViewById(R.id.rv_ecgcomment_list);
        LinearLayoutManager commentLayoutManager = new LinearLayoutManager(this);
        commentLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvComments.setLayoutManager(commentLayoutManager);
        rvComments.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        commentAdapter = new EcgCommentAdapter(null, this);
        rvComments.setAdapter(commentAdapter);

        signalView = findViewById(R.id.rwv_ecgview);
        signalView.setOnEcgFileRollWaveViewListener(this);

        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);

        btnSwitchReplayState = findViewById(R.id.ib_ecgreplay_startandstop);
        btnSwitchReplayState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(signalView.isStart()) {
                    signalView.stopShow();
                } else {
                    signalView.startShow();
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

        ImageButton ibUpdate = findViewById(R.id.ib_update_ecgfiles);
        ibUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importFromWechat();
            }
        });

        hrHistChart = findViewById(R.id.chart_hr_histogram);
        hrLineChart = findViewById(R.id.linechart_hr);
        tvAverageHr = findViewById(R.id.tv_average_hr_value);
        tvMaxHr = findViewById(R.id.tv_max_hr_value);

        tvPromptInfo = findViewById(R.id.tv_prompt_info);
        tvPromptInfo.setText("正在载入信号");
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(explorer.loadNextFiles(DEFAULT_LOADED_FILENUM_EACH_TIMES) == 0) {
                    tvPromptInfo.setText("无信号可载入。");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ecgfile_explore, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, null);
                finish();
                break;

            case R.id.explorer_delete:
                deleteSelectedFile();
                break;

            case R.id.explorer_share:
                shareSelectedFileThroughWechat();
                break;

        }
        return true;
    }

    private void importFromWechat() {
        signalView.stopShow();
        explorer.importFromWechat();
        tvPromptInfo.setText("正在载入信号");
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(explorer.loadNextFiles(DEFAULT_LOADED_FILENUM_EACH_TIMES) == 0) {
                    tvPromptInfo.setText("无信号可载入。");
                }
            }
        });
    }

    private void deleteSelectedFile() {
        explorer.deleteSelectFile(this);
    }

    private void shareSelectedFileThroughWechat() {
        explorer.shareSelectedFileThroughWechat(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        signalView.stopShow();
        explorer.close();
    }

    public void selectFile(EcgFile ecgFile) {
        signalView.stopShow();
        explorer.selectFile(ecgFile);
    }

    public List<File> getUpdatedFiles() {
        return explorer.getUpdatedFiles();
    }

    @Override
    public void onFileSelected(final EcgFile selectedFile) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fileAdapter.updateSelectedFile(selectedFile);

                if(selectedFile != null) {
                    ViseLog.e("The selected file is: " + selectedFile.getFileName());

                    initEcgView(selectedFile, 0.5);
                    int secondInSignal = selectedFile.getDataNum()/ selectedFile.getSampleRate();
                    tvCurrentTime.setText(DateTimeUtil.secToTime(0));
                    tvTotalTime.setText(DateTimeUtil.secToTime(secondInSignal));
                    sbReplay.setMax(secondInSignal);

                    List<EcgNormalComment> commentList = getCommentListInFile(selectedFile);
                    commentAdapter.updateCommentList(commentList);
                    if(commentList.size() > 0)
                        rvComments.smoothScrollToPosition(0);

                    signalView.startShow();

                    if(selectedFile.getDataNum() == 0) {
                        signalLayout.setVisibility(View.GONE);
                    } else {
                        signalLayout.setVisibility(View.VISIBLE);
                    }

                    if(selectedFile.getHrList().isEmpty()) {
                        hrLayout.setVisibility(View.GONE);
                    } else {
                        hrLayout.setVisibility(View.VISIBLE);
                    }

                    updateSelectedFileHrStatisticsInfo(explorer.getSelectedFileHrStatisticsInfo());
                } else {
                    signalView.stopShow();
                    signalLayout.setVisibility(View.GONE);
                    hrLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initEcgView(EcgFile ecgFile, double zeroLocation) {
        if(ecgFile == null) return;
        int pixelPerGrid = DEFAULT_PIXEL_PER_GRID;
        int value1mV = ((BmeFileHead30)ecgFile.getBmeFileHead()).getCalibrationValue();
        int hPixelPerData = Math.round(pixelPerGrid / (DEFAULT_SECOND_PER_GRID * ecgFile.getSampleRate())); // 计算横向分辨率
        float vValuePerPixel = value1mV * DEFAULT_MV_PER_GRID / pixelPerGrid; // 计算纵向分辨率
        signalView.stopShow();
        signalView.setRes(hPixelPerData, vValuePerPixel);
        signalView.setGridWidth(pixelPerGrid);
        signalView.setZeroLocation(zeroLocation);
        signalView.clearData();
        signalView.initView();
        signalView.setEcgFile(ecgFile);
    }

    // 获取选中文件的留言列表
    private List<EcgNormalComment> getCommentListInFile(EcgFile ecgFile) {
        if(ecgFile == null)
            return new ArrayList<>();
        else {
            User account = UserManager.getInstance().getUser();
            boolean found = false;
            for(EcgNormalComment comment : ecgFile.getCommentList()) {
                if(comment.getCreator().equals(account)) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                ecgFile.addComment(EcgNormalComment.createDefaultComment());
            }
            return ecgFile.getCommentList();
        }
    }

    @Override
    public void onFileListChanged(final List<EcgFile> fileList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(fileList == null || fileList.isEmpty()) {
                    rvFiles.setVisibility(View.INVISIBLE);
                    tvPromptInfo.setVisibility(View.VISIBLE);
                }else {
                    rvFiles.setVisibility(View.VISIBLE);
                    tvPromptInfo.setVisibility(View.INVISIBLE);
                }

                fileAdapter.updateFileList(fileList);
            }
        });

    }

    @Override
    public void onHrStatisticInfoUpdated(EcgHrStatisticsInfo hrStatisticsInfo) {
        updateSelectedFileHrStatisticsInfo(hrStatisticsInfo);
    }

    private void updateSelectedFileHrStatisticsInfo(EcgHrStatisticsInfo hrStatisticsInfo) {
        tvAverageHr.setText(String.valueOf(hrStatisticsInfo.getAverageHr()));
        tvMaxHr.setText(String.valueOf(hrStatisticsInfo.getMaxHr()));
        hrLineChart.showLineChart(hrStatisticsInfo.getFilteredHrList(), "心率时序图", Color.BLUE);
        hrHistChart.update(hrStatisticsInfo.getNormHistogram(HR_HISTOGRAM_BAR_NUM));
    }

    @Override
    public void onShowStateUpdated(boolean isReplay) {
        if(isReplay) {
            btnSwitchReplayState.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_pause_32px));
        } else {
            btnSwitchReplayState.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_play_32px));
        }
        sbReplay.setEnabled(!isReplay);
    }

    @Override
    public void onDataLocationUpdated(long dataLocation, int sampleRate) {
        int second = (int)(dataLocation/ sampleRate);
        tvCurrentTime.setText(String.valueOf(DateTimeUtil.secToTime(second)));
        sbReplay.setProgress(second);
    }

    @Override
    public void onSelectedCommentSaved() {
        explorer.saveSelectedFileComment();
    }

    @Override
    public void onCommentDeleted(final EcgNormalComment comment) {
        if(signalView.isStart())
            signalView.stopShow();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除留言").setMessage("确定删除该留言吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //fileReplayModel.deleteComment(appendix);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }

}
