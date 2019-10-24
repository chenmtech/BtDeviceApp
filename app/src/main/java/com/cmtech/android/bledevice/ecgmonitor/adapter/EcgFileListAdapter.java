package com.cmtech.android.bledevice.ecgmonitor.adapter;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgFileExploreActivity;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgFileRollWaveView;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgHrHistogramChart;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgHrLineChart;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.model.UserManager;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.bmefile.BmeFileHead30;
import com.vise.log.ViseLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
  *
  * ClassName:      EcgFileListAdapter
  * Description:    Ecg文件列表Adapter
  * Author:         chenm
  * CreateDate:     2018/11/10 下午4:09
  * UpdateUser:     chenm
  * UpdateDate:     2018/11/10 下午4:09
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EcgFileListAdapter extends RecyclerView.Adapter<EcgFileListAdapter.ViewHolder>{
    private final EcgFileExploreActivity activity;
    private List<EcgFile> fileList = new ArrayList<>();
    private List<File> updatedFileList = new ArrayList<>();
    private EcgFile selectedFile;
    private Drawable defaultBackground; // 缺省背景

    private static final float DEFAULT_SECOND_PER_GRID = 0.04f; // 缺省横向每个栅格代表的秒数，对应于走纸速度
    private static final float DEFAULT_MV_PER_GRID = 0.1f; // 缺省纵向每个栅格代表的mV，对应于灵敏度
    private static final int DEFAULT_PIXEL_PER_GRID = 10; // 缺省每个栅格包含的像素个数

    public class ViewHolder extends RecyclerView.ViewHolder {
        View fileView;
        TextView tvModifyTime;
        TextView tvCreator; // 创建人
        TextView tvCreateTime; // 创建时间
        TextView tvLength; // 信号长度
        TextView tvHrNum; // 心率次数
        View vIsUpdate; // 是否已更新
        LinearLayout expandLayout;

        public LinearLayout signalLayout;
        public EcgFileRollWaveView signalView; // signalView
        public TextView tvTotalTime; // 总时长
        public TextView tvCurrentTime; // 当前播放信号的时刻
        public SeekBar sbReplay; // 播放条
        public ImageButton btnSwitchReplayState; // 转换回放状态
        public EcgCommentAdapter commentAdapter; // 留言Adapter
        public RecyclerView rvComments; // 留言RecycleView

        public LinearLayout hrLayout;
        public TextView tvAverageHr; // 平均心率
        public TextView tvMaxHr; // 最大心率
        public EcgHrLineChart hrLineChart; // 心率折线图
        public EcgHrHistogramChart hrHistChart; // 心率直方图

        ViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            tvModifyTime = fileView.findViewById(R.id.tv_modify_time);
            tvCreator = fileView.findViewById(R.id.ecgfile_creator);
            tvCreateTime = fileView.findViewById(R.id.ecgfile_createtime);
            tvLength = fileView.findViewById(R.id.ecgfile_length);
            tvHrNum = fileView.findViewById(R.id.ecgfile_hr_num);
            vIsUpdate = fileView.findViewById(R.id.ecgfile_update);
            expandLayout = fileView.findViewById(R.id.layout_ecgfile_expand_part);

            signalLayout = fileView.findViewById(R.id.layout_ecgfile_signal);
            signalView = fileView.findViewById(R.id.rwv_ecgview);
            signalView.setListener(activity);

            rvComments = fileView.findViewById(R.id.rv_ecgcomment_list);
            LinearLayoutManager commentLayoutManager = new LinearLayoutManager(activity);
            commentLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            rvComments.setLayoutManager(commentLayoutManager);
            rvComments.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
            commentAdapter = new EcgCommentAdapter(null, activity);
            rvComments.setAdapter(commentAdapter);

            tvCurrentTime = fileView.findViewById(R.id.tv_current_time);
            tvTotalTime = fileView.findViewById(R.id.tv_total_time);

            btnSwitchReplayState = fileView.findViewById(R.id.ib_ecgreplay_startandstop);
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

            sbReplay = fileView.findViewById(R.id.sb_ecgfile);
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

            hrLayout = fileView.findViewById(R.id.layout_ecgfile_hr);
            hrHistChart = fileView.findViewById(R.id.chart_hr_histogram);
            hrLineChart = fileView.findViewById(R.id.linechart_hr);
            tvAverageHr = fileView.findViewById(R.id.tv_average_hr_value);
            tvMaxHr = fileView.findViewById(R.id.tv_max_hr_value);
        }
    }

    public EcgFileListAdapter(EcgFileExploreActivity activity) {
        this.activity = activity;
    }

    public EcgFileListAdapter(EcgFileExploreActivity activity, List<EcgFile> fileList, List<File> updatedFileList) {
        this.activity = activity;
        this.fileList = fileList;
        this.updatedFileList = updatedFileList;
    }

    @NonNull
    @Override
    public EcgFileListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecg_file, parent, false);

        final EcgFileListAdapter.ViewHolder holder = new EcgFileListAdapter.ViewHolder(view);
        defaultBackground = holder.fileView.getBackground();
        holder.fileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.selectFile(fileList.get(holder.getAdapterPosition()));
            }
        });
        holder.tvCreator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EcgFile file = fileList.get(holder.getAdapterPosition());
                User creator = file.getCreator();
                Toast.makeText(MyApplication.getContext(), creator.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EcgFileListAdapter.ViewHolder holder, final int position) {
        EcgFile file = fileList.get(position);
        if(file == null) return;

        holder.tvModifyTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterday(file.getFile().lastModified()));

        User fileCreator = file.getCreator();
        User account = UserManager.getInstance().getUser();
        if(fileCreator.equals(account)) {
            holder.tvCreator.setText(Html.fromHtml("<u>您本人</u>"));
        } else {
            holder.tvCreator.setText(Html.fromHtml("<u>" + file.getCreatorName() + "</u>"));
        }

        String createdTime = DateTimeUtil.timeToShortStringWithTodayYesterday(file.getCreatedTime());
        holder.tvCreateTime.setText(createdTime);

        if(file.getDataNum() == 0) {
            holder.tvLength.setText("无");
        } else {
            String dataTimeLength = DateTimeUtil.secToTimeInChinese(file.getDataNum() / file.getSampleRate());
            holder.tvLength.setText(dataTimeLength);
        }

        int hrNum = file.getHrList().size();
        holder.tvHrNum.setText(String.valueOf(hrNum));

        if(file.equals(selectedFile)) {
            int bgdColor = ContextCompat.getColor(MyApplication.getContext(), R.color.secondary);
            holder.fileView.setBackgroundColor(bgdColor);
            activity.updateSelectedComponent(holder);
            holder.expandLayout.setVisibility(View.VISIBLE);

            if(selectedFile != null) {
                ViseLog.e("The selected file is: " + selectedFile.getFileName());

                int pixelPerGrid = DEFAULT_PIXEL_PER_GRID;
                int value1mV = ((BmeFileHead30)selectedFile.getBmeFileHead()).getCalibrationValue();
                int hPixelPerData = Math.round(pixelPerGrid / (DEFAULT_SECOND_PER_GRID * selectedFile.getSampleRate())); // 计算横向分辨率
                float vValuePerPixel = value1mV * DEFAULT_MV_PER_GRID / pixelPerGrid; // 计算纵向分辨率
                holder.signalView.stopShow();
                holder.signalView.setRes(hPixelPerData, vValuePerPixel);
                holder.signalView.setGridWidth(pixelPerGrid);
                holder.signalView.setZeroLocation(0.5);
                holder.signalView.clearData();
                holder.signalView.initView();
                holder.signalView.setEcgFile(selectedFile);
                int secondInSignal = selectedFile.getDataNum()/ selectedFile.getSampleRate();
                holder.tvCurrentTime.setText(DateTimeUtil.secToTime(0));
                holder.tvTotalTime.setText(DateTimeUtil.secToTime(secondInSignal));
                holder.sbReplay.setMax(secondInSignal);

                List<EcgNormalComment> commentList = getCommentListInFile(selectedFile);
                holder.commentAdapter.updateCommentList(commentList);
                if(commentList.size() > 0)
                    holder.rvComments.smoothScrollToPosition(0);

                holder.signalView.startShow();

                if(selectedFile.getDataNum() == 0) {
                    holder.signalLayout.setVisibility(View.GONE);
                } else {
                    holder.signalLayout.setVisibility(View.VISIBLE);
                }

                if(selectedFile.getHrList().isEmpty()) {
                    holder.hrLayout.setVisibility(View.GONE);
                } else {
                    holder.hrLayout.setVisibility(View.VISIBLE);
                }

                activity.updateSelectedFileHrStatisticsInfo();
            } else {
                if(holder.signalView != null)
                    holder.signalView.stopShow();
                //signalLayout.setVisibility(View.GONE);
                //hrLayout.setVisibility(View.GONE);
            }
        } else {
            holder.fileView.setBackground(defaultBackground);
            holder.expandLayout.setVisibility(View.GONE);
        }

        if (updatedFileList.contains(file.getFile())) {
            holder.vIsUpdate.setVisibility(View.VISIBLE);
        } else {
            holder.vIsUpdate.setVisibility(View.GONE);
        }
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
    public int getItemCount() {
        return fileList.size();
    }

    public void updateFileList(List<EcgFile> fileList, List<File> updatedFileList) {
        this.fileList = fileList;
        this.updatedFileList = updatedFileList;
        notifyDataSetChanged();
    }

    public void updateSelectedFile(EcgFile selectFile) {
        this.selectedFile = selectFile;
        notifyDataSetChanged();
    }
}
