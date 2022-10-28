package com.cmtech.android.bledeviceapp.adapter;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;
import static com.cmtech.android.bledeviceapp.model.ShareInfo.AGREE;
import static com.cmtech.android.bledeviceapp.model.ShareInfo.DENY;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.ShareManageActivity;
import com.cmtech.android.bledeviceapp.asynctask.AccountAsyncTask;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.model.ContactPerson;
import com.cmtech.android.bledeviceapp.model.ShareInfo;
import com.cmtech.android.bledeviceapp.model.WebResponse;
import com.cmtech.android.bledeviceapp.util.ClickCheckUtil;
import com.vise.log.ViseLog;

import java.util.List;


/**
 * ShareInfoAdapter : 分享信息Adapter
 * Created by bme on 2022/10/27.
 */

public class ShareInfoAdapter extends RecyclerView.Adapter<ShareInfoAdapter.ViewHolder> {
    private final List<ShareInfo> shareInfos;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view; // 设备视图
        TextView fromId; //
        TextView toId; //
        TextView status; //
        Button ivDeny;
        Button ivAgree;
        Context context;

        ViewHolder(Context context, View itemView) {
            super(itemView);
            this.context = context;
            view = itemView;
            fromId = view.findViewById(R.id.tv_from_id);
            toId = view.findViewById(R.id.tv_to_id);
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
        return new ViewHolder(parent.getContext(), view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShareInfoAdapter.ViewHolder holder, int position) {
        ShareInfo shareInfo = shareInfos.get(position);

        int myId = MyApplication.getAccountId();
        if(shareInfo.getFromId() == myId)
            holder.fromId.setText(MyApplication.getAccount().getNickNameOrUserId());
        else {
            int id = shareInfo.getFromId();
            ContactPerson cp = MyApplication.getAccount().getContactPerson(id);
            if(cp != null) {
                holder.fromId.setText(cp.getNickName());
            } else {
                holder.fromId.setText("ID："+ id);
                /*holder.fromId.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MyApplication.getAccount().downloadContactPerson(holder.context, "请稍等", id, new ICodeCallback() {
                            @Override
                            public void onFinish(int code) {
                                if (code == RETURN_CODE_SUCCESS) {
                                    MyApplication.getAccount().readContactPeopleFromLocalDb();
                                    notifyDataSetChanged();
                                    ViseLog.e(MyApplication.getAccount().getContactPeople());
                                }
                            }
                        });
                    }
                });*/
            }
        }

        if(shareInfo.getToId() == myId)
            holder.toId.setText(MyApplication.getAccount().getNickNameOrUserId());
        else {
            int id = shareInfo.getToId();
            ContactPerson cp = MyApplication.getAccount().getContactPerson(id);
            if(cp != null) {
                holder.toId.setText(cp.getNickName());
            } else {
                holder.toId.setText("ID："+ id);
                /*if(shareInfo.getStatus()==AGREE) {
                    holder.toId.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MyApplication.getAccount().downloadContactPerson(holder.context, "请稍等", id, new ICodeCallback() {
                                @Override
                                public void onFinish(int code) {
                                    if (code == RETURN_CODE_SUCCESS) {
                                        MyApplication.getAccount().readContactPeopleFromLocalDb();
                                        notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    });
                }*/
            }
        }

        String statusStr = "";
        switch (shareInfo.getStatus()) {
            case DENY:
                statusStr = "被拒绝";
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
                if(shareInfo.getStatus() != DENY)
                    changeShareInfo(holder.context, shareInfo.getFromId(), DENY);
            }
        });

        holder.ivAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickCheckUtil.isFastClick()) return;
                if(shareInfo.getStatus() != AGREE)
                    changeShareInfo(holder.context, shareInfo.getFromId(), AGREE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return shareInfos.size();
    }

    private void changeShareInfo(Context context, int fromId, int status) {
        new AccountAsyncTask(context, "请稍等",
                AccountAsyncTask.CMD_CHANGE_SHARE_INFO, new Object[]{fromId, status}, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                if (code == RETURN_CODE_SUCCESS) {
                    ((ShareManageActivity)context).updateShareInfoList();
                    Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "修改失败", Toast.LENGTH_SHORT).show();
                }
            }
        }).execute(MyApplication.getAccount());
    }
}
