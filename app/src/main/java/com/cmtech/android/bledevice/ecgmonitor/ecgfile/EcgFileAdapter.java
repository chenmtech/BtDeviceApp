package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.util.List;

public class EcgFileAdapter extends RecyclerView.Adapter<EcgFileAdapter.ViewHolder> {
    private EcgFileExplorerActivity activity;

    private List<EcgFile> fileList;

    private int selectItem = -1;

    private Drawable defaultBackground;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View fileView;
        TextView fileName;
        TextView fileCreatedPerson;
        TextView fileCreatedTime;
        TextView fileLength;
        TextView fileLastModifyTime;

        public ViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            fileName = fileView.findViewById(R.id.ecgfile_name);
            fileCreatedPerson = fileView.findViewById(R.id.ecgfile_createperson);
            fileCreatedTime = fileView.findViewById(R.id.ecgfile_createtime);
            fileLength = fileView.findViewById(R.id.ecgfile_filelength);
            fileLastModifyTime = fileView.findViewById(R.id.ecgfile_lastmodifytime);
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

        return holder;
    }

    @Override
    public void onBindViewHolder(EcgFileAdapter.ViewHolder holder, final int position) {
        EcgFile file = fileList.get(position);
        holder.fileName.setText(file.getFile().getName().substring(0, file.getFile().getName().length()-4));
        holder.fileCreatedPerson.setText(file.getEcgFileHead().getFileCreatedPerson());
        holder.fileCreatedTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(file.getEcgFileHead().getFileCreatedTime()));
        holder.fileLength.setText(DateTimeUtil.secToTime(file.getDataNum()/file.getFs()));
        holder.fileLastModifyTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(file.getFile().lastModified()));

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
