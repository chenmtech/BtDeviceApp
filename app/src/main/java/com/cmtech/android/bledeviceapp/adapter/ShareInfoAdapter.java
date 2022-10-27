package com.cmtech.android.bledeviceapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.model.ShareInfo;
import com.cmtech.android.bledeviceapp.util.ClickCheckUtil;

import java.util.List;


/**
 * ShareInfoAdapter : 分享信息Adapter
 * Created by bme on 2022/10/27.
 */

public class ShareInfoAdapter extends RecyclerView.Adapter<ShareInfoAdapter.ViewHolder> {
    private final List<ShareInfo> shareInfos;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view; // 设备视图
        TextView fromUserName; //
        TextView toUserName; //
        TextView status; //
        Button ivDeny;
        Button ivAgree;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            fromUserName = view.findViewById(R.id.tv_from_username);
            toUserName = view.findViewById(R.id.tv_to_username);
            status = view.findViewById(R.id.tv_status);
            ivDeny = view.findViewById(R.id.btn_deny);
            ivAgree = view.findViewById(R.id.btn_agree);
        }
    }

    public ShareInfoAdapter(List<ShareInfo> shareInfos) {
        this.shareInfos = shareInfos;
    }


    @Override
    public ShareInfoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_share_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShareInfoAdapter.ViewHolder holder, int position) {
        ShareInfo shareInfo = shareInfos.get(position);

        int myId = MyApplication.getAccountId();
        if(shareInfo.getFromId() == myId)
            holder.fromUserName.setText("你");
        else
            holder.fromUserName.setText(shareInfo.getFromUserName());

        if(shareInfo.getToId() == myId)
            holder.toUserName.setText("你");
        else
            holder.toUserName.setText(shareInfo.getToUserName());

        String statusStr = "";
        switch (shareInfo.getStatus()) {
            case ShareInfo.DENY:
                statusStr = "已拒绝";
                break;
            case ShareInfo.WAITING:
                statusStr = "申请中";
                break;
            case ShareInfo.AGREE:
                statusStr = "已同意";
                break;
        }
        holder.status.setText(statusStr);

        if(shareInfo.getToId() == MyApplication.getAccountId()) {
            holder.ivDeny.setVisibility(View.VISIBLE);
            holder.ivAgree.setVisibility(View.VISIBLE);
        } else {
            holder.ivDeny.setVisibility(View.GONE);
            holder.ivAgree.setVisibility(View.GONE);
        }

        holder.ivDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickCheckUtil.isFastClick()) return;
                //activity.deleteRecord(holder.getAdapterPosition());
            }
        });

        holder.ivAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickCheckUtil.isFastClick()) return;
                //activity.uploadRecord(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return shareInfos.size();
    }

}
