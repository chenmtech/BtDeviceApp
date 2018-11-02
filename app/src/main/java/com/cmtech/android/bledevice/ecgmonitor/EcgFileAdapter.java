package com.cmtech.android.bledevice.ecgmonitor;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;

import java.io.File;
import java.util.List;

public class EcgFileAdapter extends RecyclerView.Adapter<EcgFileAdapter.ViewHolder> {
    private EcgReplayActivity activity;

    private List<File> fileList;

    private int selectItem = -1;

    Drawable defaultBackground;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View fileView;
        TextView fileName;

        public ViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            fileName = fileView.findViewById(R.id.ecgfile_name);
        }
    }

    public EcgFileAdapter(List<File> fileList, EcgReplayActivity activity) {
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
                selectItem = holder.getAdapterPosition();
                notifyDataSetChanged();
                if(selectItem != -1 && activity != null)
                    activity.replayEcgFile(fileList.get(selectItem));
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(EcgFileAdapter.ViewHolder holder, final int position) {
        File file = fileList.get(position);
        holder.fileName.setText("文件名：" + file.getName());

        if(selectItem == position) {
            holder.fileView.setBackgroundColor(Color.parseColor("#00a0e9"));
        } else {
            holder.fileView.setBackground(defaultBackground);
        }


    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public int getSelectItem() {
        return selectItem;
    }

    public void setSelectItem(int selectItem) {
        if(selectItem >= 0 && selectItem < fileList.size())
            this.selectItem = selectItem;
        else
            this.selectItem = -1;
    }
}
