package com.cmtech.android.bledevice.ecgmonitor.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.UserManager;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EcgCommentAdapter extends RecyclerView.Adapter<EcgCommentAdapter.ViewHolder> {
    private List<EcgNormalComment> commentList; // 留言列表

    private final OnEcgCommentListener listener; // 附加信息操作者

    public interface OnEcgCommentListener {
        void onCommentSaved(); // 保存留言
        void onCommentDeleted(EcgNormalComment comment); // 删除留言
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View appendixView;
        TextView tvCreatorName;
        TextView tvModifyTime;
        EditText etContent;
        ImageButton ibSave;

        private ViewHolder(View itemView) {
            super(itemView);
            appendixView = itemView;
            etContent = appendixView.findViewById(R.id.et_comment_content);
            tvCreatorName = appendixView.findViewById(R.id.tv_comment_creator);
            tvModifyTime = appendixView.findViewById(R.id.ecgappendix_modifytime);
            ibSave = appendixView.findViewById(R.id.ib_save_comment);
        }
    }

    public EcgCommentAdapter(List<EcgNormalComment> commentList, OnEcgCommentListener listener) {
        if(commentList == null) this.commentList = new ArrayList<>();
        else this.commentList = commentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EcgCommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecg_comment, parent, false);

        final EcgCommentAdapter.ViewHolder holder = new EcgCommentAdapter.ViewHolder(view);

        holder.tvCreatorName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User creator = commentList.get(holder.getAdapterPosition()).getCreator();
                Toast.makeText(MyApplication.getContext(), creator.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User creator = commentList.get(holder.getAdapterPosition()).getCreator();
                User account = UserManager.getInstance().getUser();
                if(listener != null && creator.equals(account)) {
                    EcgNormalComment comment = commentList.get(holder.getAdapterPosition());
                    comment.setContent(holder.etContent.getText().toString());
                    long modifyTime = new Date().getTime();
                    comment.setModifyTime(modifyTime);
                    listener.onCommentSaved();
                    holder.tvModifyTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterday(modifyTime));
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final EcgCommentAdapter.ViewHolder holder, final int position) {
        EcgNormalComment comment = commentList.get(position);
        User creator = comment.getCreator();
        User account = UserManager.getInstance().getUser();
        if(creator.equals(account)) {
            holder.tvCreatorName.setText(Html.fromHtml("<u>您</u>"));
            holder.etContent.setHint("请输入。");
        } else {
            holder.tvCreatorName.setText(Html.fromHtml("<u>" + comment.getCreator().getName() + "</u>"));
            holder.etContent.setHint("");
        }

        holder.tvModifyTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterday(comment.getModifyTime()));
        holder.etContent.setText(comment.getContent());

        if(listener != null && creator.equals(account)) {
            holder.ibSave.setVisibility(View.VISIBLE);
            holder.etContent.setEnabled(true);
            holder.etContent.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    holder.etContent.setFocusableInTouchMode(true);
                    return false;
                }
            });
        } else {
            holder.ibSave.setVisibility(View.GONE);
            holder.etContent.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void updateCommentList(List<EcgNormalComment> commentList) {
        this.commentList = commentList;
        notifyDataSetChanged();
    }

}
