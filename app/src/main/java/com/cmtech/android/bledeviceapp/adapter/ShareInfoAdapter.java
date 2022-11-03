package com.cmtech.android.bledeviceapp.adapter;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_SUCCESS;
import static com.cmtech.android.bledeviceapp.model.ShareInfo.AGREE;
import static com.cmtech.android.bledeviceapp.model.ShareInfo.DENY;
import static com.cmtech.android.bledeviceapp.util.KMWebService11Util.CMD_CHANGE_SHARE_INFO;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.ShareManageActivity;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.model.WebAsyncTask;
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

public class ShareInfoAdapter extends RecyclerView.Adapter<ShareInfoAdapter.ViewHolder> {
    private final List<ShareInfo> shareInfos;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view; // 设备视图
        TextView tvFromName; //
        TextView tvFromId;
        ImageView ivFromImage;
        TextView tvToName; //
        TextView tvToId;
        ImageView ivToImage;
        TextView tvStatus;
        Context context;

        ViewHolder(Context context, View itemView) {
            super(itemView);
            this.context = context;
            view = itemView;
            tvFromName = view.findViewById(R.id.tv_from_name);
            tvFromId = view.findViewById(R.id.tv_from_id);
            ivFromImage = view.findViewById(R.id.iv_from_image);
            tvToName = view.findViewById(R.id.tv_to_name);
            tvToId = view.findViewById(R.id.tv_to_id);
            ivToImage = view.findViewById(R.id.iv_to_image);
            tvStatus = view.findViewById(R.id.tv_status);
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
        ShareInfo si = shareInfos.get(position);

        int myId = MyApplication.getAccountId();
        int fromId = si.getFromId();
        int toId = si.getToId();

        holder.tvFromId.setText("ID:"+fromId);
        Pair<String,String> rtn = getNameAndIcon(fromId, myId);
        if(TextUtils.isEmpty(rtn.first))
            holder.tvFromName.setVisibility(View.GONE);
        else {
            holder.tvFromName.setVisibility(View.VISIBLE);
            holder.tvFromName.setText(rtn.first);
        }
        if(TextUtils.isEmpty(rtn.second)) {
            holder.ivFromImage.setImageResource(R.mipmap.ic_user_32px);
        } else {
            Bitmap bitmap = MyBitmapUtil.showToDp(rtn.second,  32);
            holder.ivFromImage.setImageBitmap(bitmap);
        }
        holder.ivFromImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPersonNoteInfo(holder.context, fromId);
            }
        });

        holder.tvToId.setText("ID:"+toId);
        rtn = getNameAndIcon(toId, myId);
        if(TextUtils.isEmpty(rtn.first))
            holder.tvToName.setVisibility(View.GONE);
        else {
            holder.tvToName.setVisibility(View.VISIBLE);
            holder.tvToName.setText(rtn.first);
        }
        if(TextUtils.isEmpty(rtn.second)) {
            holder.ivToImage.setImageResource(R.mipmap.ic_user_32px);
        } else {
            Bitmap bitmap = MyBitmapUtil.showToDp(rtn.second,  32);
            holder.ivToImage.setImageBitmap(bitmap);
        }
        holder.ivToImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(si.getStatus() == AGREE)
                    showPersonNoteInfo(holder.context, toId);
            }
        });

        String statusStr = "";
        switch (si.getStatus()) {
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
        holder.tvStatus.setText(statusStr);

        // 长按
        if(toId == MyApplication.getAccountId()) {
            holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                final MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {         //设置每个菜单的点击动作
                        switch (item.getItemId()) {
                            case 1: // agree
                                if (ClickCheckUtil.isFastClick()) return true;
                                changeShareInfo(holder.context, fromId, AGREE);
                                break;
                            case 2: // deny
                                if (ClickCheckUtil.isFastClick()) return true;
                                changeShareInfo(holder.context, fromId, DENY);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                };

                @Override
                public boolean onLongClick(View view) {
                    view.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                        @Override
                        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                            if (si.getStatus() != AGREE) {
                                MenuItem agree = menu.add(Menu.NONE, 1, Menu.NONE, "同意");
                                agree.setOnMenuItemClickListener(listener);
                            }
                            if (si.getStatus() != DENY) {
                                MenuItem deny = menu.add(Menu.NONE, 2, Menu.NONE, "拒绝");
                                deny.setOnMenuItemClickListener(listener);
                            }
                        }
                    });
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return shareInfos.size();
    }

    // 显示个人简介
    private void showPersonNoteInfo(Context context, int id) {
        ContactPerson cp = MyApplication.getAccount().getContactPerson(id);
        if(cp != null && !TextUtils.isEmpty(cp.getNote().trim()))
            Toast.makeText(context, cp.getNote(), Toast.LENGTH_LONG).show();
    }

    // 获取个人昵称和头像路径
    private Pair<String, String> getNameAndIcon(int id, int myId) {
        String nameStr = "";
        String icon = "";
        if(id == myId) {
            nameStr = MyApplication.getAccount().getNickName();
            icon = MyApplication.getAccount().getIcon();
        } else {
            ContactPerson cp = MyApplication.getAccount().getContactPerson(id);
            if(cp != null) {
                nameStr = cp.getNickName();
                icon = cp.getIcon();
            }
        }
        return new Pair<>(nameStr, icon);
    }

    // 修改分享信息
    private void changeShareInfo(Context context, int fromId, int status) {
        MyApplication.getAccount().changeShareInfo(context, fromId, status, new ICodeCallback() {
            @Override
            public void onFinish(int code, String msg) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                if (code == RCODE_SUCCESS) {
                    ((ShareManageActivity)context).updateShareInfoList();
                }
            }
        });
    }
}
