package com.cmtech.android.bledevice.ecgmonitor.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.model.IEcgAppendixOperator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.IEcgAppendix;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.IEcgAppendixDataLocation;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.util.List;

public class EcgAppendixAdapter extends RecyclerView.Adapter<EcgAppendixAdapter.ViewHolder> {
    private List<IEcgAppendix> appendixList; // 附加信息列表
    private IEcgAppendixOperator appendixOperator; // 附加信息操作者

    static class ViewHolder extends RecyclerView.ViewHolder {
        View appendixView;
        TextView tvCreateTime;
        TextView tvCreator;
        TextView content;
        ImageButton ibSecondWhenComment;
        ImageButton ibDeleteAppendix;

        private ViewHolder(View itemView) {
            super(itemView);
            appendixView = itemView;
            tvCreateTime = appendixView.findViewById(R.id.tv_ecgreport_time);
            tvCreator = appendixView.findViewById(R.id.tv_ecgreport_commentator);
            content = appendixView.findViewById(R.id.tv_ecgreport_content);
            ibSecondWhenComment = appendixView.findViewById(R.id.ib_ecgcomment_locate);
            ibDeleteAppendix = appendixView.findViewById(R.id.ib_ecgcomment_delete);
        }
    }

    public EcgAppendixAdapter(List<IEcgAppendix> appendixList, IEcgAppendixOperator appendixOperator) {
        this.appendixList = appendixList;
        this.appendixOperator = appendixOperator;
    }

    @NonNull
    @Override
    public EcgAppendixAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecgcomment, parent, false);

        final EcgAppendixAdapter.ViewHolder holder = new EcgAppendixAdapter.ViewHolder(view);

        holder.ibDeleteAppendix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(appendixOperator != null) {
                    appendixOperator.deleteAppendix(appendixList.get(holder.getAdapterPosition()));
                }
            }
        });

        holder.ibSecondWhenComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(appendixOperator != null)
                    appendixOperator.locateAppendix(appendixList.get(holder.getAdapterPosition()));
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(EcgAppendixAdapter.ViewHolder holder, final int position) {
        IEcgAppendix comment = appendixList.get(position);
        holder.tvCreateTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(comment.getCreateTime()));
        holder.tvCreator.setText(comment.getCreator());
        String content;
        if(comment instanceof IEcgAppendixDataLocation) {
            content = MyApplication.getContext().getResources().getString(R.string.comment_with_second);
            int second = (int)(((IEcgAppendixDataLocation) comment).getDataLocation());
            content = String.format(content, second, comment.getContent());
        } else {
            content = comment.getContent();
        }
        holder.content.setText(content);

        if(appendixOperator != null) {
            holder.ibDeleteAppendix.setVisibility(View.VISIBLE);
            if(comment instanceof IEcgAppendixDataLocation) {
                holder.ibSecondWhenComment.setVisibility(View.VISIBLE);
            } else {
                holder.ibSecondWhenComment.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return appendixList.size();
    }

    public void updateCommentList(List<IEcgAppendix> commentList) {
        this.appendixList = commentList;
        notifyDataSetChanged();
    }
}
