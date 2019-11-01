package com.cmtech.android.bledevice.ecgmonitor.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgRecordExplorerActivity;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.model.UserManager;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import java.io.File;
import java.util.List;


/**
  *
  * ClassName:      EcgRecordListAdapter
  * Description:    Ecg记录列表Adapter
  * Author:         chenm
  * CreateDate:     2018/11/10 下午4:09
  * UpdateUser:     chenm
  * UpdateDate:     2018/11/10 下午4:09
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EcgRecordListAdapter extends RecyclerView.Adapter<EcgRecordListAdapter.ViewHolder>{
    private final EcgRecordExplorerActivity activity;
    private final List<EcgFile> ecgFileList;
    private final List<File> updatedFileList;
    private EcgFile selectedEcgFile;
    private Drawable defaultBackground; // 缺省背景

    class ViewHolder extends RecyclerView.ViewHolder {
        View fileView;

        TextView tvModifyTime; // 更新时间
        TextView tvCreator; // 创建人
        TextView tvCreateTime; // 创建时间
        TextView tvLength; // 信号长度
        TextView tvHrNum; // 心率次数

        ViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            tvModifyTime = fileView.findViewById(R.id.tv_modify_time);
            tvCreator = fileView.findViewById(R.id.tv_creator);
            tvCreateTime = fileView.findViewById(R.id.tv_create_time);
            tvLength = fileView.findViewById(R.id.tv_signal_length);
            tvHrNum = fileView.findViewById(R.id.tv_hr_num);
        }
    }

    public EcgRecordListAdapter(EcgRecordExplorerActivity activity, List<EcgFile> ecgFileList, List<File> updatedFileList, EcgFile selectedEcgFile) {
        this.activity = activity;
        this.ecgFileList = ecgFileList;
        this.updatedFileList = updatedFileList;
        this.selectedEcgFile = selectedEcgFile;
    }

    @NonNull
    @Override
    public EcgRecordListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecg_record, parent, false);

        final EcgRecordListAdapter.ViewHolder holder = new EcgRecordListAdapter.ViewHolder(view);

        defaultBackground = holder.fileView.getBackground();
        holder.fileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EcgFile newSelectFile = ecgFileList.get(holder.getAdapterPosition());
                activity.selectFile(newSelectFile);
            }
        });
        holder.tvCreator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EcgFile file = ecgFileList.get(holder.getAdapterPosition());
                User creator = file.getCreator();
                Toast.makeText(MyApplication.getContext(), creator.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EcgRecordListAdapter.ViewHolder holder, final int position) {
        ViseLog.e("onBindViewHolder " + position);
        EcgFile file = ecgFileList.get(position);
        if(file == null) return;

        holder.tvModifyTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterday(file.getFile().lastModified()));

        User fileCreator = file.getCreator();
        User account = UserManager.getInstance().getUser();
        if(fileCreator.equals(account)) {
            holder.tvCreator.setText(Html.fromHtml("<u>您本人</u>"));
        } else {
            holder.tvCreator.setText(Html.fromHtml("<u>" + file.getCreatorName() + "</u>"));
        }

        String createdTime = DateTimeUtil.timeToShortStringWithTodayYesterday(file.getCreateTime());
        holder.tvCreateTime.setText(createdTime);

        if(file.getDataNum() == 0) {
            holder.tvLength.setText("无");
        } else {
            String dataTimeLength = DateTimeUtil.secToTimeInChinese(file.getDataNum() / file.getSampleRate());
            holder.tvLength.setText(dataTimeLength);
        }

        int hrNum = file.getHrList().size();
        holder.tvHrNum.setText(String.valueOf(hrNum));

        if (updatedFileList.contains(file.getFile())) {
            holder.tvModifyTime.setTextColor(Color.RED);
        } else {
            holder.tvModifyTime.setTextColor(Color.BLACK);
        }

        if(file.equals(selectedEcgFile)) {
            int bgdColor = ContextCompat.getColor(MyApplication.getContext(), R.color.secondary);
            holder.fileView.setBackgroundColor(bgdColor);
        } else {
            holder.fileView.setBackground(defaultBackground);
        }
    }
    @Override
    public int getItemCount() {
        return ecgFileList.size();
    }

    public void updateFileList(List<EcgFile> fileList, List<File> updatedFileList) {
        notifyDataSetChanged();
    }

    public void updateSelectedFile(EcgFile selectFile) {
        int beforePos = ecgFileList.indexOf(this.selectedEcgFile);
        int afterPos = ecgFileList.indexOf(selectFile);

        if(beforePos != afterPos) {
            this.selectedEcgFile = selectFile;
            notifyItemChanged(beforePos);
            notifyItemChanged(afterPos);
        } else {
            notifyItemChanged(beforePos);
        }
    }

    public void insertNewFile(EcgFile file) {
        ViseLog.e("insert " + file);

        //notifyItemInserted(getItemCount()-1);
        notifyDataSetChanged();
    }

    public void clear() {
        ecgFileList.clear();
    }
}
