package com.cmtech.android.bledeviceapp.adapter;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;
import static com.cmtech.android.bledeviceapp.model.ShareInfo.AGREE;
import static com.cmtech.android.bledeviceapp.model.ShareInfo.DENY;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.ShareManageActivity;
import com.cmtech.android.bledeviceapp.asynctask.AccountAsyncTask;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.model.ContactPerson;
import com.cmtech.android.bledeviceapp.model.ShareInfo;
import com.cmtech.android.bledeviceapp.model.WebResponse;
import com.cmtech.android.bledeviceapp.util.ClickCheckUtil;
import com.cmtech.android.bledeviceapp.util.MyBitmapUtil;

import java.util.List;


/**
 * ShareInfoAdapter : 分享信息Adapter
 * Created by bme on 2022/10/27.
 */

public class SharePersonAdapter extends RecyclerView.Adapter<SharePersonAdapter.ViewHolder> {
    private final List<ShareInfo> shareInfos;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view; // 设备视图
        TextView fromName; //
        ImageView ivFromImage;
        TextView toName; //
        ImageView ivToImage;
        TextView status; //
        Button ivDeny;
        Button ivAgree;
        Context context;

        ViewHolder(Context context, View itemView) {
            super(itemView);
            this.context = context;
            view = itemView;
            fromName = view.findViewById(R.id.tv_from_name);
            ivFromImage = view.findViewById(R.id.iv_from_image);
            toName = view.findViewById(R.id.tv_to_name);
            ivToImage = view.findViewById(R.id.iv_to_image);
            status = view.findViewById(R.id.tv_status);
            ivDeny = view.findViewById(R.id.btn_deny);
            ivAgree = view.findViewById(R.id.btn_agree);
        }
    }

    public SharePersonAdapter(List<ShareInfo> shareInfos) {
        this.shareInfos = shareInfos;
    }


    @Override
    public SharePersonAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_share_info, parent, false);
        return new ViewHolder(parent.getContext(), view);
    }

    @Override
    public void onBindViewHolder(@NonNull SharePersonAdapter.ViewHolder holder, int position) {
        ShareInfo shareInfo = shareInfos.get(position);

        int myId = MyApplication.getAccountId();
        int fromId = shareInfo.getFromId();
        Pair<String,String> rtn = getNameAndIcon(fromId, myId);
        holder.fromName.setText(rtn.first);
        if(TextUtils.isEmpty(rtn.second)) {
            holder.ivFromImage.setImageResource(R.mipmap.ic_user_32px);
        } else {
            Bitmap bitmap = MyBitmapUtil.showToDp(rtn.second,  32);
            holder.ivFromImage.setImageBitmap(bitmap);
        }

        int toId = shareInfo.getToId();
        rtn = getNameAndIcon(toId, myId);
        holder.toName.setText(rtn.first);
        if(TextUtils.isEmpty(rtn.second)) {
            holder.ivToImage.setImageResource(R.mipmap.ic_user_32px);
        } else {
            Bitmap bitmap = MyBitmapUtil.showToDp(rtn.second,  32);
            holder.ivToImage.setImageBitmap(bitmap);
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

    private Pair<String, String> getNameAndIcon(int id, int myId) {
        String nameStr = "";
        String icon = "";
        if(id == myId) {
            nameStr = MyApplication.getAccount().getNickNameOrUserId();
            icon = MyApplication.getAccount().getIcon();
        } else {
            ContactPerson cp = MyApplication.getAccount().getContactPerson(id);
            if(cp != null) {
                nameStr = cp.getNickName();
                icon = cp.getIcon();
            } else {
                nameStr = "ID："+ id;
                icon = "";
            }
        }
        return new Pair<>(nameStr, icon);
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
