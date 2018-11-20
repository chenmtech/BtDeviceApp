package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.util.List;

public class EcgCommentAdapter extends RecyclerView.Adapter<EcgCommentAdapter.ViewHolder> {

    private List<EcgFileComment> commentList;

    private IEcgCommentObserver observer;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View commentView;
        TextView createTime;
        TextView commentator;
        TextView comment;
        ImageButton ibDeleteComment;

        private ViewHolder(View itemView) {
            super(itemView);
            commentView = itemView;
            createTime = commentView.findViewById(R.id.tv_ecgreport_time);
            commentator = commentView.findViewById(R.id.tv_ecgreport_commentator);
            comment = commentView.findViewById(R.id.tv_ecgreport_comment);
            ibDeleteComment = commentView.findViewById(R.id.ib_ecgcomment_delete);
        }
    }

    public EcgCommentAdapter(List<EcgFileComment> commentList, IEcgCommentObserver observer) {
        this.commentList = commentList;
        this.observer = observer;
    }

    @NonNull
    @Override
    public EcgCommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecgcomment, parent, false);

        final EcgCommentAdapter.ViewHolder holder = new EcgCommentAdapter.ViewHolder(view);

        holder.ibDeleteComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(observer != null)
                    observer.deleteComment(commentList.get(holder.getAdapterPosition()));
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(EcgCommentAdapter.ViewHolder holder, final int position) {
        EcgFileComment comment = commentList.get(position);
        holder.createTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(comment.getCommentTime()));
        holder.commentator.setText(comment.getCommentator());
        holder.comment.setText(comment.getComment());

        if(observer != null) {
            holder.ibDeleteComment.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void updateCommentList(List<EcgFileComment> commentList) {
        this.commentList = commentList;
    }
}
