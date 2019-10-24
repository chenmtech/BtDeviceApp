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

public class EcgFileExploreActivity extends AppCompatActivity implements OpenedEcgFilesManager.OnOpenedEcgFilesListener, HrStatisticProcessor.OnHrStatisticInfoUpdatedListener, EcgFileRollWaveView.OnRollWaveViewListener, EcgCommentAdapter.OnEcgCommentListener  {
    private static final String TAG = "EcgFileExploreActivity";

    private static final float DEFAULT_SECOND_PER_GRID = 0.04f; // 缺省横向每个栅格代表的秒数，对应于走纸速度
    private static final float DEFAULT_MV_PER_GRID = 0.1f; // 缺省纵向每个栅格代表的mV，对应于灵敏度
    private static final int DEFAULT_PIXEL_PER_GRID = 10; // 缺省每个栅格包含的像素个数
    private static final int DEFAULT_LOADED_FILENUM_EACH_TIMES = 5; // 缺省每次加载的文件数

    private EcgFileExplorer explorer;      // 文件浏览器实例
    private EcgFileListAdapter fileAdapter; // 文件Adapter
    private RecyclerView rvFiles; // 文件RecycleView
    private TextView tvPromptInfo; // 提示信息

    public LinearLayout signalLayout;
    public EcgFileRollWaveView signalView; // signalView
    public EcgCommentAdapter commentAdapter; // 留言Adapter
    public RecyclerView rvComments; // 留言RecycleView
    public TextView tvTotalTime; // 总时长
    public TextView tvCurrentTime; // 当前播放信号的时刻
    public SeekBar sbReplay; // 播放条
    public ImageButton btnSwitchReplayState; // 转换回放状态

    public LinearLayout hrLayout;
    public TextView tvAverageHr; // 平均心率
    public TextView tvMaxHr; // 最大心率
    public EcgHrLineChart hrLineChart; // 心率折线图
    public EcgHrHistogramChart hrHistChart; // 心率直方图

    public void updateSelectedComponent(EcgFileListAdapter.ViewHolder holder) {
        signalLayout = holder.signalLayout;
        signalView = holder.signalView;
        commentAdapter = holder.commentAdapter;
        rvComments = holder.rvComments;
        tvTotalTime = holder.tvTotalTime;
        tvCurrentTime = holder.tvCurrentTime;
        sbReplay = holder.sbReplay;
        btnSwitchReplayState = holder.btnSwitchReplayState;
        hrLayout = holder.hrLayout;
        tvAverageHr = holder.tvAverageHr;
        tvMaxHr = holder.tvMaxHr;
        hrLineChart = holder.hrLineChart;
        hrHistChart = holder.hrHistChart;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgfile_explorer);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_ecgfile_explorer);
        setSupportActionBar(toolbar);

        try {
            explorer = new EcgFileExplorer(ECG_FILE_DIR, EcgFileExplorer.FILE_ORDER_MODIFIED_TIME, this);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "心电记录目录错误。", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvFiles = findViewById(R.id.rv_ecgfile_list);
        LinearLayoutManager fileLayoutManager = new LinearLayoutManager(this);
        fileLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvFiles.setLayoutManager(fileLayoutManager);
        rvFiles.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        fileAdapter = new EcgFileListAdapter(this);
        rvFiles.setAdapter(fileAdapter);
        rvFiles.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //判断RecyclerView的状态 是空闲时，同时，是最后一个可见的ITEM时才加载
                if(newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem == fileAdapter.getItemCount()-1) {
                    explorer.loadNextFiles(DEFAULT_LOADED_FILENUM_EACH_TIMES);
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

        ImageButton ibUpdate = findViewById(R.id.ib_update_ecgfile_list);
        ibUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importFromWechat();
            }
        });

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

            case R.id.share_with_wechat:
                shareSelectedFileThroughWechat();
                break;

        }
        return true;
    }

    private void importFromWechat() {
        if(signalView != null) {
            signalView.stopShow();
        }
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
        if(signalView != null)
            signalView.stopShow();
        explorer.close();
    }

    public void selectFile(EcgFile ecgFile) {
        if(signalView != null)
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

                /*if(selectedFile != null) {
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
                    if(signalView != null)
                        signalView.stopShow();
                    //signalLayout.setVisibility(View.GONE);
                    //hrLayout.setVisibility(View.GONE);
                }*/
            }
        });
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

                fileAdapter.updateFileList(fileList, getUpdatedFiles());
            }
        });

    }

    @Override
    public void onHrStatisticInfoUpdated(EcgHrStatisticsInfo hrStatisticsInfo) {
        updateSelectedFileHrStatisticsInfo(hrStatisticsInfo);
    }

    public void updateSelectedFileHrStatisticsInfo() {
        EcgHrStatisticsInfo hrStatisticsInfo = explorer.getSelectedFileHrStatisticsInfo();
        tvAverageHr.setText(String.valueOf(hrStatisticsInfo.getAverageHr()));
        tvMaxHr.setText(String.valueOf(hrStatisticsInfo.getMaxHr()));
        hrLineChart.showLineChart(hrStatisticsInfo.getFilteredHrList(), "心率时序图", Color.BLUE);
        hrHistChart.update(hrStatisticsInfo.getNormHistogram(HR_HISTOGRAM_BAR_NUM));
    }

    private void updateSelectedFileHrStatisticsInfo(EcgHrStatisticsInfo hrStatisticsInfo) {
        tvAverageHr.setText(String.valueOf(hrStatisticsInfo.getAverageHr()));
        tvMaxHr.setText(String.valueOf(hrStatisticsInfo.getMaxHr()));
        hrLineChart.showLineChart(hrStatisticsInfo.getFilteredHrList(), "心率时序图", Color.BLUE);
        hrHistChart.update(hrStatisticsInfo.getNormHistogram(HR_HISTOGRAM_BAR_NUM));
    }

    @Override
    public void onShowStateUpdated(boolean isShow) {
        if(isShow) {
            btnSwitchReplayState.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_pause_32px));
        } else {
            btnSwitchReplayState.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_play_32px));
        }
        sbReplay.setEnabled(!isShow);
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
