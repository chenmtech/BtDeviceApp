package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.util.List;

public class EcgReportAdapter extends RecyclerView.Adapter<EcgReportAdapter.ViewHolder> {

    private List<EcgFileComment> commentList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View commentView;
        TextView createTime;
        TextView commentator;
        TextView comment;

        public ViewHolder(View itemView) {
            super(itemView);
            commentView = itemView;
            createTime = commentView.findViewById(R.id.tv_ecgreport_time);
            commentator = commentView.findViewById(R.id.tv_ecgreport_commentator);
            comment = commentView.findViewById(R.id.tv_ecgreport_comment);
        }
    }

    public EcgReportAdapter(List<EcgFileComment> commentList) {
        this.commentList = commentList;
    }

    @Override
    public EcgReportAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecgreport, parent, false);

        final EcgReportAdapter.ViewHolder holder = new EcgReportAdapter.ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(EcgReportAdapter.ViewHolder holder, final int position) {
        EcgFileComment comment = commentList.get(position);
        holder.createTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(comment.getCommentTime()));
        holder.commentator.setText(comment.getCommentator());
        holder.comment.setText(comment.getComment());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void updateCommentList(List<EcgFileComment> commentList) {
        this.commentList = commentList;
    }
}
