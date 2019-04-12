package com.cmtech.android.bledevice.ecgmonitor.adapter;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgFileExplorerModel;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.List;

public class EcgFileListAdapter extends RecyclerView.Adapter<EcgFileListAdapter.ViewHolder> {
    private EcgFileExplorerModel explorerModel; // 浏览器模型

    private List<EcgFile> fileList;

    private EcgFile selectFile;

    private Drawable defaultBackground; // 缺省背景

    static class ViewHolder extends RecyclerView.ViewHolder {
        View fileView;
        TextView tvCreator; // 创建人
        TextView tvCreateTime; // 创建时间
        TextView tvLength; // 信号长度
        TextView tvHrNum; // 心率次数

        ViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            tvCreator = fileView.findViewById(R.id.ecgfile_creator);
            tvCreateTime = fileView.findViewById(R.id.ecgfile_createtime);
            tvLength = fileView.findViewById(R.id.ecgfile_length);
            tvHrNum = fileView.findViewById(R.id.ecgfile_hr_num);
        }
    }

    public EcgFileListAdapter(EcgFileExplorerModel explorerModel) {
        this.explorerModel = explorerModel;
        this.fileList = new ArrayList<>();
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
                explorerModel.select(fileList.get(holder.getAdapterPosition()));
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
    public void onBindViewHolder(EcgFileListAdapter.ViewHolder holder, final int position) {
        EcgFile file = fileList.get(position);

        if(file == null) return;

        User fileCreator = file.getCreator();

        User account = AccountManager.getInstance().getAccount();
        if(fileCreator.equals(account)) {
            holder.tvCreator.setText(Html.fromHtml("<u>您本人</u>"));
        } else {
            holder.tvCreator.setText(Html.fromHtml("<u>" + file.getCreatorName() + "</u>"));
        }

        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(file.getCreateTime());
        holder.tvCreateTime.setText(createTime);

        if(file.getDataNum() == 0) {
            holder.tvLength.setText("无");
        } else {
            String fileTimeLength = DateTimeUtil.secToTimeInChinese(file.getDataNum() / file.getSampleRate());
            holder.tvLength.setText(fileTimeLength);
        }

        int hrNum = file.getHrList().size();
        holder.tvHrNum.setText(String.valueOf(hrNum));

        int bgdColor;
        if(file.equals(selectFile)) {
            bgdColor = MyApplication.getContext().getResources().getColor(R.color.secondary);
            holder.fileView.setBackgroundColor(bgdColor);
        } else {
            holder.fileView.setBackground(defaultBackground);
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }


    public void updateFileList(List<EcgFile> fileList) {
        this.fileList = fileList;
        notifyDataSetChanged();
    }


    public void updateSelectFile(EcgFile selectFile) {
        this.selectFile = selectFile;
        notifyDataSetChanged();
    }

}
