package com.cmtech.android.bledevice.ecgmonitor.controller;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgComment;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.bmefile.BmeFileHead30;

import java.util.List;

public class EcgFileAdapter extends RecyclerView.Adapter<EcgFileAdapter.ViewHolder> {
    private EcgFileExplorerActivity activity;

    private List<EcgFile> fileList;

    private int selectItem = -1;

    private Drawable defaultBackground;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View fileView;
        TextView fileCreatedPerson;
        TextView fileCreatedTime;
        TextView fileLastComment;
        ImageButton ibPlay;

        public ViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            fileCreatedPerson = fileView.findViewById(R.id.ecgfile_createperson);
            fileCreatedTime = fileView.findViewById(R.id.ecgfile_createtime);
            fileLastComment = fileView.findViewById(R.id.ecgfile_lastcomment);
            ibPlay = fileView.findViewById(R.id.ib_ecgfile_play);
        }
    }

    public EcgFileAdapter(List<EcgFile> fileList, EcgFileExplorerActivity activity) {
        this.fileList = fileList;
        this.activity = activity;
    }

    @Override
    public EcgFileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecgfile, parent, false);
        final EcgFileAdapter.ViewHolder holder = new EcgFileAdapter.ViewHolder(view);
        defaultBackground = holder.fileView.getBackground();

        holder.fileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.selectFile(holder.getAdapterPosition());
            }
        });

        holder.ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectItem == holder.getAdapterPosition()) {
                    activity.openSelectedFile();
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(EcgFileAdapter.ViewHolder holder, final int position) {
        EcgFile file = fileList.get(position);

        holder.fileCreatedPerson.setText(file.getEcgFileHead().getCreatedPerson());

        StringBuilder createTimeSb = new StringBuilder();
        createTimeSb.append(DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(((BmeFileHead30)file.getBmeFileHead()).getCreatedTime()));
        createTimeSb.append(" [");
        createTimeSb.append(DateTimeUtil.secToTime(file.getDataNum()/file.getFs()));
        createTimeSb.append(']');
        holder.fileCreatedTime.setText(createTimeSb.toString());

        int commentNum = file.getEcgFileHead().getCommentsNum();
        if(commentNum > 0) {
            EcgComment comment = file.getEcgFileHead().getCommentList().get(commentNum - 1);
            StringBuilder sb = new StringBuilder();
            sb.append(DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(comment.getCreatedTime()));
            sb.append(' ');
            sb.append(comment.getCommentator());
            sb.append("说“");
            sb.append(comment.getContent());
            sb.append('”');
            holder.fileLastComment.setText(sb.toString());
        } else {
            holder.fileLastComment.setText("无留言");
        }

        int bgdColor = 0;
        if(selectItem == position) {
            bgdColor = MyApplication.getContext().getColor(R.color.secondary);
            holder.fileView.setBackgroundColor(bgdColor);
            holder.ibPlay.setBackgroundColor(bgdColor);
            holder.ibPlay.setVisibility(View.VISIBLE);
        } else {
            holder.fileView.setBackground(defaultBackground);
            holder.ibPlay.setBackground(defaultBackground);
            holder.ibPlay.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public void updateFileList(List<EcgFile> fileList) {
        this.fileList = fileList;
    }

    public int getSelectItem() {
        return selectItem;
    }

    public void updateSelectItem(int selectItem) {
        if(selectItem >= 0 && selectItem < fileList.size())
            this.selectItem = selectItem;
        else
            this.selectItem = -1;
    }

}
