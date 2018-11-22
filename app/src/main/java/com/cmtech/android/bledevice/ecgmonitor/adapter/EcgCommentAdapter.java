package com.cmtech.android.bledevice.ecgmonitor.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgComment;
import com.cmtech.android.bledevice.ecgmonitor.model.IEcgCommentOperator;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.util.List;

public class EcgCommentAdapter extends RecyclerView.Adapter<EcgCommentAdapter.ViewHolder> {

    private List<EcgComment> commentList;

    private IEcgCommentOperator commentOperator;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View commentView;
        TextView createdTime;
        TextView commentator;
        TextView content;
        ImageButton ibDeleteComment;

        private ViewHolder(View itemView) {
            super(itemView);
            commentView = itemView;
            createdTime = commentView.findViewById(R.id.tv_ecgreport_time);
            commentator = commentView.findViewById(R.id.tv_ecgreport_commentator);
            content = commentView.findViewById(R.id.tv_ecgreport_content);
            ibDeleteComment = commentView.findViewById(R.id.ib_ecgcomment_delete);
        }
    }

    public EcgCommentAdapter(List<EcgComment> commentList, IEcgCommentOperator commentOperator) {
        this.commentList = commentList;
        this.commentOperator = commentOperator;
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
                if(commentOperator != null)
                    commentOperator.deleteComment(commentList.get(holder.getAdapterPosition()));
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(EcgCommentAdapter.ViewHolder holder, final int position) {
        EcgComment comment = commentList.get(position);
        holder.createdTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(comment.getCreatedTime()));
        holder.commentator.setText(comment.getCommentator());
        int secondInEcg = comment.getSecondInEcg();
        String content;
        if(secondInEcg != -1) {
            content = MyApplication.getContext().getResources().getString(R.string.comment_with_second);
            content = String.format(content, DateTimeUtil.secToTime(comment.getSecondInEcg()), comment.getContent());
        } else {
            content = comment.getContent();
        }
        holder.content.setText(content);

        if(commentOperator != null) {
            holder.ibDeleteComment.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void updateCommentList(List<EcgComment> commentList) {
        this.commentList = commentList;
        notifyDataSetChanged();
    }
}
