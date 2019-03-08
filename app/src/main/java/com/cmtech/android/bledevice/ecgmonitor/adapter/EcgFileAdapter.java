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

public class EcgFileAdapter extends RecyclerView.Adapter<EcgFileAdapter.ViewHolder> {
    private EcgFileExplorerModel explorerModel; // 浏览器模型
    private Drawable defaultBackground; // 缺省背景

    static class ViewHolder extends RecyclerView.ViewHolder {
        View fileView;
        TextView tvCreator; // 创建人
        TextView tvCreateTime; // 创建时间
        TextView tvLength; // 信号长度

        ViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            tvCreator = fileView.findViewById(R.id.ecgfile_creator);
            tvCreateTime = fileView.findViewById(R.id.ecgfile_createtime);
            tvLength = fileView.findViewById(R.id.ecgfile_length);
        }
    }

    public EcgFileAdapter(EcgFileExplorerModel explorerModel) {
        this.explorerModel = explorerModel;
    }

    @NonNull
    @Override
    public EcgFileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecgfile, parent, false);
        final EcgFileAdapter.ViewHolder holder = new EcgFileAdapter.ViewHolder(view);
        defaultBackground = holder.fileView.getBackground();

        holder.fileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectPos = holder.getAdapterPosition();
                // 这次点击的未知与已经选择的位置一样，即再次点击
                if(selectPos == explorerModel.getSelectIndex()) {
                    explorerModel.replaySelectFile();
                }
                // 否则仅仅改变选中ecg文件
                else {
                    explorerModel.select(holder.getAdapterPosition());
                }
            }
        });

        /*holder.ibShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                explorerModel.shareSelectFileThroughWechat();
            }
        });
*/
        holder.tvCreator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EcgFile file = explorerModel.getFileList().get(holder.getAdapterPosition());
                User creator = file.getCreator();
                Toast.makeText(MyApplication.getContext(), creator.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(EcgFileAdapter.ViewHolder holder, final int position) {
        EcgFile file = explorerModel.getFileList().get(position);

        User fileCreator = file.getCreator();
        User account = AccountManager.getInstance().getAccount();
        if(fileCreator.equals(account)) {
            holder.tvCreator.setText(Html.fromHtml("<u>您</u>"));
        } else {
            holder.tvCreator.setText(Html.fromHtml("<u>" + file.getCreatorName() + "</u>"));
        }
        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(file.getCreateTime());
        holder.tvCreateTime.setText(createTime);
        String fileTimeLength = DateTimeUtil.secToTime(file.getDataNum()/file.getFs());
        holder.tvLength.setText(fileTimeLength);


        /*int appendixNum = file.getAppendixNum();
        if(appendixNum > 0) {
            IEcgAppendix lastAppendix = file.getAppendixList().get(appendixNum - 1);
            *//*String lastEcgComment = MyApplication.getContext().getResources().getString(R.string.lastecgcomment);
            createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(appendix.getCreateTime());
            String person = appendix.getCreatorName();
            String tvContent;
            if(!(appendix instanceof IEcgAppendixDataLocation)) {
                tvContent = appendix.getContent();
            } else {
                tvContent = MyApplication.getContext().getResources().getString(R.string.comment_with_second);
                int second = (int)(((IEcgAppendixDataLocation) appendix).getLocation()/file.getFs());
                tvContent = String.format(tvContent, DateTimeUtil.secToTime(second), appendix.getContent());
            }
            lastEcgComment = String.format(lastEcgComment, createTime, person, tvContent);

            holder.tvCreateTime.setText(lastEcgComment);*//*
            holder.tvCreateTime.setText(lastAppendix.toStringWithSampleRate(file.getFs()));
        } else {
            holder.tvCreateTime.setText("无留言");
        }*/

        int bgdColor;
        if(explorerModel.getSelectIndex() == position) {
            bgdColor = MyApplication.getContext().getResources().getColor(R.color.secondary);
            holder.fileView.setBackgroundColor(bgdColor);
        } else {
            holder.fileView.setBackground(defaultBackground);
        }
    }

    @Override
    public int getItemCount() {
        return explorerModel.getFileList().size();
    }

}
