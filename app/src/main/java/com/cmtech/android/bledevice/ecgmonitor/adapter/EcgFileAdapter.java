package com.cmtech.android.bledevice.ecgmonitor.adapter;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgFileExplorerModel;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.IEcgAppendix;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

public class EcgFileAdapter extends RecyclerView.Adapter<EcgFileAdapter.ViewHolder> {
    private EcgFileExplorerModel explorerModel; // 浏览器模型
    private Drawable defaultBackground; // 缺省背景

    static class ViewHolder extends RecyclerView.ViewHolder {
        View fileView;
        TextView tvCreator; // 创建人
        TextView tvCreateTime; // 创建时间
        TextView tvLastAppendix; // 最后一条附加信息
        ImageButton ibShare; // 分享按钮

        ViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            tvCreator = fileView.findViewById(R.id.ecgfile_createperson);
            tvCreateTime = fileView.findViewById(R.id.ecgfile_createtime);
            tvLastAppendix = fileView.findViewById(R.id.ecgfile_lastcomment);
            ibShare = fileView.findViewById(R.id.ib_ecgfile_share);
        }
    }

    public EcgFileAdapter(EcgFileExplorerModel explorerModel) {
        this.explorerModel = explorerModel;
    }

    @NonNull
    @Override
    public EcgFileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecgfile, parent, false);
        final EcgFileAdapter.ViewHolder holder = new EcgFileAdapter.ViewHolder(view);
        defaultBackground = holder.fileView.getBackground();

        holder.fileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectPos = holder.getAdapterPosition();
                // 已经选择的和这次点击的一样，即再次点击
                if(selectPos == explorerModel.getCurrentSelectIndex()) {
                    explorerModel.playSelectedFile();
                }
                // 否则仅仅改变选中ecg文件
                else {
                    explorerModel.select(holder.getAdapterPosition());
                }
            }
        });

        holder.ibShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                explorerModel.shareSelectFileThroughWechat();
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(EcgFileAdapter.ViewHolder holder, final int position) {
        EcgFile file = explorerModel.getFileList().get(position);

        holder.tvCreator.setText(file.getCreator());

        String createTimeAndLength = MyApplication.getContext().getResources().getString(R.string.ecgfile_createinfo);
        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(file.getCreateTime());
        String fileTimeLength = DateTimeUtil.secToTime(file.getDataNum()/file.getFs());
        createTimeAndLength = String.format(createTimeAndLength, createTime, fileTimeLength);
        holder.tvCreateTime.setText(createTimeAndLength);

        int appendixNum = file.getAppendixNum();
        if(appendixNum > 0) {
            IEcgAppendix lastAppendix = file.getAppendixList().get(appendixNum - 1);
            /*String lastEcgComment = MyApplication.getContext().getResources().getString(R.string.lastecgcomment);
            createTime = DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(appendix.getCreateTime());
            String person = appendix.getCreator();
            String tvContent;
            if(!(appendix instanceof IEcgAppendixDataLocation)) {
                tvContent = appendix.getContent();
            } else {
                tvContent = MyApplication.getContext().getResources().getString(R.string.comment_with_second);
                int second = (int)(((IEcgAppendixDataLocation) appendix).getDataLocation()/file.getFs());
                tvContent = String.format(tvContent, DateTimeUtil.secToTime(second), appendix.getContent());
            }
            lastEcgComment = String.format(lastEcgComment, createTime, person, tvContent);

            holder.tvLastAppendix.setText(lastEcgComment);*/
            holder.tvLastAppendix.setText(lastAppendix.toString(file.getFs()));
        } else {
            holder.tvLastAppendix.setText("无留言");
        }

        int bgdColor;
        if(explorerModel.getCurrentSelectIndex() == position) {
            bgdColor = MyApplication.getContext().getResources().getColor(R.color.secondary);
            holder.fileView.setBackgroundColor(bgdColor);
            holder.ibShare.setBackgroundColor(bgdColor);
            holder.ibShare.setVisibility(View.VISIBLE);
        } else {
            holder.fileView.setBackground(defaultBackground);
            holder.ibShare.setBackground(defaultBackground);
            holder.ibShare.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return explorerModel.getFileList().size();
    }

}
