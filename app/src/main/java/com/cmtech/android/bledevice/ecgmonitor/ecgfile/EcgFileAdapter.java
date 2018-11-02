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
import com.cmtech.android.bledevicecore.model.BleDevice;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class EcgFileAdapter extends RecyclerView.Adapter<EcgFileAdapter.ViewHolder> {
    private EcgFileExplorerActivity activity;

    private List<File> fileList;

    private int selectItem = -1;

    private Drawable defaultBackground;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View fileView;
        TextView fileName;
        TextView fileLastModifyTime;

        public ViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            fileName = fileView.findViewById(R.id.ecgfile_name);
            fileLastModifyTime = fileView.findViewById(R.id.ecgfile_lastmodifytime);
        }
    }

    public EcgFileAdapter(List<File> fileList, EcgFileExplorerActivity activity) {
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
                    activity.selectFile(fileList.get(selectItem));
            }
        });

        holder.fileView.setOnLongClickListener(new View.OnLongClickListener() {
            final MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {         //设置每个菜单的点击动作
                    File file = fileList.get(holder.getAdapterPosition());
                    switch (item.getItemId()){
                        case 1:
                            activity.deleteFile(file);
                            return true;

                        default:
                            return true;
                    }
                }
            };

            @Override
            public boolean onLongClick(View view) {
                selectItem = holder.getAdapterPosition();
                notifyDataSetChanged();
                view.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        MenuItem delete = menu.add(Menu.NONE, 1, 0, "删除");
                        delete.setOnMenuItemClickListener(listener);
                    }
                });
                return false;
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(EcgFileAdapter.ViewHolder holder, final int position) {
        File file = fileList.get(position);
        holder.fileName.setText(file.getName().substring(0, file.getName().length()-4));
        holder.fileLastModifyTime.setText(DateTimeUtil.timeToStringWithSimpleFormat(file.lastModified()));

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